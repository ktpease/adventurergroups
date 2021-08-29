package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ktpweb.adventurergroups.entity.Character;
import ktpweb.adventurergroups.entity.CharacterGroup;
import ktpweb.adventurergroups.entity.Instance;
import ktpweb.adventurergroups.exception.CharacterServiceException;
import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.model.CharacterGroupDto;
import ktpweb.adventurergroups.model.InstanceDto;
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

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private InstanceService instanceService;

    // -----------------------------------------------------------------------------------------------------------------
    // Character-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String DEFAULT_CHARACTER_NAME = "New Character";

    private final String EXCEPTION_CHARACTER_CREATE = "Cannot create Character for Instance id: ";
    private final String EXCEPTION_CHARACTER_RETRIEVE = "Cannot retrieve Character with id: ";
    private final String EXCEPTION_CHARACTER_UPDATE = "Cannot update Character with id: ";
    private final String EXCEPTION_CHARACTER_DELETE = "Cannot delete Character with id: ";

    private final String EXCEPTION_CHARACTER_MODEL = "Cannot return model for Character with id: ";

    @Transactional
    public CharacterDto createCharacter(InstanceDto instance)
        throws CharacterServiceException
    {
        if (instance == null)
        {
            throw generateException(
                "Attempted creation of a Character with a null Instance",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        // Load and validate Instance.
        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_CREATE + instance.getId()
                    + ". Error reading Instance from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_CHARACTER_CREATE + instance.getId()
                    + ". Instance not found",
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        if (!instanceEntity.getActive())
        {
            throw generateException(
                EXCEPTION_CHARACTER_CREATE + instance.getId()
                    + ". Instance is inactive",
                CharacterServiceException.Codes.INSTANCE_INACTIVE);
        }

        // Generate database entity.
        Character characterEntity = new Character();

        characterEntity.setInstance(instanceEntity);
        characterEntity.setName(DEFAULT_CHARACTER_NAME);
        characterEntity.setDescription("");
        characterEntity.setCreateDate(LocalDateTime.now());

        try
        {
            characterEntity = characterRepository.save(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_CREATE + instance.getId()
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Character with id: {}", characterEntity.getId());

        // Update inverse side of Character-Instance relationship because
        // it doesn't automatically do that?!?!
        Set<Character> characterList = instanceEntity.getCharacters();

        if (characterList == null)
            characterList = new HashSet<Character>();

        characterList.add(characterEntity);

        instanceEntity.setCharacters(characterList);

        try
        {
            return getCharacterDto(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_MODEL + characterEntity.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public CharacterDto retrieveCharacter(Long characterId)
        throws CharacterServiceException
    {
        Character characterEntity;

        // Attempt to read from the database.
        try
        {
            characterEntity = getCharacterEntity(characterId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE + characterId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the character exists.
        if (characterEntity == null)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE + characterId
                    + ". Character not found",
                CharacterServiceException.Codes.CHARACTER_NOT_FOUND);
        }

        log.info("Found Character with id: {}", characterId);

        // Return a full DTO.
        try
        {
            return getCharacterDto(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_MODEL + characterId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public CharacterDto updateCharacter(CharacterDto character)
        throws CharacterServiceException
    {
        if (character == null)
        {
            throw generateException(
                "Attempted update of a null Character object",
                CharacterServiceException.Codes.NULL_CHARACTER_OBJECT);
        }

        log.info("Attempting to update Instance id: {}", character.getId());

        Character characterEntity;

        // Attempt to read from the database.
        try
        {
            characterEntity = getCharacterEntity(character);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_UPDATE + character.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (characterEntity == null)
        {
            throw generateException(
                EXCEPTION_CHARACTER_UPDATE + character.getId()
                    + ". Character not found",
                CharacterServiceException.Codes.CHARACTER_NOT_FOUND);
        }

        // Attempt to modify and save character.
        characterEntity.setName(character.getName());
        characterEntity.setDescription(character.getDescription());
        characterEntity.setColorPrimary(character.getColorPrimary());
        characterEntity.setColorSecondary(character.getColorSecondary());

        try
        {
            characterEntity = characterRepository.save(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_UPDATE + character.getId()
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Character with id: {}", character.getId());

        try
        {
            return getCharacterDto(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_MODEL + character.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public void deleteCharacter(Long characterId)
        throws CharacterServiceException
    {
        log.info("Attempting to delete Character with id: {}", characterId);

        Character characterEntity;

        // Attempt to read from the database.
        try
        {
            characterEntity = getCharacterEntity(characterId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_DELETE + characterId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (characterEntity == null)
        {
            throw generateException(
                EXCEPTION_CHARACTER_DELETE + characterId
                    + ". Character not found",
                CharacterServiceException.Codes.CHARACTER_NOT_FOUND);
        }

        // Attempt to soft-delete character.
        characterEntity.setDeleted(true);
        characterEntity.setDeleteDate(LocalDateTime.now());

        try
        {
            characterEntity = characterRepository.save(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_DELETE + characterId
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Deleted Character with id: {}", characterId);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Character Group-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String DEFAULT_GROUP_NAME = "New Group";

    private final String EXCEPTION_GROUP_CREATE = "Cannot create Character Group for Instance id: ";
    private final String EXCEPTION_GROUP_RETRIEVE = "Cannot retrieve Character Group with id: ";
    private final String EXCEPTION_GROUP_UPDATE = "Cannot update Character Group with id: ";
    private final String EXCEPTION_GROUP_DELETE = "Cannot delete Character Group with id: ";

    private final String EXCEPTION_GROUP_MODEL = "Cannot return model for Character Group with id: ";

    @Transactional
    public CharacterGroupDto createCharacterGroup(InstanceDto instance)
        throws CharacterServiceException
    {
        if (instance == null)
        {
            throw generateException(
                "Attempted creation of a Character with a null Instance",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        log.info("Attempting to create Character Group for Instance id: {}",
            instance.getId());

        // Load and validate Instance.
        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_CREATE + instance.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_GROUP_CREATE + instance.getId()
                    + ". Instance not found",
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        if (!instanceEntity.getActive())
        {
            throw generateException(
                EXCEPTION_GROUP_CREATE + instance.getId()
                    + ". Instance is inactive",
                CharacterServiceException.Codes.INSTANCE_INACTIVE);
        }

        // Generate database entity.
        CharacterGroup characterGroupEntity = new CharacterGroup();

        characterGroupEntity.setInstance(instanceEntity);
        characterGroupEntity.setName(DEFAULT_GROUP_NAME);
        characterGroupEntity.setDescription("");
        characterGroupEntity.setCreateDate(LocalDateTime.now());

        try
        {
            characterGroupEntity = characterGroupRepository
                .save(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_CREATE + instance.getId()
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Character Group with id: {}",
            characterGroupEntity.getId());

        // Update inverse side of CharacterGroup-Instance relationship because
        // it doesn't automatically do that?!?!
        Set<CharacterGroup> groupList = instanceEntity.getCharacterGroups();

        if (groupList == null)
            groupList = new HashSet<CharacterGroup>();

        groupList.add(characterGroupEntity);

        instanceEntity.setCharacterGroups(groupList);

        try
        {
            return getCharacterGroupDto(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_MODEL + characterGroupEntity.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public CharacterGroupDto retrieveCharacterGroup(Long characterGroupId)
        throws CharacterServiceException
    {
        CharacterGroup characterGroupEntity;

        // Attempt to read from the database.
        try
        {
            characterGroupEntity = getCharacterGroupEntity(characterGroupId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_RETRIEVE + characterGroupId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the group exists.
        if (characterGroupEntity == null)
        {
            throw generateException(
                EXCEPTION_GROUP_RETRIEVE + characterGroupId
                    + ". Character Group not found",
                CharacterServiceException.Codes.CHARACTER_GROUP_NOT_FOUND);
        }

        log.info("Found Character Group with id: {}",
            characterGroupEntity.getId());

        // Return a full DTO.
        try
        {
            return getCharacterGroupDto(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_MODEL + characterGroupEntity.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public CharacterGroupDto updateCharacterGroup(
        CharacterGroupDto characterGroup) throws CharacterServiceException
    {
        if (characterGroup == null)
        {
            throw generateException(
                "Attempted update of a null Character Group object",
                CharacterServiceException.Codes.NULL_CHARACTER_GROUP_OBJECT);
        }

        CharacterGroup characterGroupEntity;

        // Attempt to read from the database.
        try
        {
            characterGroupEntity = getCharacterGroupEntity(
                characterGroup.getId());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_UPDATE + characterGroup.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the group exists.
        if (characterGroupEntity == null)
        {
            throw generateException(
                EXCEPTION_GROUP_UPDATE + characterGroup.getId()
                    + ". Character Group not found",
                CharacterServiceException.Codes.CHARACTER_GROUP_NOT_FOUND);
        }

        // Attempt to modify and save group.
        characterGroupEntity.setName(characterGroup.getName());
        characterGroupEntity.setDescription(characterGroup.getDescription());
        characterGroupEntity.setColorPrimary(characterGroup.getColorPrimary());

        try
        {
            characterGroupEntity = characterGroupRepository
                .save(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_UPDATE + characterGroup.getId()
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Character Group with id: {}", characterGroup.getId());

        try
        {
            return getCharacterGroupDto(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_MODEL + characterGroupEntity.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public void deleteCharacterGroup(Long characterGroupId)
        throws CharacterServiceException
    {
        CharacterGroup characterGroupEntity;

        // Attempt to read from the database.
        try
        {
            characterGroupEntity = getCharacterGroupEntity(characterGroupId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_DELETE + characterGroupId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the group exists.
        if (characterGroupEntity == null)
        {
            throw generateException(
                EXCEPTION_GROUP_DELETE + characterGroupId
                    + ". Character Group not found",
                CharacterServiceException.Codes.CHARACTER_GROUP_NOT_FOUND);
        }

        // TODO: Move all characters in this group to the instance's root group.

        // Attempt to soft-delete group.
        characterGroupEntity.setDeleted(true);
        characterGroupEntity.setDeleteDate(LocalDateTime.now());

        try
        {
            characterGroupEntity = characterGroupRepository
                .save(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_DELETE + characterGroupId
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Deleted Character Group with id: {}", characterGroupId);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // General methods, not to be used with business layer.
    // -----------------------------------------------------------------------------------------------------------------

    protected Character getCharacterEntity(Long id) throws Exception
    {
        try
        {
            Character character = characterRepository.findById(id).orElse(null);

            return (character == null || character.getDeleted()) ? null
                : character;
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    protected Character getCharacterEntity(CharacterDto characterDto)
        throws Exception
    {
        return getCharacterEntity(characterDto.getId());
    }

    protected CharacterGroup getCharacterGroupEntity(Long id) throws Exception
    {
        try
        {
            CharacterGroup characterGroup = characterGroupRepository
                .findById(id).orElse(null);

            return (characterGroup != null && !characterGroup.getDeleted())
                ? characterGroup
                : null;
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    protected CharacterGroup getCharacterGroupEntity(
        CharacterGroupDto characterGroupDto) throws Exception
    {
        return getCharacterGroupEntity(characterGroupDto.getId());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // DTO Mapping methods.
    // -----------------------------------------------------------------------------------------------------------------

    protected CharacterDto getCharacterDto(Character character) throws Exception
    {
        CharacterDto dto = new CharacterDto();

        dto.setId(character.getId());
        dto.setName(character.getName());
        dto.setDescription(character.getDescription());
        dto.setInstanceId(
            character.getInstance() != null ? character.getInstance().getId()
                : null);
        dto.setMaintainerId(character.getMaintainer() != null
            ? character.getMaintainer().getId()
            : null);
        dto.setCharacterGroupId(character.getCharacterGroup() != null
            ? character.getCharacterGroup().getId()
            : null);
        dto.setCreateDate(character.getCreateDate());

        return dto;
    }

    protected CharacterGroupDto getCharacterGroupDto(CharacterGroup cg)
        throws Exception
    {
        CharacterGroupDto dto = new CharacterGroupDto();

        dto.setId(cg.getId());
        dto.setInstanceId(
            cg.getInstance() != null ? cg.getInstance().getId() : null);
        dto.setName(cg.getName());
        dto.setDescription(cg.getDescription());
        dto.setColorPrimary(cg.getColorPrimary());
        dto.setCharacterIds(Optional.ofNullable(cg.getCharacters())
            .map(Set::stream).orElseGet(Stream::empty).map(c -> c.getId())
            .collect(Collectors.toSet()));
        dto.setCreateDate(cg.getCreateDate());

        return dto;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Exception handling methods.
    // -----------------------------------------------------------------------------------------------------------------

    private CharacterServiceException generateException(String message,
        CharacterServiceException.Codes code)
    {
        log.error(message);

        return new CharacterServiceException(code, message);
    }

    private CharacterServiceException generateException(String message,
        CharacterServiceException.Codes code, Exception ex)
    {
        log.error(message, ex);

        return new CharacterServiceException(code, message, ex);
    }
}
