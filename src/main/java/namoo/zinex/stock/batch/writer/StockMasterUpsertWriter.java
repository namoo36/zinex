package namoo.zinex.stock.batch.writer;

import namoo.zinex.stock.batch.dto.StockUpsert;
import namoo.zinex.stock.domain.Stocks;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

///  Processor에서 정제된 StockUpsert들을 DB에 업서트(INSERT or UPDATE)하는 Writer
public class StockMasterUpsertWriter implements ItemWriter<StockUpsert> {

  // NamedParameterJdbcTemplate 사용 SQL 쿼리 실행
  private final NamedParameterJdbcTemplate jdbc;

  // 이미 조회한 카테고리를 캐싱 -> 중복 SELECT/INSERT 피함
  private final Map<String, Long> categoryCache = new ConcurrentHashMap<>();

  public StockMasterUpsertWriter(DataSource dataSource) {
    this.jdbc = new NamedParameterJdbcTemplate(dataSource);
  }

  @Override
  public void write(@NonNull Chunk<? extends StockUpsert> chunk) throws Exception {
    for (StockUpsert agg : chunk) {

      // stock 테이블에 업데이트
      upsertStock(agg);

      // stock id 조회
      Long stockId = jdbc.queryForObject("""
        SELECT id FROM stocks WHERE isin = :isin
        """, Map.of("isin", agg.isin()), Long.class);

      // Category 처리 후 매핑
      linkCategory(stockId, "MARKET", agg.market());
      linkCategory(stockId, "SECURITY_TYPE", agg.securityType());
      linkCategory(stockId, "STOCK_TYPE", agg.stockType());
    }
  }

  ///  stock 테이블 삽입
  private void upsertStock(StockUpsert agg) {
    jdbc.update("""
        INSERT INTO stocks(isin, symbol, name, market, status, updated_at)
        VALUES (:isin, :symbol, :name, :market, CURRENT_TIMESTAMP)
        ON DUPLICATE KEY UPDATE
          name = VALUES(name),
          market = VALUES(market),
          updated_at = CURRENT_TIMESTAMP
        """, Map.of(
            "isin", agg.isin(),
            "symbol", agg.symbol(),
            "name", agg.name(),
            "market", agg.market()
    ));
  }

  ///  stock_category 테이블 매핑
  private void linkCategory(Long stockId, String type, String name) {
    // 이름이 없으면 무시
    if (name == null || name.isBlank()) return;

    // type | name 으로 key를 넣어둠
    Long categoryId = categoryCache.computeIfAbsent(type + "|" + name, k -> findOrCreateCategory(type, name));

    // stock_category 매핑
    jdbc.update("""
        INSERT INTO stock_category(stock_id, category_id)
        VALUES (:stockId, :categoryId)
        ON DUPLICATE KEY UPDATE stock_id = stock_id
        """, Map.of(
            "stockId", stockId,
            "categoryId", categoryId
    ));
  }

  /// category 테이블 삽입
  private Long findOrCreateCategory(String type, String name) {
    try {
      // insert 시도
      jdbc.update("""
          INSERT INTO categories(type, name)
          VALUES (:type, :name)
          """, Map.of(
              "type", type,
              "name", name
      ));
    } catch (DuplicateKeyException ignored) {
      // 이미 존재하면 무시
    }

    // id 반환
    return jdbc.queryForObject("""
        SELECT id FROM categories WHERE type = :type AND name = :name
        """, Map.of(
            "type", type,
            "name", name
    ), Long.class);
  }

}

