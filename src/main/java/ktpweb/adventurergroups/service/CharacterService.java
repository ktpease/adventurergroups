package ktpweb.adventurergroups.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Character getCharacter(Long id) throws Exception
    {
        try
        {
            return characterRepository.findById(id).orElse(null);
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    public Character getCharacter(CharacterDto characterDto) throws Exception
    {
        return getCharacter(characterDto.getId());
    }

    public CharacterGroup getCharacterGroup(Long id) throws Exception
    {
        try
        {
            return characterGroupRepository.findById(id).orElse(null);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    public CharacterGroup getCharacterGroup(CharacterGroupDto characterGroupDto)
        throws Exception
    {
        return getCharacterGroup(characterGroupDto.getId());
    }
}
