package namoo.zinex.stock.batch.dto;

///  DB에 업서트할 정제된 DTO.
public record StockUpsert(
        String isin,
        String symbol,
        String name,
        String market,
        String securityType,
        String stockType
){
    public static StockUpsert of(String isin,String symbol, String name, String market, String securityType, String stockType){
        return new StockUpsert(isin, symbol, name, market, securityType, stockType);
    }
}