package ktpweb.adventurergroups.repository;

import org.springframework.data.repository.CrudRepository;

import ktpweb.adventurergroups.entity.Character;

public interface CharacterRepository extends CrudRepository<Character, Long>
{

}