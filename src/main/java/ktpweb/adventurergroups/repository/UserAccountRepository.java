package ktpweb.adventurergroups.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ktpweb.adventurergroups.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long>
{
    Optional<UserAccount> findByInviteToken(String inviteToken);
}