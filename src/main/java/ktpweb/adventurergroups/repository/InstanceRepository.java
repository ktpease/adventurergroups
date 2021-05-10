package ktpweb.adventurergroups.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ktpweb.adventurergroups.entity.Instance;

public interface InstanceRepository extends JpaRepository<Instance, Long>
{

}