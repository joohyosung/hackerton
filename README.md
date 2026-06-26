# 대출알리오

공공데이터포털 서민대출상품 API를 실시간으로 조회하고, 사용자의 핵심 조건을 공공데이터 상품 필드와 비교해 맞춤형 대출 상품을 추천하는 Spring Boot 웹사이트입니다.

## 주요 기능

- 공공데이터포털 API 기반 대출 상품 조회
- 필수 입력값 5개 기반 조건 검색
  - 나이
  - 연소득
  - 희망 대출금액
  - 거주 지역
  - 대출 용도
- 선택 입력값 기반 추가 매칭
  - 신용등급
  - 사용자 유형
- 추천 사유, 확인 필요 메시지, 적합도 점수 표시
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
| `LOAN_POLICY_MINIMUM_RECOMMENDATION_SCORE` | 추천 결과에 노출할 최소 점수 |

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
공공데이터 필드 기반 조건 매칭 점수 계산
      │
      ▼
검색 결과 리스트 출력
```

API 키가 없거나 API 호출이 실패하면 상품 목록은 비어 있는 결과로 처리됩니다.

## 추천 알고리즘

추천 점수는 공공데이터 API에서 받은 상품 필드와 사용자 입력 조건의 일치도를 기준으로 계산합니다.

| 항목 | 배점 |
|---|---:|
| 한도 적합도 | 25점 |
| 용도 적합도 | 25점 |
| 대상 적합도 | 20점 |
| 지역 적합도 | 15점 |
| 조건 적합도 | 10점 |
| 금리 매력도 | 5점 |

주요 활용 필드는 `lnLmt`, `irt`, `usge`, `trgt`, `tgtFltr`, `suprTgtDtlCond`, `rsdAreaPamtEqltIstm`, `age`, `incm`, `crdtSc` 계열 필드입니다.

## 안내

대출알리오의 추천 결과는 금융상품 선택을 돕기 위한 참고 정보입니다. 실제 대출 가능 여부, 한도, 금리, 상환 조건은 금융기관의 심사 기준과 공공기관의 공식 안내에 따라 달라질 수 있습니다.
