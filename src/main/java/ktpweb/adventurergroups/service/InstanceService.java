package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import ktpweb.adventurergroups.entity.Instance;
import ktpweb.adventurergroups.entity.UserAccount;
import ktpweb.adventurergroups.exception.InstanceServiceException;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.OwnerDto;
import ktpweb.adventurergroups.repository.InstanceRepository;
import ktpweb.adventurergroups.util.UserAccountUtils.UserAccountRoles;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstanceService
{
    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private CharacterService characterService;

    // -----------------------------------------------------------------------------------------------------------------
    // Instance-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String EXCEPTION_CREATE = "Cannot create Instance for owner User Account id: ";
    private final String EXCEPTION_RETRIEVE = "Cannot retrieve Instance with id: ";
    private final String EXCEPTION_UPDATE = "Cannot update Instance with id: ";
    private final String EXCEPTION_ACTIVATE = "Cannot activate Instance with id: ";
    private final String EXCEPTION_DEACTIVATE = "Cannot deactivate Instance with id: ";
    private final String EXCEPTION_DELETE = "Cannot delete Instance with id: ";

    private final String EXCEPTION_MODEL = "Cannot return model for Instance with id: ";

    @Transactional
    public InstanceDto createInstance(OwnerDto owner, String subdomainName)
        throws InstanceServiceException
    {
        if (owner == null)
        {
            throw generateException(
                "Attempted creation of an Instance with a null owner User Account",
                InstanceServiceException.Codes.INVALID_OWNER_OBJECT);
        }

        log.info("Attempting to create Instance for owner User Account id: {}",
            owner.getId());

        UserAccount ownerEntity;

        try
        {
            ownerEntity = userAccountService.getUserAccountEntity(owner);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CREATE + owner.getId()
                    + ". Error reading User Account from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (ownerEntity == null)
        {
            throw generateException(
                EXCEPTION_CREATE + owner.getId() + ". User Account not found",
                InstanceServiceException.Codes.OWNER_NOT_FOUND);
        }

        if (ownerEntity.getRole() != UserAccountRoles.USER_ROLE_OWNER)
        {
            throw generateException(
                EXCEPTION_CREATE + owner.getId()
                    + ". User Account is not an owner role",
                InstanceServiceException.Codes.INVALID_OWNER_ROLE);
        }

        // Invalid subdomain name.
        if (!StringUtils.hasText(subdomainName))
        {
            throw generateException(
                EXCEPTION_CREATE + owner.getId() + ". Invalid subdomain name",
                InstanceServiceException.Codes.INVALID_SUBDOMAINNAME);
        }

        // Check to see if an instance with the same subdomain name exists.
        if (instanceExists(subdomainName))
        {
            throw generateException(
                EXCEPTION_CREATE + owner.getId() + ". Subdomain already exists",
                InstanceServiceException.Codes.INSTANCE_ALREADY_EXISTS);
        }

        // Generate database entity.
        Instance instanceEntity = new Instance();

        instanceEntity.setOwner(ownerEntity);
        instanceEntity.setSubdomainName(subdomainName);
        instanceEntity.setDisplayName(subdomainName);
        instanceEntity.setCreateDate(LocalDateTime.now());

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_CREATE + owner.getId()
                    + ". Error writing to database",
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Instance with id: {}", instanceEntity.getId());

        // Update inverse side of Instance-Owner relationship.
        Set<Instance> instanceList = ownerEntity.getInstances();

        if (instanceList == null)
            instanceList = new HashSet<Instance>();

        instanceList.add(instanceEntity);

        ownerEntity.setInstances(instanceList);

        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MODEL + instanceEntity.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ_MAPPING, ex);
        }
    }

    @Transactional
    public InstanceDto retrieveInstance(Long instanceId)
        throws InstanceServiceException
    {
        Instance instanceEntity;

        // Attempt to read from the database.
        try
        {
            instanceEntity = getInstanceEntity(instanceId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_RETRIEVE + instanceId
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_RETRIEVE + instanceId + ". Instance not found",
                InstanceServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        log.info("Found Instance with id: {}", instanceEntity.getId());

        // Prepare collection objects and return a full DTO.
        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MODEL + instanceEntity.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ_MAPPING, ex);
        }
    }

    @Transactional
    public InstanceDto updateInstance(InstanceDto instance)
        throws InstanceServiceException
    {
        if (instance == null)
        {
            throw generateException("Attempted update of a null Instance",
                InstanceServiceException.Codes.NULL_INSTANCE_OBJECT);
        }

        log.info("Attempting to update Instance with id: {}", instance.getId());

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_UPDATE + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_UPDATE + instance.getId()
                    + ". Instance not found in database",
                InstanceServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        // Attempt to modify and save instance.
        instanceEntity.setSubdomainName(instance.getSubdomainName());
        instanceEntity.setDisplayName(instance.getDisplayName());
        instanceEntity.setDescription(instance.getDescription());

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_UPDATE + instance.getId()
                    + ". Error writing to database",
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Instance with id: {}", instanceEntity.getId());

        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MODEL + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ_MAPPING, ex);
        }
    }

    @Transactional
    public InstanceDto activateInstance(InstanceDto instance)
        throws InstanceServiceException
    {
        if (instance == null)
        {
            throw generateException("Attempted activation of a null Instance",
                InstanceServiceException.Codes.NULL_INSTANCE_OBJECT);
        }

        log.info("Attempting to activate Instance with id: {}",
            instance.getId());

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ACTIVATE + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_ACTIVATE + instance.getId()
                    + ". Instance not found in database",
                InstanceServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        // Attempt to modify and save instance.
        instanceEntity.setActive(true);
        instanceEntity.setLastActivateDate(LocalDateTime.now());

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ACTIVATE + instance.getId()
                    + ". Error writing to database",
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Activated Instance with id: {}", instanceEntity.getId());

        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MODEL + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ_MAPPING, ex);
        }
    }

    @Transactional
    public InstanceDto deactivateInstance(InstanceDto instance)
        throws InstanceServiceException
    {
        if (instance == null)
        {
            throw generateException("Attempted deactivation of a null Instance",
                InstanceServiceException.Codes.NULL_INSTANCE_OBJECT);
        }

        log.info("Attempting to deactivate Instance with id: {}",
            instance.getId());

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_DEACTIVATE + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_DEACTIVATE + instance.getId()
                    + ". Instance not found in database",
                InstanceServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        // Attempt to modify and save instance.
        instanceEntity.setActive(false);
        instanceEntity.setLastDeactivateDate(LocalDateTime.now());

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_DEACTIVATE + instance.getId()
                    + ". Error writing to database",
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Deactivated Instance with id: {}", instanceEntity.getId());

        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MODEL + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ_MAPPING, ex);
        }
    }

    @Transactional
    public void deleteInstance(Long instanceId) throws InstanceServiceException
    {
        log.info("Attempting to delete Instance with id: {}", instanceId);

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstanceEntity(instanceId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_DELETE + instanceId + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_DELETE + instanceId
                    + ". Instance not found in database",
                InstanceServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        // TODO: Soft-delete all associated Maintainers, Characters, and
        // Character Groups.

        // Attempt to soft-delete instance.
        instanceEntity.setDeleted(false);
        instanceEntity.setDeleteDate(LocalDateTime.now());

        try
        {
            instanceRepository.save(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_DEACTIVATE + instanceId
                    + ". Error writing to database",
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Deleted Instance with id: {}", instanceId);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // General methods, not to be used with business layer.
    // -----------------------------------------------------------------------------------------------------------------

    protected Instance getInstanceEntity(Long id) throws Exception
    {
        try
        {
            Instance instance = instanceRepository.findById(id).orElse(null);

            return (instance == null || instance.getDeleted()) ? null
                : instance;
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    protected Instance getInstanceEntity(InstanceDto instanceDto)
        throws Exception
    {
        return getInstanceEntity(instanceDto.getId());
    }

    private Boolean instanceExists(String subdomainName)
    {
        log.debug("Searching for existance of Instance with subdomain name: {}",
            subdomainName);

        Instance probe = new Instance();
        probe.setSubdomainName(subdomainName);
        probe.setDeleted(false);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase()
            .withIgnorePaths("deleted");

        return instanceRepository.exists(Example.of(probe, matcher));
    }

    // -----------------------------------------------------------------------------------------------------------------
    // DTO Mapping methods.
    // -----------------------------------------------------------------------------------------------------------------

    protected InstanceDto getInstanceDto(Instance instance) throws Exception
    {
        return getInstanceDto(instance, false);
    }

    protected InstanceDto getInstanceDto(Instance instance, Boolean skipNested)
        throws Exception
    {
        InstanceDto dto = new InstanceDto();

        dto.setId(instance.getId());

        dto.setSubdomainName(instance.getSubdomainName());
        dto.setDisplayName(instance.getDisplayName());
        dto.setDescription(instance.getDescription());
        dto.setActive(instance.getActive());

        dto.setCreateDate(instance.getCreateDate());
        dto.setLastActivateDate(instance.getLastActivateDate());
        dto.setLastDeactivateDate(instance.getLastDeactivateDate());

        if (!skipNested)
        {
            dto.setOwner(instance.getOwner() != null
                ? userAccountService.getOwnerDto(instance.getOwner(), true)
                : null);

            dto.setMaintainers(Optional.ofNullable(instance.getMaintainers())
                .map(Set::stream).orElseGet(Stream::empty).map(m -> {
                    try
                    {
                        return userAccountService.getMaintainerDto(m, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet()));

            dto.setCharacters(Optional.ofNullable(instance.getCharacters())
                .map(Set::stream).orElseGet(Stream::empty).map(c -> {
                    try
                    {
                        return characterService.getCharacterDto(c, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet()));

            dto.setCharacterGroups(
                Optional.ofNullable(instance.getCharacterGroups())
                    .map(Set::stream).orElseGet(Stream::empty).map(cg -> {
                        try
                        {
                            return characterService.getCharacterGroupDto(cg,
                                true);
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

    private InstanceServiceException generateException(String message,
        InstanceServiceException.Codes code)
    {
        log.error(message);

        return new InstanceServiceException(code, message);
    }

    private InstanceServiceException generateException(String message,
        InstanceServiceException.Codes code, Exception ex)
    {
        log.error(message, ex);

        return new InstanceServiceException(code, message, ex);
    }
}
