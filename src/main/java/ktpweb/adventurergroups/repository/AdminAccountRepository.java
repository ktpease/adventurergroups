package ktpweb.adventurergroups.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ktpweb.adventurergroups.entity.AdminAccount;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long>
{
}