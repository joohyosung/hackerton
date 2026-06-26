# 대출알리오

공공데이터포털 서민대출상품 API를 실시간으로 조회하고, 사용자의 연령, 연소득, 신용등급, 희망 대출금액, 거주 지역 등을 바탕으로 적합한 상품을 추천하는 Spring Boot 웹사이트입니다.

## 주요 기능

- 공공데이터포털 API 기반 대출 상품 조회
- 사용자 조건 기반 대출 상품 추천
- 추천 사유와 적합도 점수 표시
- 상품별 대출한도, 금리, 용도, 기간, 취급기관, 지원대상 상세 조회
- DSR, DTI, LTV, 예상 월상환액 계산
- 검색 결과 10개 단위 페이지네이션과 상품 상세 모달

## 기술 스택

- Java 11
- Spring Boot 2.7
- Vanilla HTML/CSS/JavaScript
- 공공데이터포털 XML API

## 실행 방법

공공데이터포털 API 키를 설정한 뒤 실행합니다.

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

브라우저에서 `http://localhost:8080`으로 접속합니다.

## 공공데이터 API 설정

API 인증키는 코드에 넣지 않고 환경변수나 로컬 설정 파일로 관리합니다.

| 설정 | 설명 |
|---|---|
| `DATA_GO_KR_SERVICE_KEY` | 공공데이터포털 인증키 |
| `LOAN_API_ENDPOINT` | API base URL |
| `LOAN_API_PATH` | API operation path |
| `LOAN_API_PAGE_SIZE` | 한 번에 조회할 상품 수 |

현재 서비스는 대출 상품 데이터를 오직 공공데이터포털 API 응답에서만 가져옵니다. 샘플 데이터, 로컬 JSON 데이터셋, 데이터 파이프라인 산출물은 사용하지 않습니다.

## 데이터 조회 흐름

```text
사용자 검색 요청
      │
      ▼
공공데이터포털 API 호출
      │
      ▼
XML 응답 파싱
      │
      ▼
추천 점수 및 DSR/DTI/LTV 계산
      │
      ▼
검색 결과 리스트 출력
```

API 키가 없거나 API 호출이 실패하면 상품 목록은 비어 있는 결과로 처리됩니다.

## 추천 알고리즘

대출알리오는 단순 조건 비교가 아니라 DSR, DTI, LTV를 함께 계산해 대출 가능성을 평가합니다.

### 계산 항목

- DSR = 모든 대출의 연간 원리금 상환액 / 연소득 × 100
- DTI = 신규 주택담보대출 연간 원리금 + 기존 기타대출 연간 이자 / 연소득 × 100
- LTV = 신청 대출금액 + 기존 주담대 + 선순위 보증금 / 담보가치 × 100

### 점수 구조

| 항목 | 배점 |
|---|---:|
| 상품 조건 적합성 | 35점 |
| 상환 가능성 | 35점 |
| 한도/금리 매력도 | 20점 |
| 정보 완성도 | 10점 |

DSR, DTI, LTV 기준값은 `application.properties`에서 설정할 수 있습니다.

## 금융 계산 안내

대출알리오에서 제공하는 DSR, DTI, LTV 및 예상 월상환액 계산은 사용자가 입력한 정보를 기반으로 한 참고용 계산입니다.

실제 대출 가능 여부, 한도, 금리, 상환 조건은 금융기관의 심사 기준, 신용평가 결과, 담보 평가, 소득 증빙, 보증기관 심사 등에 따라 달라질 수 있습니다.

따라서 본 서비스의 추천 결과는 금융상품 선택을 돕기 위한 참고 정보이며, 최종 대출 신청 전에는 반드시 해당 금융기관 또는 공공기관의 공식 안내를 확인해야 합니다.
