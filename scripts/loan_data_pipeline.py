#!/usr/bin/env python3
"""Fetch, validate, and normalize loan product data from the public API."""

import argparse
import datetime as dt
import hashlib
import json
import os
import re
import sys
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path


DEFAULT_BASE_URL = "https://apis.data.go.kr/B553701/LoanProductSearchingInfo"
DEFAULT_API_PATH = "/LoanProductSearchingInfo/getLoanProductSearchingInfo"
REQUIRED_FIELDS = ("finPrdNm", "lnLmt", "irt", "hdlInst")


def main():
    args = parse_args()
    generated_at = dt.datetime.now(dt.timezone.utc).isoformat()

    if args.sample:
        xml_text = Path(args.sample_file).read_text(encoding="utf-8")
        source_url = "sample://" + args.sample_file
    else:
        service_key = args.service_key or os.getenv("DATA_GO_KR_SERVICE_KEY", "")
        if not service_key:
            print("DATA_GO_KR_SERVICE_KEY is required unless --sample is used.", file=sys.stderr)
            return 2

        source_url = build_url(args.base_url, args.path, service_key, args.page_no, args.num_rows)
        xml_text = fetch_xml(source_url, args.timeout)

    parsed = parse_response(xml_text)
    warnings = validate(parsed, strict=args.strict)
    products = normalize_products(parsed["items"])

    output = {
        "generatedAt": generated_at,
        "source": {
            "baseUrl": args.base_url,
            "path": args.path,
            "pageNo": args.page_no,
            "numOfRows": args.num_rows,
            "sample": args.sample,
        },
        "header": parsed["header"],
        "totalCount": parsed["total_count"],
        "count": len(products),
        "warnings": warnings,
        "products": products,
    }

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    manifest = {
        "generatedAt": generated_at,
        "sourceUrl": redact_service_key(source_url),
        "output": str(output_path),
        "sha256": sha256(output_path),
        "count": len(products),
        "warnings": warnings,
    }
    manifest_path = Path(args.manifest_output)
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    print(json.dumps({"count": len(products), "warnings": len(warnings), "output": str(output_path)}, ensure_ascii=False))
    return 0


def parse_args():
    parser = argparse.ArgumentParser(description="Loan product data CI/CD pipeline")
    parser.add_argument("--base-url", default=os.getenv("LOAN_API_ENDPOINT", DEFAULT_BASE_URL))
    parser.add_argument("--path", default=os.getenv("LOAN_API_PATH", DEFAULT_API_PATH))
    parser.add_argument("--service-key", default=os.getenv("DATA_GO_KR_SERVICE_KEY", ""))
    parser.add_argument("--page-no", type=int, default=int(os.getenv("LOAN_API_PAGE_NO", "1")))
    parser.add_argument("--num-rows", type=int, default=int(os.getenv("LOAN_API_PAGE_SIZE", "100")))
    parser.add_argument("--timeout", type=int, default=20)
    parser.add_argument("--output", default="data/loan-products.json")
    parser.add_argument("--manifest-output", default="data/loan-products.manifest.json")
    parser.add_argument("--sample", action="store_true", help="Use the checked-in sample XML instead of the live API")
    parser.add_argument("--sample-file", default="src/test/resources/sample-loan-products.xml")
    parser.add_argument("--strict", action="store_true", help="Fail on API errors or empty product data")
    return parser.parse_args()


def build_url(base_url, path, service_key, page_no, num_rows):
    url = base_url.rstrip("/") + "/" + path.lstrip("/")
    query = urllib.parse.urlencode(
        {
            "serviceKey": service_key,
            "pageNo": page_no,
            "numOfRows": num_rows,
            "type": "xml",
        },
        safe="%",
    )
    return url + "?" + query


def fetch_xml(url, timeout):
    request = urllib.request.Request(url, headers={"User-Agent": "daechul-alio-data-pipeline/1.0"})
    with urllib.request.urlopen(request, timeout=timeout) as response:
        charset = response.headers.get_content_charset() or "utf-8"
        return response.read().decode(charset, errors="replace")


def parse_response(xml_text):
    root = ET.fromstring(xml_text)
    header = {
        "resultCode": find_text(root, ".//header/resultCode"),
        "resultMsg": find_text(root, ".//header/resultMsg"),
    }
    total_count_text = find_text(root, ".//body/totalCount")
    items = root.findall(".//items/item")
    return {
        "header": header,
        "total_count": to_int(total_count_text),
        "items": items,
    }


def validate(parsed, strict):
    warnings = []
    result_code = parsed["header"].get("resultCode")
    if result_code and result_code != "00":
        message = "API returned resultCode={} resultMsg={}".format(
            result_code,
            parsed["header"].get("resultMsg", ""),
        )
        if strict:
            raise SystemExit(message)
        warnings.append(message)

    if not parsed["items"]:
        message = "No loan product item nodes were found."
        if strict:
            raise SystemExit(message)
        warnings.append(message)

    for index, item in enumerate(parsed["items"], start=1):
        missing = [field for field in REQUIRED_FIELDS if not child_text(item, field)]
        if missing:
            warnings.append("item {} missing required fields: {}".format(index, ", ".join(missing)))

    return warnings


def normalize_products(items):
    products = []
    for index, item in enumerate(items, start=1):
        name = child_text(item, "finPrdNm")
        institution = first_child_text(item, "hdlInst", "ofrInstNm", "hdlInstDtlVw")
        product = {
            "id": child_text(item, "seq") or slug("{}-{}-{}".format(name, institution, index)),
            "name": name,
            "institution": institution,
            "provider": child_text(item, "ofrInstNm"),
            "institutionCategory": child_text(item, "instCtg"),
            "productCategory": child_text(item, "prdCtg"),
            "loanLimit": child_text(item, "lnLmt"),
            "interestRate": child_text(item, "irt"),
            "interestRateType": child_text(item, "irtCtg"),
            "purpose": child_text(item, "usge"),
            "totalLoanTermYears": child_text(item, "maxTotLnTrm"),
            "repaymentTermYears": child_text(item, "maxRdptTrm"),
            "defermentTermYears": child_text(item, "maxDfrmTrm"),
            "repaymentMethod": child_text(item, "rdptMthd"),
            "target": child_text(item, "trgt"),
            "targetFilter": child_text(item, "tgtFltr"),
            "supportTargetDetail": child_text(item, "suprTgtDtlCond"),
            "annualIncome": first_child_text(item, "anin", "incm"),
            "incomeCondition": child_text(item, "incmCnd"),
            "creditScore": child_text(item, "crdtSc"),
            "age": child_text(item, "age"),
            "region": first_child_text(item, "rsdArea", "rsdAreaPamtEqltIstm"),
            "applicationMethod": child_text(item, "jnMthd"),
            "contact": first_child_text(item, "rfrcCnpl", "cnpl"),
            "relatedSite": child_text(item, "rltSite"),
            "guaranteeInstitution": child_text(item, "grnInst"),
            "extraNotes": first_child_text(item, "kinfaPrdEtc", "etcRefSbjc"),
            "rawFlags": normalize_flags(item),
        }
        products.append(product)
    return products


def normalize_flags(item):
    names = [
        "age_39Blw",
        "age_40Abnml",
        "age_60Abnml",
        "crdtSc_1",
        "crdtSc_2",
        "crdtSc_3",
        "crdtSc_4",
        "crdtSc_5",
        "crdtSc_6",
        "crdtSc_7",
        "crdtSc_8",
        "crdtSc_9",
        "crdtSc_0",
        "crdtSc_1_5",
        "crdtSc_6_0",
        "incmCndY",
        "incmCndN",
        "lnLmt_1000Abnml",
        "lnLmt_2000Abnml",
        "lnLmt_3000Abnml",
        "lnLmt_5000Abnml",
        "lnLmt_10000Abnml",
        "kinfaPrdYn",
    ]
    return {name: child_text(item, name) for name in names if child_text(item, name)}


def find_text(root, path):
    node = root.find(path)
    return "" if node is None or node.text is None else node.text.strip()


def child_text(item, name):
    node = item.find(name)
    return "" if node is None or node.text is None else node.text.strip()


def first_child_text(item, *names):
    for name in names:
        value = child_text(item, name)
        if value:
            return value
    return ""


def to_int(value):
    try:
        return int(value)
    except (TypeError, ValueError):
        return None


def slug(value):
    normalized = re.sub(r"[^0-9a-zA-Z가-힣]+", "-", value.lower()).strip("-")
    return normalized or "loan-product"


def redact_service_key(url):
    return re.sub(r"(serviceKey=)[^&]+", r"\1***", url)


def sha256(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as file:
        for chunk in iter(lambda: file.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


if __name__ == "__main__":
    raise SystemExit(main())
