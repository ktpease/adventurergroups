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

    private final String DEFAULT_INSTANCE = "New Instance";

    public Instance getInstance(Long id) throws Exception
    {
        try
        {
            return instanceRepository.findById(id).orElse(null);
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    public Instance getInstance(InstanceDto instanceDto) throws Exception
    {
        return getInstance(instanceDto.getId());
    }

    @Transactional
    public InstanceDto createInstance(OwnerDto owner, String subdomainName)
        throws InstanceServiceException
    {
        if (owner == null)
        {
            log.info("Attempted creation of an instance with a null Owner.");

            throw new InstanceServiceException(
                InstanceServiceException.Codes.INVALID_OWNER_OBJECT,
                "Attempted creation of an instance with a null Owner.");
        }

        log.info("Attempting to create Instance for owner account id {}",
            owner.getId());

        UserAccount ownerEntity;

        try
        {
            ownerEntity = userAccountService.getUserAccount(owner);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create Instance for owner account id {}. Error reading owner from database.",
                owner.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot create Instance for owner account id " + owner.getId()
                    + ". Error reading from database.",
                e);
        }

        if (ownerEntity == null)
        {
            log.info(
                "Cannot create Instance for owner account id {}. Owner not found.",
                owner.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.OWNER_NOT_FOUND,
                "Cannot create Instance for owner account id " + owner.getId()
                    + ". Owner not found.");
        }

        if (ownerEntity.getRole() != UserAccountRoles.USER_ROLE_OWNER)
        {
            log.info(
                "Cannot create Instance for owner account id {}. User account is not an owner role!",
                owner.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.INVALID_OWNER_ROLE,
                "Cannot create Instance for owner account id " + owner.getId()
                    + ". User account is not an owner role!");
        }

        // Incorrect subdomain name.
        if (!StringUtils.hasText(subdomainName))
        {
            log.info(
                "Cannot create Instance for owner account id {}. Invalid subdomain name",
                owner.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.INVALID_SUBDOMAINNAME,
                "Cannot create Instance for owner account id " + owner.getId()
                    + ". Invalid subdomain name");
        }

        // Check to see if an instance with the same subdomain name exists.
        if (instanceExists(subdomainName))
        {
            log.info("Account with subdomain '{}' already exists!",
                subdomainName);

            throw new InstanceServiceException(
                InstanceServiceException.Codes.INSTANCE_ALREADY_EXISTS,
                "Account with subdomain '" + subdomainName
                    + "' already exists!");
        }

        // Generate database entity.
        Instance instanceEntity = new Instance();

        instanceEntity.setActive(false);
        instanceEntity.setOwner(ownerEntity);
        instanceEntity.setSubdomainName(subdomainName);
        instanceEntity.setDisplayName(DEFAULT_INSTANCE);
        instanceEntity.setCreateDate(LocalDateTime.now());

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create Instance for owner account id {}. Error writing to database.",
                owner.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot create Instance for owner account id " + owner.getId()
                    + ". Error writing to database.",
                e);
        }

        log.info("Created Instance with id {}", instanceEntity.getId());

        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for instance id {}. Error reading from database.",
                instanceEntity.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for instance id " + instanceEntity.getId()
                    + ". Error reading from database.",
                e);
        }
    }

    @Transactional
    public InstanceDto activateInstance(InstanceDto instance)
        throws InstanceServiceException
    {
        if (instance == null)
        {
            log.info("Attempted activation of a null instance.");

            throw new InstanceServiceException(
                InstanceServiceException.Codes.NULL_INSTANCE_OBJECT,
                "Attempted activation of a null instance.");
        }

        log.info("Attempting to activate Instance id {}", instance.getId());

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstance(instance);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot activate Instance id {}. Error reading from database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot activate Instance id " + instance.getId()
                    + ". Error reading from database.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot activate Instance id {}. Instance not found in database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.INSTANCE_NOT_FOUND,
                "Cannot activate Instance id " + instance.getId()
                    + ". Instance not found in database.");
        }

        // Attempt to modify and save instance.
        instanceEntity.setActive(true);
        instanceEntity.setLastActivateDate(LocalDateTime.now());

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot activate Instance id {}. Error writing to database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot activate Instance id " + instance.getId()
                    + ". Error writing to database.",
                e);
        }

        log.info("Activated Instance with id {}", instanceEntity.getId());

        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for instance id {}. Error reading from database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for instance id " + instance.getId()
                    + ". Error reading from database.",
                e);
        }
    }

    @Transactional
    public InstanceDto deactivateInstance(InstanceDto instance)
        throws InstanceServiceException
    {
        if (instance == null)
        {
            log.info("Attempted deactivation of a null instance.");

            throw new InstanceServiceException(
                InstanceServiceException.Codes.NULL_INSTANCE_OBJECT,
                "Attempted deactivation of a null instance.");
        }

        log.info("Attempting to deactivate Instance id {}", instance.getId());

        // Read instance from database.
        Instance instanceEntity;

        try
        {
            instanceEntity = getInstance(instance);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot deactivate Instance id {}. Error reading from database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot deactivate Instance id " + instance.getId()
                    + ". Error reading from database.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot deactivate Instance id {}. Instance not found in database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.INSTANCE_NOT_FOUND,
                "Cannot deactivate Instance id " + instance.getId()
                    + ". Instance not found in database.");
        }

        // Attempt to modify and save instance.
        instanceEntity.setActive(false);
        instanceEntity.setLastDeactivateDate(LocalDateTime.now());

        try
        {
            instanceEntity = instanceRepository.save(instanceEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot deactivate Instance id {}. Error writing to database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot deactivate Instance id " + instance.getId()
                    + ". Error writing to database.",
                e);
        }

        log.info("Deactivated Instance with id {}", instanceEntity.getId());

        try
        {
            Hibernate.initialize(instanceEntity.getMaintainers());
            Hibernate.initialize(instanceEntity.getCharacters());
            Hibernate.initialize(instanceEntity.getCharacterGroups());

            return getInstanceDto(instanceEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for Instance id {}. Error reading from database.",
                instance.getId());

            throw new InstanceServiceException(
                InstanceServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for Instance id " + instance.getId()
                    + ". Error reading from database.",
                e);
        }
    }

    private Boolean instanceExists(String subdomainName)
    {
        log.debug("Searching for existance of Instance with subdomainName '{}'",
            subdomainName);

        Instance probe = new Instance();
        probe.setSubdomainName(subdomainName);
        probe.setDeleted(false);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase()
            .withIgnorePaths("deleted");

        return instanceRepository.exists(Example.of(probe, matcher));
    }

    public InstanceDto getInstanceDto(Instance instance) throws Exception
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
}
