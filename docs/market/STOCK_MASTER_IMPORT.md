## 종목 마스터 가져오기(초기 방식: CSV 업로드)

외부 데이터 소스를 아직 확정하지 않은 상태에서도 개발을 진행할 수 있도록, 서버에 CSV를 업로드해서 `stocks` 테이블을 채우는 방식을 제공합니다.

### 왜 이 방식부터?

- 크롤링/외부 API는 약관/차단/변경 이슈가 잦음
- DB 스키마/도메인/거래 로직 개발을 먼저 진행 가능
- 이후에 “외부 소스 동기화(@Scheduled / Spring Batch)”로 바꿀 때도 **업서트 로직은 그대로 재사용** 가능

---

## CSV 포맷

헤더는 있어도 되고 없어도 됩니다.

```text
symbol,name,market,status
005930,삼성전자,KOSPI,ACTIVE
000660,SK하이닉스,KOSPI,ACTIVE
```

- **symbol**: 종목 코드/티커(필수)
- **name**: 종목명(필수)
- **market**: 시장(필수, 예: KOSPI/KOSDAQ/NASDAQ…)
- **status**: `ACTIVE` 또는 `INACTIVE` (선택, 없으면 `ACTIVE`)

샘플 파일: `docs/seed/stocks.sample.csv`

### 인코딩(중요)

KRX에서 내려받은 CSV는 종종 **CP949(EUC-KR 계열)** 인코딩이라, UTF-8로 읽으면 한글이 깨져 보일 수 있습니다.

- UTF-8 파일이면: 기본 설정 그대로 업로드
- CP949 파일이면: 업로드 시 `encoding=CP949` 파라미터를 함께 전송

---

## 업로드 API

- 경로: `POST /api/admin/stocks/import`
- 형식: `multipart/form-data`
- 필드: `file`
- (선택) `encoding`: `UTF-8`(기본) / `CP949` / `EUC-KR` 등
- 권한: `ROLE_ADMIN` 필요(`SecurityConfig`의 `/api/admin/**`)

반환값은 업로드 처리 결과입니다.

- `total`: 처리한 데이터 라인 수(헤더 제외)
- `inserted`: 신규 생성 수
- `updated`: 기존 갱신 수
- `skipped`: 형식 오류/필수값 누락으로 스킵된 수

