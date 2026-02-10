package namoo.zinex.account.repository;

import namoo.zinex.account.domain.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepository extends JpaRepository<Accounts, Long> {}

