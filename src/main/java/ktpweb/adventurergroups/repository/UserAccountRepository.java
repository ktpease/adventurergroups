package ktpweb.adventurergroups.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ktpweb.adventurergroups.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long>
{
}