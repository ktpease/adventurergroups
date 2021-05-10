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
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    protected CharacterGroup getCharacterGroup(CharacterGroupDto characterGroupDto)
        throws Exception
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
            log.info("Attempted creation of a Character with a null Instance.");

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT,
                "Attempted creation of a Character with a null Instance.");
        }

        if (creator == null)
        {
            log.info(
                "Attempted creation of a Character with a null creator User Account.");

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT,
                "Attempted creation of a Character with a null creator User Account.");
        }

        log.info(
            "Attempting to create Character for Instance id {} by User Account id {}",
            instance.getId(), creator.getId());

        // Load and validate Instance.
        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstance(instance);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create Character for Instance id {}. Error reading from database.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot create Character for Instance id " + instance.getId()
                    + ". Error reading from database.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot create Character for Instance id {}. Instance not found.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND,
                "Cannot create Character for Instance id " + instance.getId()
                    + ". Instance not found.");
        }

        if (instanceEntity.getActive())
        {
            log.info(
                "Cannot create Character for Instance id {}. Instance is inactive.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND,
                "Cannot create Character for Instance id " + instance.getId()
                    + ". Instance is inactive.");
        }

        // Load and validate creator User Account.
        UserAccount creatorEntity;

        try
        {
            creatorEntity = userAccountService.getUserAccount(creator);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create Character from creator User Account id {}. Error reading from database.",
                creator.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot create Character for Instance id " + creator.getId()
                    + ". Error reading from database.",
                e);
        }

        if (creatorEntity == null)
        {
            log.info(
                "Cannot create Character from creator User Account id {}. User Account not found.",
                creator.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.CREATOR_NOT_FOUND,
                "Cannot create Character for creator User Account id "
                    + creator.getId() + ". User Account not found.");
        }

        if (creatorEntity.getRole() == UserAccountRoles.USER_ROLE_TRANSIENT)
        {
            log.info(
                "Cannot create Character from creator User Account id {}. User account is transient!",
                creator.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INVALID_CREATOR_ROLE,
                "Cannot create Character from creator User Account id "
                    + creator.getId() + ". User account is transient!");
        }

        if ((creatorEntity.getRole() == UserAccountRoles.USER_ROLE_OWNER
            && creatorEntity.getInstances().contains(instanceEntity))
            || (creatorEntity.getRole() == UserAccountRoles.USER_ROLE_MAINTAINER
                && creatorEntity.getParentInstance() != instanceEntity))
        {
            log.info(
                "Cannot create Character from creator User Account id {}. User account should not see Instance!",
                creator.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INVALID_CREATOR_OBJECT,
                "Cannot create Character from creator User Account id "
                    + creator.getId()
                    + ". User account should not see Instance!");
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
        catch (Exception e)
        {
            log.info(
                "Cannot create Character for Instance id {}. Error writing to database.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot create Character for Instance id " + instance.getId()
                    + ". Error writing to database.",
                e);
        }

        log.info("Created Character with id {}", characterEntity.getId());

        try
        {
            return getCharacterDto(characterEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for Character id {}. Error reading from database.",
                characterEntity.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for Character id "
                    + characterEntity.getId()
                    + ". Error reading from database.",
                e);
        }
    }

    @Transactional
    public CharacterGroupDto createCharacterGroup(InstanceDto instance)
        throws CharacterServiceException
    {
        if (instance == null)
        {
            log.info("Attempted creation of a Character with a null Instance.");

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INVALID_INSTANCE_OBJECT,
                "Attempted creation of a Character with a null Instance.");
        }

        log.info("Attempting to create Character Group for Instance id {}",
            instance.getId());

        // Load and validate Instance.
        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstance(instance);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create Character Group for Instance id {}. Error reading from database.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot create Character Group for Instance id "
                    + instance.getId() + ". Error reading from database.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot create Character Group for Instance id {}. Instance not found.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND,
                "Cannot create Character Group for Instance id "
                    + instance.getId() + ". Instance not found.");
        }

        if (instanceEntity.getActive())
        {
            log.info(
                "Cannot create Character Group for Instance id {}. Instance is inactive.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.INSTANCE_NOT_FOUND,
                "Cannot create Character Group for Instance id "
                    + instance.getId() + ". Instance is inactive.");
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
        catch (Exception e)
        {
            log.info(
                "Cannot create Character Group for Instance id {}. Error writing to database.",
                instance.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot create Character Group for Instance id "
                    + instance.getId() + ". Error writing to database.",
                e);
        }

        log.info("Created Character Group with id {}",
            characterGroupEntity.getId());

        try
        {
            return getCharacterGroupDto(characterGroupEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for Character Group id {}. Error reading from database.",
                characterGroupEntity.getId());

            throw new CharacterServiceException(
                CharacterServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for Character Group id "
                    + characterGroupEntity.getId()
                    + ". Error reading from database.",
                e);
        }
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
