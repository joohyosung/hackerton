# 데이터 CI/CD 파이프라인

대출알리오의 데이터 파이프라인은 공공데이터 API 응답을 검증하고, 앱이 재사용할 수 있는 JSON 데이터셋으로 정규화하는 흐름입니다.

## API 계약

- Base URL: `https://apis.data.go.kr/B553701/LoanProductSearchingInfo`
- Path: `/LoanProductSearchingInfo/getLoanProductSearchingInfo`
- Method: `GET`
- Format: XML
- Required query:
  - `serviceKey`
  - `pageNo`
  - `numOfRows`
  - `type=xml`

## 흐름

1. **계약 검증**
   - `src/test/resources/sample-loan-products.xml`로 XML 구조와 필수 필드를 검증합니다.
   - PR과 push에서 항상 실행되므로 API 키 없이도 CI가 동작합니다.

2. **실데이터 수집**
   - GitHub Actions `Data Pipeline` 워크플로가 `DATA_GO_KR_SERVICE_KEY` 시크릿을 사용해 실제 API를 호출합니다.
   - 매일 오전 5시(KST)에 스케줄 실행됩니다.

3. **정규화**
   - `scripts/loan_data_pipeline.py`가 XML의 `items.item`을 읽어 `data/loan-products.json`으로 변환합니다.
   - 생성 결과의 건수, SHA-256, 경고는 `data/loan-products.manifest.json`에 기록합니다.

4. **전달**
   - 기본적으로 GitHub Actions artifact로 데이터셋을 저장합니다.
   - 수동 실행 시 `commit_dataset=true`를 선택하면 생성된 `data/*.json` 파일을 현재 브랜치에 커밋합니다.

## GitHub Secret 설정

GitHub 저장소에서 다음 경로로 이동합니다.

`Settings` -> `Secrets and variables` -> `Actions` -> `New repository secret`

추가할 값:

- Name: `DATA_GO_KR_SERVICE_KEY`
- Secret: 공공데이터 포털에서 발급받은 서비스 키

## 로컬 실행

샘플 데이터 검증:

```bash
python scripts/loan_data_pipeline.py --sample --strict
```

실제 API 호출:

```bash
set DATA_GO_KR_SERVICE_KEY=발급받은_API_키
python scripts/loan_data_pipeline.py --strict
```

PowerShell:

```powershell
$env:DATA_GO_KR_SERVICE_KEY="발급받은_API_키"
python scripts/loan_data_pipeline.py --strict
```

## 실패 기준

- `resultCode`가 `00`이 아니면 실데이터 파이프라인이 실패합니다.
- `items.item`이 하나도 없으면 실패합니다.
- `finPrdNm`, `lnLmt`, `irt`, `hdlInst` 중 누락 필드가 있으면 경고가 기록됩니다.
