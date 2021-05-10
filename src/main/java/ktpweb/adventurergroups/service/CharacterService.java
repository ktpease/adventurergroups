package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;
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
import ktpweb.adventurergroups.model.UserAccountDto;
import ktpweb.adventurergroups.repository.CharacterGroupRepository;
import ktpweb.adventurergroups.repository.CharacterRepository;
import ktpweb.adventurergroups.util.UserAccountUtils.UserAccountRoles;
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

    private final String DEFAULT_CHARACTER_NAME = "New Character";
    private final String DEFAULT_GROUP_NAME = "New Group";

    private final String EXCEPTION_PRIMER_CHARACTER_CREATE = "Cannot create Character for Instance id: ";

    private final String EXCEPTION_PRIMER_GROUP_CREATE = "Cannot create Character Group for Instance id: ";

    private final String EXCEPTION_PRIMER_CHARACTER_MODEL = "Cannot return model for Character with id: ";
    private final String EXCEPTION_PRIMER_GROUP_MODEL = "Cannot return model for Character Group with id: ";

    public Character getCharacter(Long id) throws Exception
    {
        try
        {
            Character character = characterRepository.findById(id).orElse(null);

            return (character != null && !character.getDeleted()) ? character
                : null;
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

    protected CharacterGroup getCharacterGroup(Long id) throws Exception
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

    protected CharacterGroup getCharacterGroup(
        CharacterGroupDto characterGroupDto) throws Exception
    {
        return getCharacterGroup(characterGroupDto.getId());
    }

    @Transactional
    public CharacterDto createCharacter(InstanceDto instance,
        CharacterGroupDto characterGroup, UserAccountDto creator)
        throws CharacterServiceException
    {
        if (instance == null)
        {
            throw generateException(
                "Attempted creation of a Character with a null Instance",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        if (creator == null)
        {
            throw generateException(
                "Attempted creation of a Character with a null creator User Account",
                CharacterServiceException.Codes.INVALID_CREATOR_OBJECT);
        }

        log.info(
            "Attempting to create Character for Instance id: {} by User Account id: {}",
            instance.getId(), creator.getId());

        // Load and validate Instance.
        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstance(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Error reading Instance from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Instance not found",
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        if (!instanceEntity.getActive())
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Instance is inactive",
                CharacterServiceException.Codes.INSTANCE_INACTIVE);
        }

        // Load and validate creator User Account.
        UserAccount creatorEntity;

        try
        {
            creatorEntity = userAccountService.getUserAccount(creator);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Error reading creator User Account with id: "
                    + creator.getId() + " from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (creatorEntity == null)
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Creator User Account with id: " + creator.getId()
                    + " not found",
                CharacterServiceException.Codes.CREATOR_NOT_FOUND);
        }

        if (creatorEntity.getRole() == UserAccountRoles.USER_ROLE_TRANSIENT)
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Creator User Account with id: " + creator.getId()
                    + " is transient",
                CharacterServiceException.Codes.INVALID_CREATOR_ROLE);
        }

        if ((creatorEntity.getRole() == UserAccountRoles.USER_ROLE_OWNER
            && creatorEntity.getInstances().contains(instanceEntity))
            || (creatorEntity.getRole() == UserAccountRoles.USER_ROLE_MAINTAINER
                && creatorEntity.getParentInstance() != instanceEntity))
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Creator User Account with id: " + creator.getId()
                    + " should not see Instance",
                CharacterServiceException.Codes.INVALID_CREATOR_OBJECT);
        }

        // Load and validate Character Group.
        if (characterGroup != null)
        {

        }

        // Generate database entity.
        Character characterEntity = new Character();

        characterEntity.setInstance(instanceEntity);
        characterEntity.setName(DEFAULT_CHARACTER_NAME);
        characterEntity.setDescription("");
        characterEntity.setCreatedBy(creatorEntity);
        characterEntity.setCreateDate(LocalDateTime.now());
        characterEntity.setDeleted(false);

        try
        {
            characterEntity = characterRepository.save(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_CREATE + instance.getId()
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Character with id: {}", characterEntity.getId());

        try
        {
            return getCharacterDto(characterEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_CHARACTER_MODEL + characterEntity.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }
    }

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
            instanceEntity = instanceService.getInstance(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_GROUP_CREATE + instance.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_PRIMER_GROUP_CREATE + instance.getId()
                    + ". Instance not found",
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        if (instanceEntity.getActive())
        {
            throw generateException(
                EXCEPTION_PRIMER_GROUP_CREATE + instance.getId()
                    + ". Instance is inactive",
                CharacterServiceException.Codes.INSTANCE_INACTIVE);
        }

        // Generate database entity.
        CharacterGroup characterGroupEntity = new CharacterGroup();

        characterGroupEntity.setInstance(instanceEntity);
        characterGroupEntity.setName(DEFAULT_GROUP_NAME);
        characterGroupEntity.setDescription("");
        characterGroupEntity.setCreateDate(LocalDateTime.now());
        characterGroupEntity.setDeleted(false);

        try
        {
            characterGroupEntity = characterGroupRepository
                .save(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_GROUP_CREATE + instance.getId()
                    + ". Error writing to database",
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Character Group with id: {}",
            characterGroupEntity.getId());

        try
        {
            return getCharacterGroupDto(characterGroupEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_GROUP_MODEL + characterGroupEntity.getId()
                    + ". Error reading from database",
                CharacterServiceException.Codes.DATABASE_ERROR_READ, ex);
        }
    }

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

    public CharacterDto getCharacterDto(Character character) throws Exception
    {
        CharacterDto dto = new CharacterDto();

        dto.setId(character.getId());
        dto.setName(character.getName());
        dto.setDescription(character.getDescription());
        dto.setInstanceId(character.getInstance().getId());
        dto.setMaintainerId(character.getMaintainer().getId());
        dto.setCharacterGroupId(character.getCharacterGroup().getId());
        dto.setCreateDate(character.getCreateDate());

        return dto;
    }

    public CharacterGroupDto getCharacterGroupDto(CharacterGroup cg)
        throws Exception
    {
        CharacterGroupDto dto = new CharacterGroupDto();

        dto.setId(cg.getId());
        dto.setInstanceId(cg.getInstance().getId());
        dto.setName(cg.getName());
        dto.setDescription(cg.getDescription());
        dto.setColorPrimary(cg.getColorPrimary());
        dto.setCharacterIds(Optional.ofNullable(cg.getCharacters())
            .map(Set::stream).orElseGet(Stream::empty).map(c -> c.getId())
            .collect(Collectors.toSet()));
        dto.setCreateDate(cg.getCreateDate());

        return dto;
    }
}
