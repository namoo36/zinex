-- MySQL 8.0+ DDL (MVP)
-- Stack: Java + Spring Boot + MySQL + Redis
-- Domain: users, accounts(예수금), stocks, orders, fills, holdings, trade_logs, accounts_hold(Hold)
--
-- Source
-- - ERD Cloud에서 추출한 SQL을 기반으로 하되, MySQL에서 실행 불가능한 구문/오타는 보정했습니다.
-- - 특히 accounts_hold 테이블의 상태 컬럼은 ERD 추출본에서 `version`으로 출력되었으나 의미상 `status`로 정리합니다.
--
-- Notes
-- - 금액(원화)은 소수점 없는 정수(BIGINT, KRW)로 저장합니다.
-- - 주문 가능 금액(Available) = deposit_krw - SUM(accounts_hold.hold_krw where status='ACTIVE')
-- - 동시성 제어는 Redis 분산락(user 단위) + DB 트랜잭션(필수)로 보완합니다.

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- =============
-- users
-- =============
CREATE TABLE IF NOT EXISTS users (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email             VARCHAR(255)     NOT NULL,
  password          VARCHAR(255)     NOT NULL, -- 해시된 비밀번호(예: BCrypt)
  status            ENUM('ACTIVE','SUSPENDED','DELETED') NOT NULL DEFAULT 'ACTIVE',
  created_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============
-- accounts (예수금)
-- =============
CREATE TABLE IF NOT EXISTS accounts (
  user_id           BIGINT UNSIGNED NOT NULL,
  deposit_krw       BIGINT          NOT NULL DEFAULT 0, -- 예수금(실잔액)
  version           BIGINT          NOT NULL DEFAULT 0, -- (선택) 낙관적 락/재시도에 사용
  created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_accounts_non_negative CHECK (deposit_krw >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============
-- stocks
-- =============
CREATE TABLE IF NOT EXISTS stocks (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  symbol            VARCHAR(32)      NOT NULL,           -- 종목코드
  name              VARCHAR(255)     NOT NULL,           -- 종목명
  market            VARCHAR(32)      NOT NULL,           -- 시장구분(예: KOSPI/KOSDAQ/ETF 등)
  status            ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_stocks_symbol (symbol),
  KEY idx_stocks_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============
-- orders
-- =============
CREATE TABLE IF NOT EXISTS orders (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id               BIGINT UNSIGNED NOT NULL,
  stock_id              BIGINT UNSIGNED NOT NULL,
  side                  ENUM('BUY','SELL') NOT NULL,
  order_type            ENUM('LIMIT','MARKET') NOT NULL DEFAULT 'LIMIT',
  status                ENUM('NEW','OPEN','CANCELLED','FILLED','FAILED','EXPIRED') NOT NULL DEFAULT 'NEW',

  quantity              BIGINT          NOT NULL,                -- 주문 수량(주)
  limit_price_krw       BIGINT          NULL,                    -- 지정가(시장가면 NULL)
  filled_quantity       BIGINT          NOT NULL DEFAULT 0,
  created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cancelled_at          TIMESTAMP       NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_orders_user  FOREIGN KEY (user_id)  REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_orders_stock FOREIGN KEY (stock_id) REFERENCES stocks(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_orders_qty CHECK (quantity > 0 AND filled_quantity >= 0 AND filled_quantity <= quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_orders_user_created_at ON orders (user_id, created_at);
CREATE INDEX idx_orders_user_status     ON orders (user_id, status);
CREATE INDEX idx_orders_stock_created_at ON orders (stock_id, created_at);

-- =============
-- accounts_hold (주문 단위 Hold)
-- =============
CREATE TABLE IF NOT EXISTS accounts_hold (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id               BIGINT UNSIGNED NOT NULL,
  order_id              BIGINT UNSIGNED NOT NULL,
  hold_krw              BIGINT          NOT NULL, -- 이 주문이 홀드한 금액(원화)
  status                ENUM('ACTIVE','RELEASED') NOT NULL DEFAULT 'ACTIVE',
  reserved_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  released_at           TIMESTAMP       NULL DEFAULT NULL,
  release_reason        ENUM('CANCELLED','FILLED','FAILED','EXPIRED','ADJUSTED') NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_accounts_hold_order (order_id), -- 주문당 1 Hold
  KEY idx_accounts_hold_user_status (user_id, status),
  KEY idx_accounts_hold_user_reserved_at (user_id, reserved_at),
  CONSTRAINT fk_accounts_hold_user  FOREIGN KEY (user_id)  REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_accounts_hold_order FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_accounts_hold_amount CHECK (hold_krw > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============
-- fills (체결)
-- =============
CREATE TABLE IF NOT EXISTS fills (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id              BIGINT UNSIGNED NOT NULL,
  user_id               BIGINT UNSIGNED NOT NULL,
  stock_id              BIGINT UNSIGNED NOT NULL,
  side                  ENUM('BUY','SELL') NOT NULL,
  quantity              BIGINT          NOT NULL,
  price_krw             BIGINT          NOT NULL,                -- 체결 단가
  fee_krw               BIGINT          NOT NULL DEFAULT 0,       -- 수수료(옵션)
  executed_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_fills_order FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_fills_user  FOREIGN KEY (user_id)  REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_fills_stock FOREIGN KEY (stock_id) REFERENCES stocks(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_fills_qty CHECK (quantity > 0),
  CONSTRAINT chk_fills_money CHECK (price_krw > 0 AND fee_krw >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_fills_order_id        ON fills (order_id);
CREATE INDEX idx_fills_user_executed   ON fills (user_id, executed_at);
CREATE INDEX idx_fills_stock_executed  ON fills (stock_id, executed_at);

-- =============
-- holdings (보유 종목/평단)
-- =============
CREATE TABLE IF NOT EXISTS holdings (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id               BIGINT UNSIGNED NOT NULL,
  stock_id              BIGINT UNSIGNED NOT NULL,
  quantity              BIGINT          NOT NULL DEFAULT 0,       -- 보유 수량
  avg_price_krw         BIGINT          NOT NULL DEFAULT 0,       -- 매수 평균가(0이면 미보유)
  created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_holdings_user_stock (user_id, stock_id),
  CONSTRAINT fk_holdings_user  FOREIGN KEY (user_id)  REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_holdings_stock FOREIGN KEY (stock_id) REFERENCES stocks(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_holdings_qty CHECK (quantity >= 0),
  CONSTRAINT chk_holdings_avg CHECK (avg_price_krw >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============
-- trade_logs (체결 로그 - MVP는 MySQL 저장)
-- 확장(Phase 이후): MongoDB로 이관 가능
-- =============
CREATE TABLE IF NOT EXISTS trade_logs (
  id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id               BIGINT UNSIGNED NOT NULL,
  order_id              BIGINT UNSIGNED NULL,
  fill_id               BIGINT UNSIGNED NULL,
  event_type            VARCHAR(64)      NOT NULL,               -- 예: FILL_SUCCEEDED, FILL_FAILED, ORDER_CANCELLED, DEPOSIT 등
  payload_json          JSON             NULL,                   -- 상세(유연한 스키마)
  created_at            TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_trade_logs_user  FOREIGN KEY (user_id)  REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_trade_logs_order FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_trade_logs_fill  FOREIGN KEY (fill_id)  REFERENCES fills(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_trade_logs_user_created_at ON trade_logs (user_id, created_at);
CREATE INDEX idx_trade_logs_event_type      ON trade_logs (event_type);

