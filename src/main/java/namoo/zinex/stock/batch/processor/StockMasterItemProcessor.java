package namoo.zinex.stock.batch.processor;

import namoo.zinex.stock.batch.dto.StockCsvRow;
import namoo.zinex.stock.batch.dto.StockUpsert;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.item.ItemProcessor;

///  CSV에서 읽은 원본 행(StockCsvRow)을 DB에 upsert할 형태(StockUpsert)로 정제·변환
public class StockMasterItemProcessor implements ItemProcessor<StockCsvRow, StockUpsert> {

  @Override
  public StockUpsert process(@NonNull StockCsvRow row) {

    // null 처리
    String isin = stripBom(trim(row.isin()));
    String symbol = trim(row.symbol());
    String name = trim(row.name());
    String market = trim(row.market()).toUpperCase();
    String securityType = trim(row.securityType());
    String stockType = trim(row.stockType());

    if (symbol.isBlank() || name.isBlank() || market.isBlank() || isin.isBlank()) {
      return null;
    }

    return StockUpsert.of(isin, symbol, name, market, securityType, stockType);
  }

  private static String trim(String s) {
    return s == null ? "" : s.trim();
  }

  private static String stripBom(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.charAt(0) == '\uFEFF' ? s.substring(1) : s;
  }
}

