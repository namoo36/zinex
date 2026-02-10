## Spring Batch로 종목 마스터(CSV) 적재

`stocks` 테이블에 CSV를 업서트(없으면 insert / 있으면 update)하는 Batch Job을 제공합니다.

### Job 개요

- Job name: `stockMasterImportJob`
- Step: `stockMasterImportStep`
- 입력: CSV 파일
- 출력: `stocks` 테이블 업서트
- 중복 기준: `(symbol, market)` 유니크(`uq_stocks_symbol_market`)

---

## 실행 방법

Spring Batch Job은 기본적으로 애플리케이션 시작 시 자동 실행하지 않도록 설정되어 있습니다.

```yaml
spring:
  batch:
    job:
      enabled: false
```

### 1) `bootRun`으로 실행

예시 1) 일반 포맷(UTF-8)

```bash
./gradlew bootRun --args="--spring.batch.job.name=stockMasterImportJob --filePath=docs/seed/stocks.sample.csv --format=simple --encoding=UTF-8 --hasHeader=true"
```

예시 2) KRX 포맷(CP949)

```bash
./gradlew bootRun --args="--spring.batch.job.name=stockMasterImportJob --filePath=docs/seed/krx/stocks.krx.csv --format=krx --encoding=CP949 --hasHeader=true"
```

---

## 지원 포맷

### simple (기본)

```text
symbol,name,market,status
005930,삼성전자,KOSPI,ACTIVE
```

- status는 선택(없으면 ACTIVE)

### krx

KRX에서 내려받는 CSV에서 필요한 컬럼만 추출합니다.

- symbol: 단축코드(예상 index 1)
- name: 한글 종목명(예상 index 2)
- market: 시장구분(예상 index 6)

> KRX CSV 컬럼 순서가 다르면 `StockMasterImportBatchConfig.krxTokenizer()`의 `includedFields` 인덱스를 조정하면 됩니다.

