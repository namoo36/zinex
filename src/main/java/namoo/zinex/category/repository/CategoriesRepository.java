package namoo.zinex.category.repository;

import namoo.zinex.category.domain.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {}

