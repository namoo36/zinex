package namoo.zinex.stock.batch.dto;


///   CSV에서 한 줄을 읽어온 원본 DTO.
public record StockCsvRow(
        String isin,
        String symbol,
        String name,
        String market,
        String securityType,
        String stockType
){
    public static StockCsvRow of( String isin, String symbol, String name, String market, String securityType, String stockType){
        return new StockCsvRow(isin, symbol, name, market, securityType, stockType);
    }
}

