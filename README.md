# zinex

고성능/확장 가능한 **주식 거래소(Stock Exchange) 백엔드** 프로젝트입니다.  


## 기술 스택 (MVP 목표)

- **Java 21**
- **Spring Boot 3.x**
- **MySQL 8.0+**
- **Redis** (분산락)


## 데이터베이스 스키마 (MVP)

MySQL DDL은 아래 파일에 정리되어 있습니다.

- `docs/schema.mysql.sql`

포함 테이블(요약):
- `users`, `accounts`
- `stocks`
- `orders`, `accounts_hold`
- `fills`, `holdings`
- `trade_logs` (MVP는 MySQL 저장, 이후 Phase에서 MongoDB로 이관 가능)

## 문서

- `docs/PRD.md`: 제품 요구사항/로드맵(Phase 0~)
- `docs/requirements.md`: MVP 기능/비기능 요구사항


### 실행

```bash
./gradlew bootRun
```

Windows(PowerShell):

```powershell
.\gradlew.bat bootRun
```


## MVP 기능 범위 (요약)

- **회원/인증**: 회원가입, 로그인(JWT), 로그아웃(토큰 무효화 정책 포함)
- **계좌/예수금**: 입금, 예수금/Hold/주문 가능 금액 조회, 트랜잭션 기반 정합성
- **종목**: CRUD, 목록(페이징/정렬)
- **주문**: 매수/매도 생성, 검증(주문 가능 금액/보유 수량), 미체결 취소, Hold 반영/복구
- **체결**: 단순 매칭/가상 체결 가능(최소), 체결 시 예수금·Hold·잔고 갱신
- **로그**: 핵심 이벤트(입금/주문/취소/체결 성공/실패) 기록

