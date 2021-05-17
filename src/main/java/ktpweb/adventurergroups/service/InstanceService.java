package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;
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

    private final String DEFAULT_NAME = "New Instance";

    private final String EXCEPTION_PRIMER_CREATE = "Cannot create Instance for owner User Account id: ";
    private final String EXCEPTION_PRIMER_ACTIVATE = "Cannot activate Instance with id: ";
    private final String EXCEPTION_PRIMER_DEACTIVATE = "Cannot deactivate Instance with id: ";

    private final String EXCEPTION_PRIMER_MODEL = "Cannot return model for Instance with id: ";

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
                EXCEPTION_PRIMER_CREATE + owner.getId()
                    + ". Error reading User Account from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (ownerEntity == null)
        {
            throw generateException(
                EXCEPTION_PRIMER_CREATE + owner.getId()
                    + ". User Account not found",
                InstanceServiceException.Codes.OWNER_NOT_FOUND);
        }

        if (ownerEntity.getRole() != UserAccountRoles.USER_ROLE_OWNER)
        {
            throw generateException(
                EXCEPTION_PRIMER_CREATE + owner.getId()
                    + ". User Account is not an owner role",
                InstanceServiceException.Codes.INVALID_OWNER_ROLE);
        }

        // Invalid subdomain name.
        if (!StringUtils.hasText(subdomainName))
        {
            throw generateException(
                EXCEPTION_PRIMER_CREATE + owner.getId()
                    + ". Invalid subdomain name",
                InstanceServiceException.Codes.INVALID_SUBDOMAINNAME);
        }

        // Check to see if an instance with the same subdomain name exists.
        if (instanceExists(subdomainName))
        {
            throw generateException(
                EXCEPTION_PRIMER_CREATE + owner.getId()
                    + ". Subdomain already exists",
                InstanceServiceException.Codes.INSTANCE_ALREADY_EXISTS);
        }

        // Generate database entity.
        Instance instanceEntity = new Instance();

        instanceEntity.setActive(false);
        instanceEntity.setOwner(ownerEntity);
        instanceEntity.setSubdomainName(subdomainName);
        instanceEntity.setDisplayName(DEFAULT_NAME);
        instanceEntity.setCreateDate(LocalDateTime.now());
        instanceEntity.setDeleted(false);

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_CREATE + owner.getId()
                    + ". Error writing to database",
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Instance with id: {}", instanceEntity.getId());

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
                EXCEPTION_PRIMER_MODEL + instanceEntity.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
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

        log.info("Attempting to activate Instance id: {}", instance.getId());

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_ACTIVATE + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_PRIMER_ACTIVATE + instance.getId()
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
                EXCEPTION_PRIMER_ACTIVATE + instance.getId()
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
                EXCEPTION_PRIMER_MODEL + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
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

        log.info("Attempting to deactivate Instance id: {}", instance.getId());

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstanceEntity(instance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_PRIMER_DEACTIVATE + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_PRIMER_DEACTIVATE + instance.getId()
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
                EXCEPTION_PRIMER_DEACTIVATE + instance.getId()
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
                EXCEPTION_PRIMER_MODEL + instance.getId()
                    + ". Error reading from database",
                InstanceServiceException.Codes.DATABASE_ERROR_READ, ex);
        }
    }

    protected Instance getInstanceEntity(Long id) throws Exception
    {
        try
        {
            Instance instance = instanceRepository.findById(id).orElse(null);

            return (instance != null && !instance.getDeleted()) ? instance
                : null;
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

    protected InstanceDto getInstanceDto(Instance instance) throws Exception
    {
        InstanceDto dto = new InstanceDto();

        dto.setId(instance.getId());
        dto.setSubdomainName(instance.getSubdomainName());
        dto.setDisplayName(instance.getDisplayName());
        dto.setDescription(instance.getDescription());
        dto.setOwnerId(instance.getOwner().getId());
        dto.setActive(instance.getActive());
        dto.setCreateDate(instance.getCreateDate());
        dto.setLastActivateDate(instance.getLastActivateDate());
        dto.setLastDeactivateDate(instance.getLastDeactivateDate());

        dto.setMaintainerIds(Optional.ofNullable(instance.getMaintainers())
            .map(Set::stream).orElseGet(Stream::empty).map(ua -> ua.getId())
            .collect(Collectors.toSet()));

        dto.setCharacterIds(Optional.ofNullable(instance.getCharacters())
            .map(Set::stream).orElseGet(Stream::empty).map(c -> c.getId())
            .collect(Collectors.toSet()));

        dto.setCharacterGroupIds(
            Optional.ofNullable(instance.getCharacterGroups()).map(Set::stream)
                .orElseGet(Stream::empty).map(cg -> cg.getId())
                .collect(Collectors.toSet()));

        return dto;
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
