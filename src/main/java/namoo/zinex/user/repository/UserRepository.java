package namoo.zinex.user.repository;

import java.util.Optional;
import namoo.zinex.user.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {

  Optional<Users> findByEmail(String email);

  boolean existsByEmail(String email);
}

