package ktpweb.adventurergroups.repository;

import org.springframework.data.repository.CrudRepository;

import ktpweb.adventurergroups.entity.UserAccount;

public interface InstanceRepository extends CrudRepository<UserAccount, Long>
{

}