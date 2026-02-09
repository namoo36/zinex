package namoo.zinex.stockcategory.repository;

import namoo.zinex.stockcategory.domain.StockCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockCategoryRepository extends JpaRepository<StockCategory, Long> {}

