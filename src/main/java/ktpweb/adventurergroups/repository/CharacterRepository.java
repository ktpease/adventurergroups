package ktpweb.adventurergroups.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ktpweb.adventurergroups.entity.Character;
import ktpweb.adventurergroups.entity.CharacterGroup;
import ktpweb.adventurergroups.entity.Instance;
import ktpweb.adventurergroups.entity.UserAccount;

public interface CharacterRepository extends CrudRepository<Character, Long>
{
    List<Character> findAllByInstance(Instance instance);

    List<Character> findAllByMaintainer(UserAccount userAccount);

    List<Character> findAllByCharacterGroup(CharacterGroup characterGroup);
}