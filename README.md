# 대출알리오

서민대출상품을 한눈에 비교하고, 사용자의 연령, 연소득, 신용등급, 희망 대출금액, 거주 지역을 바탕으로 적합한 상품을 추천하는 Spring Boot 웹사이트입니다.

## 주요 기능

- 사용자 조건 기반 대출 상품 추천
- 추천 사유와 적합도 점수 표시
- 상품별 대출한도, 금리, 용도, 기간, 취급기관, 지원대상 상세 조회
- 공공데이터 포털 API 연동 준비
- API 키가 없거나 API 호출이 실패한 경우 개발용 샘플 데이터로 화면 확인

## 기술 스택

- Java 8
- Spring Boot 2.7
- Vanilla HTML/CSS/JavaScript
- 공공데이터 포털 XML API

## 실행 방법

Maven이 설치된 환경에서 실행합니다.

```bash
mvn spring-boot:run
```

브라우저에서 `http://localhost:8080`으로 접속합니다.

## 공공데이터 API 키 설정

API 인증키는 코드에 넣지 않고 환경변수로 관리합니다.

```bash
set DATA_GO_KR_SERVICE_KEY=발급받은_API_키
set LOAN_API_ENDPOINT=https://apis.data.go.kr/B553701/LoanProductSearchingInfo
set LOAN_API_PATH=/LoanProductSearchingInfo/getLoanProductSearchingInfo
mvn spring-boot:run
```

PowerShell에서는 다음처럼 설정할 수 있습니다.

```powershell
$env:DATA_GO_KR_SERVICE_KEY="발급받은_API_키"
$env:LOAN_API_ENDPOINT="https://apis.data.go.kr/B553701/LoanProductSearchingInfo"
$env:LOAN_API_PATH="/LoanProductSearchingInfo/getLoanProductSearchingInfo"
mvn spring-boot:run
```

API base URL이나 operation path가 변경되면 `LOAN_API_ENDPOINT`, `LOAN_API_PATH`를 각각 바꾸면 됩니다.

## 데이터 CI/CD

데이터 파이프라인은 GitHub Actions에서 빌드 검증, 샘플 XML 계약 검증, 실제 API 수집, JSON 정규화, artifact 저장을 수행합니다.

자세한 운영 방법은 [docs/data-pipeline.md](docs/data-pipeline.md)를 확인하세요.

## 추천 알고리즘

대출알리오는 단순 조건 비교가 아니라 DSR, DTI, LTV를 함께 계산해 대출 가능성을 평가합니다.

### 계산 항목

- DSR = 모든 대출의 연간 원리금 상환액 / 연소득 × 100
- DTI = 신규 주택담보대출 연간 원리금 + 기존 기타대출 연간 이자 / 연소득 × 100
- LTV = 신청 대출금액 + 기존 주담대 + 선순위 보증금 / 담보가치 × 100

### 점수 구조

| 항목 | 배점 |
|---|---:|
| 대출 가능성 | 45점 |
| 상환 안정성 | 25점 |
| 상품 적합성 | 20점 |
| 금리/한도 매력도 | 10점 |

DSR, DTI, LTV 기준값은 `application.properties`에서 설정할 수 있습니다.