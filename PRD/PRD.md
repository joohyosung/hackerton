# PRD 지침
---
### 프로젝트 개요
1. 서비스 이름
- 대출알리오
2. 존재 이유
- 다양한 서민대출상품의 핵심 정보를 한눈에 비교하고 조회
- 금융상품명, 대출한도, 금리구분, 대출 용도, 총 대출기간, 취급기관 등 주요 항목별 정보를 제공
- 사용자가 자신의 필요에 맞는 서민대출상품을 쉽게 탐색하고 비교
- 특히 정부기관, 정책금융기관, 지방자치단체 등 다양한 기관에서 제공하는 금융상품을 자금용도, 지원대상 등 수요자가 원하는 기준에 따라 검색
3. 주요 고객
- 대출을 필요로 하나 자신에게 맞는 대출이 무엇인지 모르는 사용자
4. 핵심 기능
- 연소득, 연령, 신용등급, 대출 금액, 거주 지역 등을 입력받아 해당하는 대출 상품을 추천
5. 기술스텍
- 언어: JAVA
- 프레임워크: SPRING
- 데이터: 공공데이터 포털 API 활용
- 데이터 포맷: XML
- End Point: https://apis.data.go.kr/B553701/LoanProductSearchingInfo
- api 인증키: 다른 파일로 관리
6. 사용자 흐름
- 메인페이지 랜딩 -> 검색창에서 사용자의 정보(연령, 연소득, 신용등급, 대출 금액, 거주 지역)를 입력 -> 해당 정보에 맞는 대출 상품 리스트를 출력 -> 상품 리스트 내 대출 상품명 클릭 시 상세 내용 출력

---
### 변경 관리 규칙
- 앞으로 기능 수정, UI 변경, API 변경, 데이터 파이프라인 변경, 운영 설정 추가 등 프로젝트에 영향을 주는 내용은 이 PRD.md 파일에 변경 의도와 적용 내용을 함께 정리한다.
- 구현 상세는 코드와 문서에 둔다. PRD에는 사용자 경험, 제품 방향, 선택 근거, 운영상 중요한 결정사항을 기록한다.

---
### 디자인 적용 기록
1. 참고 디자인 레포
- https://github.com/VoltAgent/awesome-design-md.git
- 레포 확인 결과 회사별 실제 html 파일이 아니라 DESIGN.md/README.md 형태의 디자인 시스템 분석 문서로 구성되어 있었다.

2. 검토 후보
- Wise: 금융 서비스에 가까운 친근한 핀테크 톤. sage 배경, 강한 ink 텍스트, lime CTA, 둥근 입력/카드가 특징이다.
- Linear: 정교하고 밀도 높은 제품 UI에 적합하지만, 어두운 개발자 도구 톤이라 일반 대출 추천 서비스에는 진입 장벽이 높다.
- Mastercard: 금융 브랜드 신뢰감은 있으나 원형 이미지, 오비트 장식, editorial hero 중심이라 현재 검색/비교 도구 화면에는 과하다.

3. 선택 디자인
- Wise-inspired 디자인을 선택했다.
- 선택 이유: 대출알리오는 금융 상품을 비교하는 서비스이므로 사용자에게 신뢰감과 명확함이 필요하다. Wise 디자인의 밝은 핀테크 톤은 정책금융/서민대출 서비스에 어울리고, 입력 폼과 추천 결과 카드 중심의 현재 화면 구조를 크게 바꾸지 않고도 적용 가능하다.

4. 적용 방향
- 배경을 sage 계열 canvas로 바꾸어 차갑지 않은 금융 서비스 분위기를 만든다.
- primary CTA는 lime green을 사용해 추천 조회 행동을 선명하게 만든다.
- 텍스트는 near-black ink를 사용해 가독성과 신뢰감을 높인다.
- 기존의 검색 패널, 결과 패널, 상세 패널 3열 구조는 유지한다.
- 랜딩 페이지식 장식보다 실제 사용 화면을 첫 화면으로 유지한다.
- 결과 카드, 점수 배지, 상세 요약은 부드러운 fintech card 스타일로 정리한다.

### DSR·DTI·LTV 기반 대출 추천 로직 개선

1. 개선 배경

기존 대출알리오의 추천 로직은 사용자의 연령, 연소득, 신용등급, 희망 대출금액, 거주 지역, 대출 용도 등을 기준으로 상품 조건과의 일치 여부를 점수화하는 방식이었다.

이번 개선에서는 실제 대출 심사에서 활용되는 주요 금융 지표인 DSR, DTI, LTV 계산식을 추천 로직에 반영한다. 이를 통해 단순 조건 매칭이 아니라 사용자의 상환 가능성, 담보 여력, 실제 대출 가능성을 함께 고려하는 추천 결과를 제공한다.

2. 개선 목표

| 목표 | 설명 |
|---|---|
| 실제 대출 가능성 반영 | DSR, DTI, LTV를 계산하여 대출 가능성을 평가한다. |
| 상환 부담 시각화 | 예상 월상환액과 DSR 비율을 사용자에게 보여준다. |
| 담보대출 판단 강화 | 담보가치, 기존 주담대, 선순위 보증금을 반영해 LTV를 계산한다. |
| 추천 신뢰도 향상 | 추천 사유와 기준 초과 경고를 함께 제공한다. |
| 정책 기준 유연화 | DSR, DTI, LTV 기준값을 설정 파일에서 관리한다. |

---

3. 추가되는 사용자 입력값

기존 입력값은 유지한다.

### 기존 입력값

| 입력값 | 설명 |
|---|---|
| 연령 | 상품의 연령 조건 판단 |
| 연소득 | 소득 조건 및 DSR, DTI 계산 |
| 신용등급 | 상품의 신용 조건 판단 |
| 희망 대출금액 | 상품 한도 및 대출 가능성 판단 |
| 거주 지역 | 지역 제한 상품 판단 |
| 대출 용도 | 생계, 주거, 창업, 대환 등 상품 목적 판단 |

### 신규 입력값

| 구분 | 입력값 | 필드명 | 설명 |
|---|---|---|---|
| 상환조건 | 상환기간 | `desiredLoanTermYears` | 월상환액 계산에 사용 |
| 상환조건 | 예상 금리 | `expectedInterestRate` | 월상환액 계산에 사용 |
| 기존부채 | 기존 대출 월 상환액 합계 | `existingMonthlyDebtPayment` | DSR 계산에 사용 |
| 기존부채 | 기존 기타대출 연간 이자액 | `existingAnnualDebtInterest` | DTI 계산에 사용 |
| 담보여부 | 담보대출 여부 | `mortgageLoan` | DTI, LTV 계산 여부 판단 |
| 담보정보 | 담보가치 | `collateralValue` | LTV 계산의 분모 |
| 담보정보 | 기존 주택담보대출 잔액 | `existingMortgageBalance` | 실질 LTV 계산에 반영 |
| 담보정보 | 선순위 보증금 | `seniorDeposit` | 실질 LTV 계산에 반영 |
| 정책조건 | 생애최초 여부 | `firstHomeBuyer` | LTV 기준 완화 여부 판단 |

---

 4. 핵심 계산식

- 공공데이터 API item에서 제공하는 금리, 대출한도, 상환기간, 상환방법, 연령, 소득, 신용등급, 주택 조건을 활용하고, 사용자가 입력한 연소득, 희망 대출금액, 기존 부채, 담보 정보를 결합하여 실제 대출 가능성에 가까운 추천 점수를 계산한다.

1) 예상 월상환액 계산
- 상품별 월상환액은 공공데이터 API item의 금리와 상환기간을 우선 사용하여 계산한다.

- 계산용 금리 우선순위: item.irt > 사용자 입력 예상 금리 > 기본값 5%
- 계산용 상환기간 우선순위: item.maxRdptTrm > item.maxTotLnTrm > 사용자 입력 상환기간 > 기본값 5년

- 원리금균등상환 기준 계산식
```text
월상환액 = 대출원금 × 월이자율 × (1 + 월이자율)^상환개월수 / ((1 + 월이자율)^상환개월수 - 1)
```
```text
월이자율 = 연이자율 / 12
상환개월수 = 상환기간년수 × 12
연간 원리금상환액 = 월상환액 × 12
```

- 활용 필드
| 구분 | 필드 |
|---|---|
| API item | `irt`, `maxRdptTrm`, `maxTotLnTrm`, `rdptMthd` |
| 사용자 입력 | `loanAmount`, `expectedInterestRate`, `desiredLoanTermYears` |

---

2) DSR 계산
- DSR은 사용자의 연소득 대비 전체 대출의 연간 원리금 상환 부담을 계산한다.
```text
DSR = (기존 대출 월상환액 × 12 + 신규 대출 예상 월상환액 × 12) ÷ 연소득 × 100
```

- 활용 필드
| 구분 | 필드 |
|---|---|
| API item | `irt`, `maxRdptTrm`, `maxTotLnTrm` |
| 사용자 입력 | `annualIncome`, `loanAmount`, `existingMonthlyDebtPayment` |

- 판단 기준
```text
DSR ≤ loan.policy.dsr-limit → DSR 기준 통과
```

3) DTI 계산
- DTI는 주택담보대출 또는 주거성 상품일 때, 연소득 대비 주택담보대출 중심의 상환 부담을 계산한다.

```text
DTI = (신규 대출 연간 원리금상환액 + 기존 기타대출 연간 이자액) ÷ 연소득 × 100
```
- DTI 계산 적용 대상
```text
mortgageLoan = true 또는
상품 item이 주거·담보성 상품으로 판단되는 경우
```
- 주거·담보성 상품 판단에 활용하는 API 필드
| API 필드 | 활용 |
|---|---|
| `usge` | 주거, 전세, 월세 등 용도 판단 |
| `prdCtg` | 주거자금, 담보성 상품 판단 |
| `lnTgtHous` | 대출 대상 주택 조건 확인 |
| `housAr` | 주택 면적 조건 확인 |
| `housHoldCnt` | 주택 보유 수 조건 확인 |
- 판단 기준
```text
DTI ≤ loan.policy.dti-limit → DTI 기준 통과
```

4) LTV 계산
- LTV는 담보가치 대비 대출 비율을 계산한다.

```text
LTV = (신청 대출금액 + 기존 주택담보대출 잔액 + 선순위 보증금) ÷ 담보가치 × 100
```
- 활용 필드
| 구분 | 필드 |
|---|---|
| API item | `lnTgtHous`, `housAr`, `housHoldCnt`, `usge`, `prdCtg` |
| 사용자 입력 | `loanAmount`, `collateralValue`, `existingMortgageBalance`, `seniorDeposit`, `firstHomeBuyer` |
- 판단 기준
```text
일반 사용자: LTV ≤ loan.policy.ltv-limit

생애최초 사용자: LTV ≤ loan.policy.first-home-buyer-ltv-limit
```

5) 상품 조건 적합성 계산
- 공공데이터 API item의 조건 필드를 활용하여 사용자 조건과 상품 조건을 비교한다.

| 평가 항목 | 활용 API 필드 | 사용자 입력 |
|---|---|---|
| 연령 조건 | `age`, `age_39Blw`, `age_40Abnml`, `age_60Abnml` | `age` |
| 소득 조건 | `anin`, `incm`, `incmCnd`, `incmCndY`, `incmCndN` | `annualIncome` |
| 신용등급 조건 | `crdtSc`, `crdtSc_1`~`crdtSc_9`, `crdtSc_0`, `crdtSc_1_5`, `crdtSc_6_0` | `creditGrade` |
| 대출한도 조건 | `lnLmt`, `lnLmt_1000Abnml`, `lnLmt_2000Abnml`, `lnLmt_3000Abnml`, `lnLmt_5000Abnml`, `lnLmt_10000Abnml` | `loanAmount` |
| 용도 조건 | `usge`, `prdCtg` | `purpose` |
| 지역 조건 | `rsdArea`, `rsdAreaPamtEqltIstm` | `region` |
| 주택 조건 | `lnTgtHous`, `housAr`, `housHoldCnt` | `houseArea`, `houseCount` |

6) 최종 추천 점수 계산
- 추천 점수는 총 100점 기준으로 계산한다.
| 평가 영역 | 배점 | 계산 방식 |
|---|---:|---|
| 상품 조건 적합성 | 35점 | 연령, 소득, 신용등급, 용도, 지역 조건 일치 여부 |
| 상환 가능성 | 35점 | DSR, DTI, LTV 기준 통과 여부 |
| 한도·금리 매력도 | 20점 | 희망금액이 상품 한도 이내인지, 금리 수준이 낮은지 |
| 정보 완성도 | 10점 | 한도, 금리, 상환방법, 신청방법, 연락처, 사이트 정보 존재 여부 |

7) 최종 추천 조건
```text
총점 ≥ loan.policy.minimum-recommendation-score
AND
DSR 기준 통과
AND
담보·주거성 상품인 경우 DTI, LTV 기준 통과
AND
연령, 소득, 신용등급 등 필수 상품 조건 통과
```

8) 추천 결과 표시 항목
- 사용자에게는 단순 점수뿐 아니라 계산 근거를 함께 제공한다.

| 표시 항목 | 설명 |
|---|---|
| 추천 점수 | 상품 조건과 상환 가능성을 종합한 점수 |
| 예상 월상환액 | 상품 금리와 상환기간 기준 계산값 |
| DSR | 전체 부채 기준 상환 부담 |
| DTI | 주거·담보성 상품일 때 소득 대비 상환 부담 |
| LTV | 담보가치 대비 대출 비율 |
| 계산 기준 금리 | API item의 `irt` 또는 fallback 금리 |
| 계산 기준 기간 | API item의 `maxRdptTrm`, `maxTotLnTrm` 또는 fallback 기간 |
| 계산상 가능 한도 | DSR, DTI, LTV 기준으로 추정한 가능 금액 |
| 추천 사유 | 조건을 충족한 이유 |
| 경고 메시지 | 기준 초과 또는 상품 조건 불일치 사유 |

---

### test01 브랜치 검증 및 수정 기록
- 검증 일시: 2026-06-24
- 검증 대상: `test01` 브랜치의 추천 계산식, 상환 가능성 분석, 프론트 결과 표시
- 검증 결과:
  - `mvn clean test` 통과
  - `node --check src/main/resources/static/app.js` 통과
  - 샘플 데이터 파이프라인 실행 통과
  - Spring Boot 서버 헬스체크 `ok` 확인
  - 비담보 생계자금 조건 추천 API가 3건을 반환하고 DSR, 월상환액, 가능 한도 계산값을 정상 반환함
- 수정 내용:
  - 추천 상세 패널에서 계산 기준 금리와 계산 기준 기간이 비어 보이지 않도록 프론트 표시 로직을 보완함
  - 주택보유수 조건에 `무주택, 1주택`처럼 복수 허용 조건이 포함된 경우 더 넓은 허용 조건부터 판단하도록 필터 로직을 수정함
- 추가 확인 필요:
  - 현재 샘플 데이터 기준으로 고소득, 고액 주거담보 조건은 결과가 0건일 수 있음
  - 실제 공공데이터 API 키 연동 후 실데이터 기준으로 담보대출 추천 결과를 추가 검증해야 함

---

### GitHub Actions 컴파일 오류 수정 기록
- 수정 일시: 2026-06-24
- 오류 내용:
  - Linux CI에서 `LoanPolicyproperties.java` 파일명과 `LoanPolicyProperties` public class명이 일치하지 않아 `mvn -B test` 컴파일이 실패함
- 원인:
  - Windows 로컬 파일시스템은 대소문자 차이를 느슨하게 처리하지만, GitHub Actions Linux 러너는 Java 파일명의 대소문자를 엄격하게 비교함
- 수정 내용:
  - Git 인덱스의 파일명을 `LoanPolicyproperties.java`에서 `LoanPolicyProperties.java`로 정정함
- 검증 방법:
  - CI와 동일하게 `mvn -B test`를 로컬에서 실행하여 컴파일 및 테스트 통과 여부를 확인함

---

### main 브랜치 병합 후 파일명 추적 정리
- 수정 일시: 2026-06-24
- 상황:
  - `test01`을 `main`에 병합한 뒤 Git 인덱스에 `LoanPolicyProperties.java`와 `LoanPolicyproperties.java`가 동시에 추적되는 상태가 확인됨
- 수정 내용:
  - 예전 대소문자 파일명인 `LoanPolicyproperties.java` 추적 엔트리를 제거하고 `LoanPolicyProperties.java`만 남김
- 검증 방법:
  - `git ls-files`로 `LoanPolicyProperties.java`만 추적되는지 확인함
  - `mvn -B clean test`로 깨끗한 재컴파일과 테스트 통과 여부를 확인함
