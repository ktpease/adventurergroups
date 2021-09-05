package ktpweb.adventurergroups.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ktpweb.adventurergroups.entity.CharacterGroup;
import ktpweb.adventurergroups.entity.Instance;

public interface CharacterGroupRepository extends CrudRepository<CharacterGroup, Long>
{
    List<CharacterGroup> findAllByInstance(Instance instance);
}