package ktpweb.adventurergroups.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import ktpweb.adventurergroups.entity.Character;
import ktpweb.adventurergroups.entity.CharacterGroup;
import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.model.CharacterGroupDto;
import ktpweb.adventurergroups.repository.CharacterGroupRepository;
import ktpweb.adventurergroups.repository.CharacterRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CharacterService
{
    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private CharacterGroupRepository characterGroupRepository;

    public Character getCharacter(CharacterDto characterDto)
    {
        try
        {
            return characterRepository.findById(characterDto.getId())
                .orElse(null);
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            return null;
        }
    }

    public CharacterGroup getCharacterGroup(CharacterGroupDto characterGroupDto)
    {
        try
        {
            return characterGroupRepository.findById(characterGroupDto.getId())
                .orElse(null);
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            return null;
        }
    }
}
