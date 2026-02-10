package namoo.zinex.stock.batch.reader;

import java.nio.charset.StandardCharsets;
import namoo.zinex.stock.batch.dto.StockCsvRow;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.core.io.FileSystemResource;

///  CSV 파일을 읽어서 StockCsvRow로 변환
public class StockMasterCsvReader implements ItemStreamReader<StockCsvRow> {

  ///  CSV Reader에 위임(delegate)
  private final FlatFileItemReader<StockCsvRow> delegate;

  ///  생성자
  public StockMasterCsvReader(String filePath, String encoding, String format, String hasHeader) {
    // format(CSV 컬럼 구조 종류) -> null인 경우는 "simple"
    String fmt = format != null ? format.trim().toLowerCase() : "simple";

    // Header -> 첫째줄이 컬럼명인지 데이터인지? -> 컬럼이 있으면 Skip 하도록
    boolean header = hasHeader == null || Boolean.parseBoolean(hasHeader);

    // Tokenizer 분기 -> 한 줄을 Column 단위로 분리
    LineTokenizer tokenizer = "krx".equals(fmt) ? krxTokenizer() : simpleTokenizer();

    // LineMapper 구성 -> 컬럼 묶음을 객체로 변환
    DefaultLineMapper<StockCsvRow> lineMapper = new DefaultLineMapper<>();
    lineMapper.setLineTokenizer(tokenizer);   // 어떻게 쪼갤지
    lineMapper.setFieldSetMapper(StockMasterCsvReader::mapToRow);  // 어떻게 객체로 만들지

    this.delegate = new FlatFileItemReaderBuilder<StockCsvRow>()
            .name("stockMasterReader")
            .resource(new FileSystemResource(filePath))   // 실제 CSV 파일
            .encoding(normalizeEncoding(encoding))
            .lineMapper(lineMapper)
            .linesToSkip(header ? 1 : 0)  // Header 스킵 여부
            .build();
  }

  ///  delegate에 위임
  @Override
  public StockCsvRow read() throws Exception {
    return delegate.read();
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    delegate.open(executionContext);
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    delegate.update(executionContext);
  }

  @Override
  public void close() throws ItemStreamException {
    delegate.close();
  }

  ///  CSV 한 줄을 → 컬럼 단위로 분해하는 인터페이스
  private static LineTokenizer simpleTokenizer() {
    // 구분자 기반 CSV pareser
    DelimitedLineTokenizer t = new DelimitedLineTokenizer();
    t.setDelimiter(",");  // 구분자 : ","
    t.setQuoteCharacter('"');   // "" 안에, 는 무시
    t.setNames("isin", "symbol", "name", "market", "securityType", "stockType");   // column 이름 지정
    return t;
  }

  ///  CSV 한 줄을 → 컬럼 단위로 분해하는 인터페이스
  private static LineTokenizer krxTokenizer() {
    DelimitedLineTokenizer t = new DelimitedLineTokenizer();
    t.setDelimiter(",");
    t.setQuoteCharacter('"');

    // 1 → 단축코드 (symbol), 2 → 한글종목명 (name), 6 → 시장구분 (market)
    t.setIncludedFields(0, 1, 2, 6, 7, 9);
    t.setNames("isin", "symbol", "name", "market", "securityType", "stockType");
    return t;
  }

  private static StockCsvRow mapToRow(FieldSet fs) {
    return StockCsvRow.of(
            fs.readString("isin"),
            fs.readString("symbol"),
            fs.readString("name"),
            fs.readString("market"),
            fs.readString("securityType"),
            fs.readString("stockType")
    );
  }

  ///  encoding 정규화
  private static String normalizeEncoding(String encoding) {
    // 기본값 : UTF-8
    if (encoding == null || encoding.isBlank()) return StandardCharsets.UTF_8.name();

    String normalized = encoding.trim().toUpperCase();
    return switch (normalized) {
      case "UTF-8", "UTF8" -> StandardCharsets.UTF_8.name();
      case "CP949", "MS949", "EUC-KR", "EUCKR" -> "MS949";
      default -> StandardCharsets.UTF_8.name();
    };
  }
}

