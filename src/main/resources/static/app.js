const PAGE_SIZE = 10;

const form = document.querySelector("#loan-form");
const resultSection = document.querySelector("#result-section");
const results = document.querySelector("#results");
const message = document.querySelector("#message");
const resultCount = document.querySelector("#result-count");
const pagination = document.querySelector("#pagination");
const modal = document.querySelector("#product-modal");
const modalDialog = modal.querySelector(".modal-dialog");
const modalClose = document.querySelector("#modal-close");
const modalTitle = document.querySelector("#modal-title");
const modalSummary = document.querySelector("#modal-summary");
const modalDetails = document.querySelector("#modal-details");
const modalReasons = document.querySelector("#modal-reasons");

let currentResults = [];
let currentPage = 1;
let lastFocusedElement = null;

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  currentPage = 1;
  await searchLoans();
});

modalClose.addEventListener("click", closeModal);

modal.addEventListener("click", (event) => {
  if (event.target === modal) {
    closeModal();
  }
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && !modal.hidden) {
    closeModal();
  }
});

async function searchLoans() {
  setLoading(true);
  resultSection.hidden = false;
  message.textContent = "검색 중입니다.";
  results.innerHTML = "";
  pagination.innerHTML = "";
  resultCount.textContent = "0개";

  const payload = {
    age: numberValue("age"),
    annualIncome: toWon(numberValue("annualIncome")),
    creditGrade: numberValue("creditGrade"),
    loanAmount: toWon(numberValue("loanAmount")),
    region: textValue("region"),
    purpose: textValue("purpose"),

    existingMonthlyDebtPayment: toWon(numberValue("existingMonthlyDebtPayment")),
    existingAnnualDebtInterest: toWon(numberValue("existingAnnualDebtInterest")),
    desiredLoanTermYears: numberValue("desiredLoanTermYears"),
    expectedInterestRate: numberValue("expectedInterestRate"),

    mortgageLoan: checkedValue("mortgageLoan"),
    collateralValue: toWon(numberValue("collateralValue")),
    existingMortgageBalance: toWon(numberValue("existingMortgageBalance")),
    seniorDeposit: toWon(numberValue("seniorDeposit")),
    houseCount: numberValue("houseCount"),
    houseArea: numberValue("houseArea"),
    firstHomeBuyer: booleanValue("firstHomeBuyer"),
  };

  try {
    const response = await fetch("/api/loans/search", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error("search failed");
    }

    const data = await response.json();
    renderResults(Array.isArray(data) ? data : data.recommendations || []);
  } catch (error) {
    currentResults = [];
    resultCount.textContent = "0개";
    message.textContent = "검색 결과를 불러오지 못했습니다.";
  } finally {
    setLoading(false);
  }
}

function renderResults(items) {
  currentResults = items;
  resultCount.textContent = `${items.length.toLocaleString("ko-KR")}개`;
  message.textContent = items.length ? "" : "조건에 맞는 상품이 없습니다.";
  currentPage = Math.min(currentPage, Math.max(getTotalPages(), 1));
  renderPage();
}

function renderPage() {
  results.innerHTML = "";

  if (!currentResults.length) {
    pagination.innerHTML = "";
    return;
  }

  const start = (currentPage - 1) * PAGE_SIZE;
  const pageItems = currentResults.slice(start, start + PAGE_SIZE);

  pageItems.forEach((item, index) => {
    results.appendChild(createResultCard(item, start + index + 1));
  });

  renderPagination();
}

function createResultCard(item, rank) {
  const product = item.product || {};
  const card = document.createElement("button");
  card.type = "button";
  card.className = "result-card";
  card.setAttribute("aria-label", `${productName(product)} 상세 보기`);

  card.innerHTML = `
    <span class="rank">${rank}</span>
    <span class="result-body">
      <span class="result-title-row">
        <strong>${escapeHtml(productName(product))}</strong>
        <em>${escapeHtml(product.institution || product.ofrInstNm || "기관 확인")}</em>
      </span>
      <span class="result-summary">${escapeHtml(product.summary || product.target || "상세 조건 확인이 필요합니다.")}</span>
      <span class="metric-row">
        <span>${escapeHtml(product.limitText || product.lnLmt || "한도 확인")}</span>
        <span>${escapeHtml(product.rateText || product.irt || "금리 확인")}</span>
        <span>${escapeHtml(product.region || product.rsdArea || "지역 확인")}</span>
        ${ratioBadges(item.affordability)}
      </span>
    </span>
  `;

  card.addEventListener("click", () => openModal(item));
  return card;
}

function renderPagination() {
  const totalPages = getTotalPages();
  pagination.innerHTML = "";

  if (totalPages <= 1) {
    return;
  }

  pagination.appendChild(createPageButton("이전", currentPage - 1, currentPage === 1));

  getVisiblePages(totalPages).forEach((page) => {
    if (page === "...") {
      const ellipsis = document.createElement("span");
      ellipsis.className = "pagination-ellipsis";
      ellipsis.textContent = "...";
      pagination.appendChild(ellipsis);
      return;
    }

    const button = createPageButton(String(page), page, false);
    button.setAttribute("aria-current", page === currentPage ? "page" : "false");
    pagination.appendChild(button);
  });

  pagination.appendChild(createPageButton("다음", currentPage + 1, currentPage === totalPages));
}

function createPageButton(label, page, disabled) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "page-button";
  button.textContent = label;
  button.disabled = disabled;
  button.addEventListener("click", () => {
    currentPage = page;
    renderPage();
    resultSection.scrollIntoView({ behavior: "smooth", block: "start" });
  });
  return button;
}

function getVisiblePages(totalPages) {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1);
  }

  const pages = [1];
  const start = Math.max(2, currentPage - 1);
  const end = Math.min(totalPages - 1, currentPage + 1);

  if (start > 2) {
    pages.push("...");
  }

  for (let page = start; page <= end; page += 1) {
    pages.push(page);
  }

  if (end < totalPages - 1) {
    pages.push("...");
  }

  pages.push(totalPages);
  return pages;
}

function openModal(item) {
  const product = item.product || {};
  const affordability = item.affordability || {};

  lastFocusedElement = document.activeElement;
  modalTitle.textContent = productName(product);
  modalSummary.textContent = product.summary || product.target || "상세 설명이 제공되지 않았습니다.";
  modalDetails.innerHTML = "";
  modalReasons.innerHTML = "";

  const detailRows = [
    ["취급기관", product.institution || product.ofrInstNm],
    ["제공기관", product.ofrInstNm],
    ["대출한도", product.limitText || product.lnLmt || formatWon(product.limitAmount)],
    ["금리", [product.rateType, product.rateText || product.irt].filter(Boolean).join(" · ")],
    ["용도", product.purpose || product.usge],
    ["대상", product.target || product.trgt],
    ["거주지역", product.region || product.rsdArea],
    ["기간", product.periodText || product.maxTotLnTrm],
    ["상환방법", product.repaymentMethod || product.rdptMthd],
    ["신청방법", product.applicationMethod || product.jnMthd],
    ["문의처", product.contact || product.rfrcCnpl],
    ["보증기관", product.guaranteeInstitution || product.grnInst],
    ["예상 월상환액", formatWon(affordability.estimatedMonthlyPayment)],
    ["DSR", ratioText(affordability.dsr, affordability.dsrLimit)],
    ["DTI", affordability.mortgageEvaluationUsed ? ratioText(affordability.dti, affordability.dtiLimit) : "비담보대출 계산 제외"],
    ["LTV", affordability.mortgageEvaluationUsed ? ratioText(affordability.ltv, affordability.ltvLimit) : "비담보대출 계산 제외"],
    ["계산 기준 금리", affordability.calculationRate == null ? "" : `${percentText(affordability.calculationRate)} 적용`],
    ["계산 기준 기간", affordability.calculationTermYears ? `${affordability.calculationTermYears}년` : ""],
    ["계산상 가능 한도", formatWon(affordability.finalPossibleLoanAmount)],
    ["관련 사이트", product.relatedSite || product.sourceUrl || product.rltSite],
    ["기타 참고사항", product.extraNotes || product.etcRefSbjc || product.kinfaPrdEtc],
  ];

  detailRows.forEach(([label, value]) => appendDetailRow(label, value));
  appendNotes("추천 사유", item.reasons);
  appendNotes("경고 메시지", [...(item.warnings || []), ...((affordability && affordability.warnings) || [])]);

  const notice = document.createElement("section");

  notice.innerHTML = `
      <h3>금융 계산 안내</h3>
      <p>
        • DSR 기준 : 40%
        • DTI 기준 : 40%
        • LTV 기준 : 70%(생애최초 80%)
      </p>
      <p>
          DSR, DTI, LTV 및 예상 월상환액은
          입력한 정보를 기반으로 계산한 <strong>참고용 추정치</strong>입니다.
      </p>
      <p>
          실제 대출 가능 여부와 한도는
          금융기관의 심사 결과에 따라 달라질 수 있습니다.
      </p>
  `;

  modalReasons.appendChild(notice);

  modal.hidden = false;
  document.body.classList.add("modal-open");
  modalDialog.focus();
}

function closeModal() {
  modal.hidden = true;
  document.body.classList.remove("modal-open");

  if (lastFocusedElement && typeof lastFocusedElement.focus === "function") {
    lastFocusedElement.focus();
  }
}

function appendDetailRow(label, value) {
  const normalized = normalizeText(value);
  const row = document.createElement("div");
  const term = document.createElement("dt");
  const description = document.createElement("dd");

  term.textContent = label;
  description.textContent = normalized || "확인 필요";
  row.append(term, description);
  modalDetails.appendChild(row);
}

function appendNotes(title, notes) {
  const validNotes = (notes || []).filter(Boolean);

  if (!validNotes.length) {
    return;
  }

  const section = document.createElement("section");
  const heading = document.createElement("h3");
  const list = document.createElement("ul");

  heading.textContent = title;
  validNotes.forEach((note) => {
    const item = document.createElement("li");
    item.textContent = note;
    list.appendChild(item);
  });

  section.append(heading, list);
  modalReasons.appendChild(section);
}

function setLoading(isLoading) {
  const button = form.querySelector("button");
  button.disabled = isLoading;
  button.querySelector("span:last-child").textContent = isLoading ? "검색 중" : "검색";
}

function ratioBadges(affordability) {
  if (!affordability) {
    return "";
  }

  const badges = [`<span>DSR ${escapeHtml(percentText(affordability.dsr))}</span>`];

  if (affordability.mortgageEvaluationUsed) {
    badges.push(`<span>DTI ${escapeHtml(percentText(affordability.dti))}</span>`);
    badges.push(`<span>LTV ${escapeHtml(percentText(affordability.ltv))}</span>`);
  }

  if (affordability.estimatedMonthlyPayment) {
    badges.push(`<span>월 ${escapeHtml(formatWon(affordability.estimatedMonthlyPayment))}</span>`);
  }

  return badges.join("");
}

function getTotalPages() {
  return Math.ceil(currentResults.length / PAGE_SIZE);
}

function productName(product) {
  return product.name || product.finPrdNm || "상품명 확인";
}

function numberValue(id) {
  const value = document.querySelector(`#${id}`).value;
  return value ? Number(value) : null;
}

function textValue(id) {
  return document.querySelector(`#${id}`).value.trim();
}

function checkedValue(id) {
  return document.querySelector(`#${id}`).checked;
}

function booleanValue(id) {
  return document.querySelector(`#${id}`).value === "true";
}

function ratioText(value, limit) {
  if (value == null) {
    return "";
  }

  return `${percentText(value)} / 기준 ${percentText(limit)}`;
}

function percentText(value) {
  if (value == null || Number.isNaN(Number(value))) {
    return "확인 필요";
  }

  return `${Number(value).toFixed(1)}%`;
}

function toWon(manwon) {
  return manwon == null ? null : manwon * 10000;
}

function formatWon(value) {
  if (value == null || value === "") {
    return "";
  }

  const number = Number(value);

  if (!Number.isFinite(number)) {
    return "";
  }

  if (number === 0) {
    return "0원";
  }

  if (number >= 100000000) {
    return `${Math.round(number / 10000000) / 10}억원`;
  }

  return `${Math.round(number / 10000).toLocaleString("ko-KR")}만원`;
}

function normalizeText(value) {
  if (value == null) {
    return "";
  }

  return String(value).trim();
}

function escapeHtml(value) {
  return String(value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
