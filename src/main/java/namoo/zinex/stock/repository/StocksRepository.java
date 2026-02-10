package namoo.zinex.stock.repository;

import java.util.Optional;
import namoo.zinex.stock.domain.Stocks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StocksRepository extends JpaRepository<Stocks, Long> {
    Optional<Stocks> findBySymbolAndMarket(String symbol, String market);
}

