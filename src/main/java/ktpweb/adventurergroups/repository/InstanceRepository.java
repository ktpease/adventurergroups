package ktpweb.adventurergroups.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ktpweb.adventurergroups.entity.Instance;
import ktpweb.adventurergroups.entity.UserAccount;

public interface InstanceRepository extends JpaRepository<Instance, Long>
{
    List<Instance> findAllByOwner(UserAccount userAccount);
}