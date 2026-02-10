package namoo.zinex.stock.batch;

import javax.sql.DataSource;
import namoo.zinex.stock.batch.dto.StockCsvRow;
import namoo.zinex.stock.batch.dto.StockUpsert;
import namoo.zinex.stock.batch.processor.StockMasterItemProcessor;
import namoo.zinex.stock.batch.reader.StockMasterCsvReader;
import namoo.zinex.stock.batch.writer.StockMasterUpsertWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

///  실행 시 전달받은 CSV 파일을 읽어서 종목 마스터를 트랜잭션으로 DB에 Upsert
@Configuration
@EnableBatchProcessing  // -> Spring Batch 인프라 자동 구성
public class StockMasterImportBatchConfig {

  ///  Job 정의 : 배치 실행 최상위 단위 (이름 : stockMasterImportJob)
  @Bean
  public Job stockMasterImportJob(JobRepository jobRepository, Step stockMasterImportStep) {
    return new JobBuilder("stockMasterImportJob", jobRepository).start(stockMasterImportStep).build();
  }

  ///  Step 정의 : 실제 데이터 처리 단위
  @Bean
  public Step stockMasterImportStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ItemStreamReader<StockCsvRow> stockMasterReader,
      ItemProcessor<StockCsvRow, StockUpsert> stockMasterProcessor,
      ItemWriter<StockUpsert> stockMasterWriter) {

    return new StepBuilder("stockMasterImportStep", jobRepository)
        .<StockCsvRow, StockUpsert>chunk(500, transactionManager)   // Chunk 설정 -> <입력, 출력>(사이즈, 트랜잭션 관리)
        .reader(stockMasterReader)    // Reader 연결
        .processor(stockMasterProcessor)  // Processor 연결
        .writer(stockMasterWriter)  // writer 연결
        .build();
  }

  ///  Reader Bean 등록
  @Bean
  @StepScope
  public ItemStreamReader<StockCsvRow> stockMasterReader(
      @Value("#{jobParameters['filePath']}") String filePath,
      @Value("#{jobParameters['encoding']}") String encoding,
      @Value("#{jobParameters['format']}") String format,
      @Value("#{jobParameters['hasHeader']}") String hasHeader) {

    //  Reader 구현체
    return new StockMasterCsvReader(filePath, encoding, format, hasHeader);
  }

  ///  Processor Bean 등록 : CSV Row → DB 반영용 객체 변환
  @Bean
  @StepScope
  public ItemProcessor<StockCsvRow, StockUpsert> stockMasterProcessor() {
    return new StockMasterItemProcessor();
  }

  ///  Writer 역할
  @Bean
  @StepScope
  public ItemWriter<StockUpsert> stockMasterWriter(DataSource dataSource) {
    return new StockMasterUpsertWriter(dataSource);
  }
}

