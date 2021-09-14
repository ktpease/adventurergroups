package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;
import java.util.List;
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
import ktpweb.adventurergroups.entity.UserAccount;
import ktpweb.adventurergroups.exception.CharacterServiceException;
import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.model.CharacterGroupDto;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.MaintainerDto;
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

    private final String EXCEPTION_CHARACTER_CREATE_FOR_MAINTAINER = "Cannot create Character for Maintainer id: ";

    private final String EXCEPTION_CHARACTER_RETRIEVE_FOR_INSTANCE = "Cannot retrieve Characters for instance id: ";
    private final String EXCEPTION_CHARACTER_RETRIEVE_FOR_MAINTAINER = "Cannot retrieve Characters for maintainer id: ";
    private final String EXCEPTION_CHARACTER_RETRIEVE_FOR_GROUP = "Cannot retrieve Characters for group id: ";

    private final String EXCEPTION_CHARACTER_MODEL = "Cannot return model for Character with id: ";

    @Transactional
    public CharacterDto createCharacter(InstanceDto instance)
        throws CharacterServiceException
    {
        // Load and validate Instance.
        if (instance == null)
        {
            throw generateException(
                "Attempted creation of character for a null Instance",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

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

        // Generate and save database entity.
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

        log.info("Created Character with id: {} for instance with id: {}",
            characterEntity.getId(), instance.getId());

        // Return full DTO.
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
        // Attempt to read from the database.
        Character characterEntity;

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

        if (characterEntity == null)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE + characterId
                    + ". Character not found",
                CharacterServiceException.Codes.CHARACTER_NOT_FOUND);
        }

        // Return full DTO.
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
    public CharacterDto updateCharacter(Long characterId,
        CharacterDto characterUpdate) throws CharacterServiceException
    {
        // Attempt to read from the database.
        if (characterUpdate == null)
        {
            throw generateException(
                "Attempted update of a null Character object",
                CharacterServiceException.Codes.NULL_CHARACTER_OBJECT);
        }

        Character characterEntity;

        try
        {
            characterEntity = getCharacterEntity(characterId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_UPDATE + characterId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (characterEntity == null)
        {
            throw generateException(
                EXCEPTION_CHARACTER_UPDATE + characterId
                    + ". Character not found",
                CharacterServiceException.Codes.CHARACTER_NOT_FOUND);
        }

        // Attempt to modify and save character.
        characterEntity.setName(characterUpdate.getName());
        characterEntity.setDescription(characterUpdate.getDescription());
        characterEntity.setColorPrimary(characterUpdate.getColorPrimary());
        characterEntity.setColorSecondary(characterUpdate.getColorSecondary());

        if (characterUpdate.getMaintainer() != null)
        {
            try
            {
                characterEntity.setMaintainer(userAccountService
                    .getUserAccountEntity(characterUpdate.getMaintainer()));
            }
            catch (Exception ex)
            {
                throw generateException(
                    EXCEPTION_CHARACTER_UPDATE + characterId
                        + ". Error reading from database",
                    CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
            }
        }
        else
        {
            characterEntity.setMaintainer(null);
        }

        if (characterUpdate.getCharacterGroup() != null)
        {
            try
            {
                characterEntity.setCharacterGroup(getCharacterGroupEntity(
                    characterUpdate.getCharacterGroup()));
            }
            catch (Exception ex)
            {
                throw generateException(
                    EXCEPTION_CHARACTER_UPDATE + characterUpdate.getId()
                        + ". Error reading from database",
                    CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
            }
        }
        else
        {
            characterEntity.setCharacterGroup(null);
        }

        try
        {
            characterEntity = characterRepository.save(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_UPDATE + characterId
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Character with id: {}", characterId);

        // Return full DTO.
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
    public void deleteCharacter(Long characterId)
        throws CharacterServiceException
    {
        // Attempt to read from the database.
        Character characterEntity;

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

    @Transactional
    public CharacterDto createCharacterForMaintainer(MaintainerDto maintainer)
        throws CharacterServiceException
    {
        // Load and validate Maintainer.
        if (maintainer == null)
        {
            throw generateException(
                "Attempted creation of character for a null user account",
                CharacterServiceException.Codes.INVALID_MAINTAINER_OBJECT);
        }

        UserAccount maintainerEntity;

        try
        {
            maintainerEntity = userAccountService
                .getUserAccountEntity(maintainer);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_CREATE_FOR_MAINTAINER + maintainer.getId()
                    + ". Error reading user account from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Generate and save database entity.
        Character characterEntity = new Character();

        characterEntity.setMaintainer(maintainerEntity);
        characterEntity.setInstance(maintainerEntity.getParentInstance());
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
                EXCEPTION_CHARACTER_CREATE_FOR_MAINTAINER + maintainer.getId()
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Character with id: {} for maintainer with id: {}",
            characterEntity.getId(), maintainer.getId());

        // Return full DTO.
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
    public List<CharacterDto> retrieveCharactersForInstance(
        InstanceDto instance) throws CharacterServiceException
    {
        // Load and validate Instance.
        if (instance == null)
        {
            throw generateException(
                "Attempted retrieval of characters for a null Instance",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_INSTANCE + instance.getId()
                    + ". Error reading Instance from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Attempt to read from the database.
        List<Character> characterEntities;

        try
        {
            characterEntities = getCharacterEntitiesForInstance(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_INSTANCE + instance.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Return DTO list.
        try
        {
            return Optional.ofNullable(characterEntities).map(List::stream)
                .orElseGet(Stream::empty).map(c -> {
                    try
                    {
                        return getCharacterDto(c, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_INSTANCE + instance.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public List<CharacterDto> retrieveCharactersForMaintainer(
        MaintainerDto maintainer) throws CharacterServiceException
    {
        // Load and validate Maintainer.
        if (maintainer == null)
        {
            throw generateException(
                "Attempted retrieval of characters for a null user account",
                CharacterServiceException.Codes.INVALID_MAINTAINER_OBJECT);
        }

        UserAccount maintainerEntity;

        try
        {
            maintainerEntity = userAccountService
                .getUserAccountEntity(maintainer);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_MAINTAINER + maintainer.getId()
                    + ". Error reading user account from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Attempt to read from the database.
        List<Character> characterEntities;

        try
        {
            characterEntities = getCharacterEntitiesForMaintainer(
                maintainerEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_MAINTAINER + maintainer.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Return DTO list.
        try
        {
            return Optional.ofNullable(characterEntities).map(List::stream)
                .orElseGet(Stream::empty).map(c -> {
                    try
                    {
                        return getCharacterDto(c, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_MAINTAINER + maintainer.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public List<CharacterDto> retrieveCharactersForGroup(
        CharacterGroupDto characterGroup) throws CharacterServiceException
    {
        // Load and validate Group.
        if (characterGroup == null)
        {
            throw generateException(
                "Attempted retrieval of characters for a null group",
                CharacterServiceException.Codes.INVALID_CHARACTER_GROUP_OBJECT);
        }

        CharacterGroup characterGroupEntity;

        try
        {
            characterGroupEntity = getCharacterGroupEntity(characterGroup);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_GROUP + characterGroup.getId()
                    + ". Error reading character group from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Attempt to read from the database.
        List<Character> characterEntities;

        try
        {
            characterEntities = getCharacterEntitiesForCharacterGroup(
                characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_GROUP + characterGroup.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Return DTO list.
        try
        {
            return Optional.ofNullable(characterEntities).map(List::stream)
                .orElseGet(Stream::empty).map(c -> {
                    try
                    {
                        return getCharacterDto(c, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CHARACTER_RETRIEVE_FOR_GROUP + characterGroup.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Character Group-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String DEFAULT_GROUP_NAME = "New Group";

    private final String EXCEPTION_GROUP_CREATE = "Cannot create Character Group for Instance id: ";
    private final String EXCEPTION_GROUP_RETRIEVE = "Cannot retrieve Character Group with id: ";
    private final String EXCEPTION_GROUP_UPDATE = "Cannot update Character Group with id: ";
    private final String EXCEPTION_GROUP_DELETE = "Cannot delete Character Group with id: ";

    private final String EXCEPTION_GROUP_RETRIEVE_INSTANCE = "Cannot retrieve Character Groups for Instance id: ";

    private final String EXCEPTION_GROUP_MODEL = "Cannot return model for Character Group with id: ";

    @Transactional
    public CharacterGroupDto createCharacterGroup(InstanceDto instance)
        throws CharacterServiceException
    {
        // Load and validate Instance.
        if (instance == null)
        {
            throw generateException(
                "Attempted creation of a Character Group with a null Instance",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

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

        // Generate and save database entity.
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

        // Return full DTO.
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
        // Attempt to read from the database.
        CharacterGroup characterGroupEntity;

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

        if (characterGroupEntity == null)
        {
            throw generateException(
                EXCEPTION_GROUP_RETRIEVE + characterGroupId
                    + ". Character Group not found",
                CharacterServiceException.Codes.CHARACTER_GROUP_NOT_FOUND);
        }

        // Return full DTO.
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
    public CharacterGroupDto updateCharacterGroup(Long characterGroupId,
        CharacterGroupDto characterGroupUpdate) throws CharacterServiceException
    {
        // Attempt to read from the database.
        if (characterGroupUpdate == null)
        {
            throw generateException(
                "Attempted update of a null Character Group object",
                CharacterServiceException.Codes.NULL_CHARACTER_GROUP_OBJECT);
        }

        CharacterGroup characterGroupEntity;

        try
        {
            characterGroupEntity = getCharacterGroupEntity(characterGroupId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_UPDATE + characterGroupId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (characterGroupEntity == null)
        {
            throw generateException(
                EXCEPTION_GROUP_UPDATE + characterGroupId
                    + ". Character Group not found",
                CharacterServiceException.Codes.CHARACTER_GROUP_NOT_FOUND);
        }

        // Attempt to modify and save group.
        characterGroupEntity.setName(characterGroupUpdate.getName());
        characterGroupEntity
            .setDescription(characterGroupUpdate.getDescription());
        characterGroupEntity
            .setColorPrimary(characterGroupUpdate.getColorPrimary());

        try
        {
            characterGroupEntity = characterGroupRepository
                .save(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_UPDATE + characterGroupId
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Character Group with id: {}", characterGroupId);

        // Return full DTO.
        try
        {
            return getCharacterGroupDto(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_MODEL + characterGroupId
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public void deleteCharacterGroup(Long characterGroupId)
        throws CharacterServiceException
    {
        // Attempt to read from the database.
        CharacterGroup characterGroupEntity;

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

        if (characterGroupEntity == null)
        {
            throw generateException(
                EXCEPTION_GROUP_DELETE + characterGroupId
                    + ". Character Group not found",
                CharacterServiceException.Codes.CHARACTER_GROUP_NOT_FOUND);
        }

        // Ungroup all of the group's characters, if there are any.
        for (Character c : characterGroupEntity.getCharacters())
        {
            c.setCharacterGroup(null);
        }

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

    @Transactional
    public List<CharacterGroupDto> retrieveCharacterGroupsForInstance(
        InstanceDto instance) throws CharacterServiceException
    {
        // Load and validate Instance.
        if (instance == null)
        {
            throw generateException(
                "Attempted retrieval of characters for a null Instance",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_RETRIEVE_INSTANCE + instance.getId()
                    + ". Error reading Instance from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Attempt to read from the database.
        List<CharacterGroup> characterGroupEntities;

        try
        {
            characterGroupEntities = getCharacterGroupEntitiesForInstance(
                instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_RETRIEVE_INSTANCE + instance.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Return DTO list.
        try
        {
            return Optional.ofNullable(characterGroupEntities).map(List::stream)
                .orElseGet(Stream::empty).map(cg -> {
                    try
                    {
                        return getCharacterGroupDto(cg, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_GROUP_RETRIEVE_INSTANCE + instance.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
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

    protected List<Character> getCharacterEntitiesForInstance(Instance instance)
        throws Exception
    {
        return Optional
            .ofNullable(characterRepository.findAllByInstance(instance))
            .map(List::stream).orElseGet(Stream::empty).filter(c -> {
                return c != null && !c.getDeleted();
            }).collect(Collectors.toList());
    }

    protected List<Character> getCharacterEntitiesForMaintainer(
        UserAccount userAccount) throws Exception
    {
        return Optional
            .ofNullable(characterRepository.findAllByMaintainer(userAccount))
            .map(List::stream).orElseGet(Stream::empty).filter(c -> {
                return c != null && !c.getDeleted();
            }).collect(Collectors.toList());
    }

    protected List<Character> getCharacterEntitiesForCharacterGroup(
        CharacterGroup characterGroup) throws Exception
    {
        return Optional
            .ofNullable(
                characterRepository.findAllByCharacterGroup(characterGroup))
            .map(List::stream).orElseGet(Stream::empty).filter(c -> {
                return c != null && !c.getDeleted();
            }).collect(Collectors.toList());
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

    protected List<CharacterGroup> getCharacterGroupEntitiesForInstance(
        Instance instance) throws Exception
    {
        return Optional
            .ofNullable(characterGroupRepository.findAllByInstance(instance))
            .map(List::stream).orElseGet(Stream::empty).filter(cg -> {
                return cg != null && !cg.getDeleted();
            }).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // DTO Mapping methods.
    // -----------------------------------------------------------------------------------------------------------------

    protected CharacterDto getCharacterDto(Character character) throws Exception
    {
        return getCharacterDto(character, false);
    }

    protected CharacterDto getCharacterDto(Character character,
        Boolean skipNested) throws Exception
    {
        CharacterDto dto = new CharacterDto();

        dto.setId(character.getId());
        dto.setName(character.getName());
        dto.setDescription(character.getDescription());

        dto.setCreateDate(character.getCreateDate());

        if (!skipNested)
        {
            dto.setInstance(character.getInstance() != null
                ? instanceService.getInstanceDto(character.getInstance(), true)
                : null);

            dto.setMaintainer(character.getMaintainer() != null
                ? userAccountService.getMaintainerDto(character.getMaintainer(),
                    true)
                : null);

            dto.setCharacterGroup(character.getCharacterGroup() != null
                ? getCharacterGroupDto(character.getCharacterGroup(), true)
                : null);
        }

        return dto;
    }

    protected CharacterGroupDto getCharacterGroupDto(CharacterGroup cg)
        throws Exception
    {
        return getCharacterGroupDto(cg, false);
    }

    protected CharacterGroupDto getCharacterGroupDto(CharacterGroup cg,
        Boolean skipNested) throws Exception
    {
        CharacterGroupDto dto = new CharacterGroupDto();

        dto.setId(cg.getId());

        dto.setName(cg.getName());
        dto.setDescription(cg.getDescription());
        dto.setColorPrimary(cg.getColorPrimary());

        dto.setCreateDate(cg.getCreateDate());

        if (!skipNested)
        {
            dto.setInstance(cg.getInstance() != null
                ? instanceService.getInstanceDto(cg.getInstance(), true)
                : null);

            dto.setCharacters(Optional.ofNullable(cg.getCharacters())
                .map(Set::stream).orElseGet(Stream::empty).map(c -> {
                    try
                    {
                        return getCharacterDto(c, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet()));
        }

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
