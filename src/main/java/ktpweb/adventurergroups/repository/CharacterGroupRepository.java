package ktpweb.adventurergroups.repository;

import org.springframework.data.repository.CrudRepository;

import ktpweb.adventurergroups.entity.CharacterGroup;

public interface CharacterGroupRepository extends CrudRepository<CharacterGroup, Long>
{

}