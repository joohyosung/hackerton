const form = document.querySelector("#loan-form");
const results = document.querySelector("#results");
const message = document.querySelector("#message");
const resultCount = document.querySelector("#result-count");
const detailEmpty = document.querySelector("#detail-empty");
const detailContent = document.querySelector("#detail-content");

const detailFields = {
  title: document.querySelector("#detail-title"),
  institution: document.querySelector("#detail-institution"),
  limit: document.querySelector("#detail-limit"),
  rate: document.querySelector("#detail-rate"),
  purpose: document.querySelector("#detail-purpose"),
  period: document.querySelector("#detail-period"),
  target: document.querySelector("#detail-target"),
  region: document.querySelector("#detail-region"),
  monthlyPayment: document.querySelector("#detail-monthly-payment"),
  dsr: document.querySelector("#detail-dsr"),
  dti: document.querySelector("#detail-dti"),
  ltv: document.querySelector("#detail-ltv"),
  calculationRate: document.querySelector("#detail-calculation-rate"),
  calculationTerm: document.querySelector("#detail-calculation-term"),
  possibleLimit: document.querySelector("#detail-possible-limit"),
  summary: document.querySelector("#detail-summary"),
};

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  await searchLoans();
});

window.addEventListener("DOMContentLoaded", () => {
  searchLoans();
});

async function searchLoans() {
  setLoading(true);

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
    renderResults(data);
  } catch (error) {
    results.innerHTML = "";
    resultCount.textContent = "0개";
    message.textContent = "추천 결과를 불러오지 못했습니다.";
  } finally {
    setLoading(false);
  }
}

function renderResults(items) {
  results.innerHTML = "";
  resultCount.textContent = `${items.length}개`;
  message.textContent = items.length ? "" : "조건에 맞는 상품이 없습니다.";

  if (!items.length) {
    clearDetail();
  }

  items.forEach((item, index) => {
    const product = item.product;
    const card = document.createElement("article");
    card.className = "result-card";
    card.tabIndex = 0;
    card.setAttribute("role", "button");
    card.setAttribute("aria-label", `${product.name} 상세 보기`);
    card.innerHTML = `
      <div class="score-box">
        <strong>${item.score}</strong>
        <span>점</span>
      </div>
      <div class="result-body">
        <div class="result-title-row">
          <h3>${escapeHtml(product.name)}</h3>
          <span>${escapeHtml(product.institution || "기관 확인")}</span>
        </div>
        <p>${escapeHtml(product.summary || product.target || "상세 조건 확인이 필요합니다.")}</p>
        <div class="metric-row">
          <span>${escapeHtml(product.limitText || "한도 확인")}</span>
          <span>${escapeHtml(product.rateText || "금리 확인")}</span>
          <span>${escapeHtml(product.region || "지역 확인")}</span>
          ${ratioBadges(item.affordability)}
        </div>
        <ul class="reason-list">
          ${item.reasons.map((reason) => `<li>${escapeHtml(reason)}</li>`).join("")}
        </ul>
      </div>
    `;

    function ratioBadges(affordability) {
      if (!affordability) {
        return "";
      }

      const badges = [];

      badges.push(`<span>DSR ${escapeHtml(percentText(affordability.dsr))}</span>`);

      if (affordability.mortgageEvaluationUsed) {
        badges.push(`<span>DTI ${escapeHtml(percentText(affordability.dti))}</span>`);
        badges.push(`<span>LTV ${escapeHtml(percentText(affordability.ltv))}</span>`);
      }

      if (affordability.estimatedMonthlyPayment) {
        badges.push(`<span>월 ${escapeHtml(formatWon(affordability.estimatedMonthlyPayment))}</span>`);
      }

      return badges.join("");
    }

    card.addEventListener("click", () => showDetail(item));
    card.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        showDetail(item);
      }
    });

    results.appendChild(card);

    if (index === 0) {
      showDetail(item);
    }
  });
}

function clearDetail() {
  detailContent.classList.add("hidden");
  detailEmpty.classList.remove("hidden");
}

function showDetail(item) {
  const product = item.product;
  const affordability = item.affordability;

  detailEmpty.classList.add("hidden");
  detailContent.classList.remove("hidden");

  detailFields.title.textContent = product.name || "상품명 확인";
  detailFields.institution.textContent = product.institution || "확인 필요";
  detailFields.limit.textContent = product.limitText || formatWon(product.limitAmount) || "확인 필요";
  detailFields.rate.textContent = [product.rateType, product.rateText].filter(Boolean).join(" · ") || "확인 필요";
  detailFields.purpose.textContent = product.purpose || "확인 필요";
  detailFields.period.textContent = product.periodText || "확인 필요";
  detailFields.target.textContent = product.target || "확인 필요";
  detailFields.region.textContent = product.region || "확인 필요";

  if (affordability) {
    detailFields.monthlyPayment.textContent = formatWon(affordability.estimatedMonthlyPayment);
    detailFields.dsr.textContent = `${percentText(affordability.dsr)} / 기준 ${percentText(affordability.dsrLimit)}`;
    detailFields.calculationRate.textContent = `${percentText(affordability.calculationRate)} 적용`;
    detailFields.calculationTerm.textContent = `${affordability.calculationTermYears || "확인 필요"}년`;

    if (affordability.mortgageEvaluationUsed) {
      detailFields.dti.textContent = `${percentText(affordability.dti)} / 기준 ${percentText(affordability.dtiLimit)}`;
      detailFields.ltv.textContent = `${percentText(affordability.ltv)} / 기준 ${percentText(affordability.ltvLimit)}`;
    } else {
      detailFields.dti.textContent = "비담보대출 계산 제외";
      detailFields.ltv.textContent = "비담보대출 계산 제외";
    }

    detailFields.possibleLimit.textContent = formatWon(affordability.finalPossibleLoanAmount);
  } else {
    detailFields.monthlyPayment.textContent = "확인 필요";
    detailFields.dsr.textContent = "확인 필요";
    detailFields.dti.textContent = "확인 필요";
    detailFields.ltv.textContent = "확인 필요";
    detailFields.calculationRate.textContent = "확인 필요";
    detailFields.calculationTerm.textContent = "확인 필요";
    detailFields.possibleLimit.textContent = "확인 필요";
  }

  detailFields.summary.textContent = product.summary || "상세 설명이 제공되지 않았습니다.";
}

function setLoading(isLoading) {
  const button = form.querySelector("button");
  button.disabled = isLoading;
  button.querySelector("span:last-child").textContent = isLoading ? "조회 중" : "추천 조회";
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
  if (!value) {
    return "";
  }

  if (value >= 100000000) {
    return `${Math.round(value / 10000000) / 10}억원`;
  }

  return `${Math.round(value / 10000).toLocaleString("ko-KR")}만원`;
}

function escapeHtml(value) {
  return String(value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
