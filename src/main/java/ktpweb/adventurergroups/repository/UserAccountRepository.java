package ktpweb.adventurergroups.repository;

import org.springframework.data.repository.CrudRepository;

import ktpweb.adventurergroups.entity.UserAccount;

public interface UserAccountRepository extends CrudRepository<UserAccount, Long>
{

}