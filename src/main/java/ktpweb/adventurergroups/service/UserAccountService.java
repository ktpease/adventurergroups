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

import ktpweb.adventurergroups.entity.Character;
import ktpweb.adventurergroups.entity.Instance;
import ktpweb.adventurergroups.entity.UserAccount;
import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.MaintainerDto;
import ktpweb.adventurergroups.model.OwnerDto;
import ktpweb.adventurergroups.model.UserAccountDto;
import ktpweb.adventurergroups.repository.UserAccountRepository;
import ktpweb.adventurergroups.util.UserAccountUtils.UserAccountRoles;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserAccountService
{
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private CharacterService characterService;

    // -----------------------------------------------------------------------------------------------------------------
    // Owner-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String EXCEPTION_OWNER_CREATE = "Cannot create Owner account with username: ";
    private final String EXCEPTION_OWNER_RETRIEVE = "Cannot retrieve Owner account with user id: ";
    private final String EXCEPTION_OWNER_UPDATE = "Cannot update Owner account with user id: ";
    private final String EXCEPTION_OWNER_DELETE = "Cannot delete Owner account with user id: ";

    private final String EXCEPTION_OWNER_MODEL = "Cannot return model for Owner account with user id: ";

    @Transactional
    public OwnerDto createOwner(String username, String password, String email,
        String displayname) throws UserAccountServiceException
    {
        log.info("Attempting to create Owner account with username: {}",
            username);

        // Invalid username.
        if (!StringUtils.hasText(username))
        {
            throw generateException(
                EXCEPTION_OWNER_CREATE + username + ". Invalid username",
                UserAccountServiceException.Codes.INVALID_USERNAME);
        }

        // Invalid password.
        if (!StringUtils.hasText(password))
        {
            throw generateException(
                EXCEPTION_OWNER_CREATE + username + ". Invalid password",
                UserAccountServiceException.Codes.INVALID_PASSWORD);
        }

        // Check to see if an owner with the same username and email exists.
        Boolean accountExists;

        try
        {
            accountExists = accountExistsInGlobal(username, email);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_CREATE + username
                    + ". Error checking for matching accounts from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountExists)
        {
            throw generateException(
                "Account with the same username or non-null email already exists! Username = "
                    + username + ", email = " + email,
                UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
        }

        // Generate database entity.
        UserAccount accountEntity = new UserAccount();

        accountEntity.setUsername(username);
        accountEntity.setPassword(password);
        accountEntity.setEmail(email);
        accountEntity.setDisplayname(
            StringUtils.hasText(displayname) ? displayname : username);
        accountEntity.setRole(UserAccountRoles.USER_ROLE_OWNER);
        accountEntity.setCreateDate(LocalDateTime.now());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_CREATE + username
                    + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Owner account with username: {}, id: {}", username,
            accountEntity.getId());

        try
        {
            Hibernate.initialize(accountEntity.getInstances());

            return getOwnerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public OwnerDto retrieveOwner(Long userId)
        throws UserAccountServiceException
    {
        UserAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getUserAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_RETRIEVE + userId
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_OWNER_RETRIEVE + userId + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_OWNER)
        {
            throw generateException(
                EXCEPTION_OWNER_RETRIEVE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        log.info("Found Owner account with id: {}", accountEntity.getId());

        // Prepare collection objects and return a full DTO.
        try
        {
            Hibernate.initialize(accountEntity.getInstances());

            return getOwnerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public OwnerDto updateOwner(OwnerDto owner)
        throws UserAccountServiceException
    {
        if (owner == null)
        {
            throw generateException("Attempted update of a null Owner account",
                UserAccountServiceException.Codes.NULL_ACCOUNT_OBJECT);
        }

        UserAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getUserAccountEntity(owner.getId());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + owner.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + owner.getId()
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_OWNER)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + owner.getId() + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // Attempt to modify and save user account.
        accountEntity.setUsername(owner.getUsername());
        accountEntity.setEmail(owner.getEmail());
        accountEntity.setDisplayname(owner.getDisplayname());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + owner.getId()
                    + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Owner account with id: {}", owner.getId());

        try
        {
            Hibernate.initialize(accountEntity.getInstances());

            return getOwnerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public void deleteOwner(Long userId) throws UserAccountServiceException
    {
        UserAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getUserAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_DELETE + userId
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_OWNER_DELETE + userId + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_OWNER)
        {
            throw generateException(
                EXCEPTION_OWNER_DELETE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // TODO: Soft-delete all associated Instances, Characters, and
        // Character Groups.

        // Attempt to soft-delete user account.
        accountEntity.setDeleted(true);
        accountEntity.setDeleteDate(LocalDateTime.now());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_DELETE + userId + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Deleted Owner account with id: {}", userId);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Maintainer-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String EXCEPTION_MAINTAINER_CREATE_FOR_INSTANCE = "Cannot create transient Maintainer account on Instance id: ";
    private final String EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER = "Cannot create transient Maintainer account for Character id: ";
    private final String EXCEPTION_MAINTAINER_REGISTER = "Cannot register transient Maintainer account with user id: ";
    private final String EXCEPTION_MAINTAINER_RETRIEVE = "Cannot retrieve Maintainer account with user id: ";
    private final String EXCEPTION_MAINTAINER_UPDATE = "Cannot update Maintainer account with user id: ";
    private final String EXCEPTION_MAINTAINER_DELETE = "Cannot delete Maintainer account with user id: ";

    private final String EXCEPTION_MAINTAINER_MODEL = "Cannot return model for Maintainer account with user id: ";

    @Transactional
    public MaintainerDto createTransientMaintainer(InstanceDto parentInstance)
        throws UserAccountServiceException
    {
        if (parentInstance == null)
        {
            throw generateException(
                "Attempted creation of a transient Maintainer account on a null Instance",
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        log.info(
            "Attempting to create transient Maintainer account on instance id: {}",
            parentInstance.getId());

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstanceEntity(parentInstance);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_INSTANCE
                    + parentInstance.getId()
                    + ". Error reading Instance from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_INSTANCE
                    + parentInstance.getId() + ". Instance not found",
                UserAccountServiceException.Codes.INSTANCE_NOT_FOUND);
        }

        // Generate database entity.
        UserAccount accountEntity = new UserAccount();

        accountEntity.setRole(UserAccountRoles.USER_ROLE_TRANSIENT);
        accountEntity.setInviteToken("newrandomtoken");
        accountEntity.setParentInstance(instanceEntity);
        accountEntity.setCreateDate(LocalDateTime.now());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_INSTANCE
                    + parentInstance.getId() + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created transient Maintainer account with id: {}",
            accountEntity.getId());

        // Update inverse side of Maintainer-Instance relationship because
        // it doesn't automatically do that?!?!
        Set<UserAccount> maintainerList = instanceEntity.getMaintainers();

        if (maintainerList == null)
            maintainerList = new HashSet<UserAccount>();

        maintainerList.add(accountEntity);

        instanceEntity.setMaintainers(maintainerList);

        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public MaintainerDto createTransientMaintainer(CharacterDto character)
        throws UserAccountServiceException
    {
        if (character == null)
        {
            throw generateException(
                "Attempted creation of a transient Maintainer acount on a null Character",
                UserAccountServiceException.Codes.INVALID_CHARACTER_OBJECT);
        }

        log.info(
            "Attempting to create transient Maintainer account for Character id: {}",
            character.getId());

        Character characterEntity;

        try
        {
            characterEntity = characterService.getCharacterEntity(character);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER + character.getId()
                    + ". Error reading Character from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (characterEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER + character.getId()
                    + ". Character not found",
                UserAccountServiceException.Codes.CHARACTER_NOT_FOUND);
        }

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService
                .getInstanceEntity(character.getInstanceId());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER + character.getId()
                    + ". Error reading Instance from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER + character.getId()
                    + ". Invalid instance object",
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        // Generate database entity.
        UserAccount accountEntity = new UserAccount();

        accountEntity.setRole(UserAccountRoles.USER_ROLE_TRANSIENT);
        // TODO: Generate randomized token.
        accountEntity.setInviteToken("newrandomtoken");
        accountEntity.setParentInstance(instanceEntity);
        accountEntity.setCharacters(Stream.of(characterEntity)
            .collect(Collectors.toCollection(HashSet::new)));
        accountEntity.setCreateDate(LocalDateTime.now());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER + character.getId()
                    + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created transient Maintainer account with id: {}",
            accountEntity.getId());

        // Update inverse side of Maintainer-Instance relationship because
        // it doesn't automatically do that?!?!
        Set<UserAccount> maintainerList = instanceEntity.getMaintainers();

        if (maintainerList == null)
            maintainerList = new HashSet<UserAccount>();

        maintainerList.add(accountEntity);

        instanceEntity.setMaintainers(maintainerList);

        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_MODEL + character.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public MaintainerDto registerMaintainer(MaintainerDto transientMaintainer,
        String username, String password, String email, String displayname)
        throws UserAccountServiceException
    {
        if (transientMaintainer == null)
        {
            throw generateException(
                "Attempted registration of a null transient Maintainer account",
                UserAccountServiceException.Codes.NULL_ACCOUNT_OBJECT);
        }

        log.info(
            "Attempting to register transient Maintainer account id: {} with username: {}",
            transientMaintainer.getId(), username);

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService
                .getInstanceEntity(transientMaintainer.getParentInstanceId());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Error reading Instance from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Invalid Instance object",
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        // Invalid username.
        if (!StringUtils.hasText(username))
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Invalid username",
                UserAccountServiceException.Codes.INVALID_USERNAME);
        }

        // Invalid password.
        if (!StringUtils.hasText(password))
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Invalid password",
                UserAccountServiceException.Codes.INVALID_PASSWORD);
        }

        // Check to see if an account with the same username and email
        // exists.

        // Check for Owner.
        UserAccount instanceOwnerEntity = instanceEntity.getOwner();
        if (instanceOwnerEntity.getUsername().equalsIgnoreCase(username)
            || (StringUtils.hasText(email)
                && instanceOwnerEntity.getEmail().equalsIgnoreCase(email)))
        {
            throw generateException(
                "Owner account with the same username or non-null email already exists! Username = "
                    + username + ", email = " + email,
                UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
        }

        // Check for other maintainers.
        Boolean accountExists;

        try
        {
            accountExists = accountExistsInInstance(username, email,
                instanceEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Error checking for matching accounts from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountExists)
        {
            throw generateException(
                "Maintainer account with the same username or non-null email already exists! Username = "
                    + username + ", email = " + email,
                UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
        }

        // Read user account from database.
        UserAccount accountEntity;

        try
        {
            accountEntity = getUserAccountEntity(transientMaintainer);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if we're editing the correct account.
        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_TRANSIENT)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // Attempt to modify and save user account.
        accountEntity.setUsername(username);
        accountEntity.setPassword(password);
        accountEntity.setEmail(email);
        accountEntity.setDisplayname(
            StringUtils.hasText(displayname) ? displayname : username);
        accountEntity.setRole(UserAccountRoles.USER_ROLE_MAINTAINER);
        accountEntity.setCreateDate(LocalDateTime.now());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_REGISTER + transientMaintainer.getId()
                    + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info(
            "Registered transient Maintainer account id: {} with username: {}",
            accountEntity.getId(), accountEntity.getUsername());

        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_MODEL + transientMaintainer.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public MaintainerDto retrieveMaintainer(Long userId)
        throws UserAccountServiceException
    {
        UserAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getUserAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_RETRIEVE + userId
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_RETRIEVE + userId
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_MAINTAINER
            && accountEntity.getRole() != UserAccountRoles.USER_ROLE_TRANSIENT)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_RETRIEVE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        log.info("Found Maintainer account with id: {}", accountEntity.getId());

        // Prepare collection objects and return a full DTO.
        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public MaintainerDto updateMaintainer(MaintainerDto maintainer)
        throws UserAccountServiceException
    {
        if (maintainer == null)
        {
            throw generateException(
                "Attempted update of a null Maintainer account",
                UserAccountServiceException.Codes.NULL_ACCOUNT_OBJECT);
        }

        UserAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getUserAccountEntity(maintainer.getId());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + maintainer.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + maintainer.getId()
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_MAINTAINER)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + maintainer.getId()
                    + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // Attempt to modify and save user account.
        accountEntity.setUsername(maintainer.getUsername());
        accountEntity.setEmail(maintainer.getEmail());
        accountEntity.setDisplayname(maintainer.getDisplayname());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + maintainer.getId()
                    + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Maintainer account with id: {}", maintainer.getId());

        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public void deleteMaintainer(Long userId) throws UserAccountServiceException
    {
        UserAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getUserAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_DELETE + userId
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        // Check if the account exists.
        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_DELETE + userId
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_MAINTAINER
            || accountEntity.getRole() != UserAccountRoles.USER_ROLE_TRANSIENT)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_DELETE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // TODO: Unassign all associated Characters.

        // Attempt to soft-delete user account.
        accountEntity.setDeleted(true);
        accountEntity.setDeleteDate(LocalDateTime.now());

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_DELETE + userId
                    + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Deleted Maintainer account with id: {}", userId);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // General User Account methods, not to be used with business layer.
    // -----------------------------------------------------------------------------------------------------------------

    protected UserAccount getUserAccountEntity(Long id) throws Exception
    {
        try
        {
            UserAccount userAccount = userAccountRepository.findById(id)
                .orElse(null);

            return (userAccount == null || userAccount.getDeleted()) ? null
                : userAccount;
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    protected UserAccount getUserAccountEntity(UserAccountDto userAccountDto)
        throws Exception
    {
        return getUserAccountEntity(userAccountDto.getId());
    }

    private Boolean accountExistsInGlobal(String username, String email)
        throws Exception
    {
        log.debug(
            "Searching for existance of Global user account with username: {} and/or email: {}",
            username, email);

        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreCase();

        UserAccount probe = new UserAccount();
        probe.setUsername(username);
        probe.setDeleted(false);
        probe.setRole(UserAccountRoles.USER_ROLE_OWNER);

        Boolean check = userAccountRepository
            .exists(Example.of(probe, matcher));

        if (check)
            return true;

        if (StringUtils.hasText(email))
        {
            probe.setUsername(null);
            probe.setEmail(email);

            check = userAccountRepository.exists(Example.of(probe, matcher));
        }

        return check;
    }

    private Boolean accountExistsInInstance(String username, String email,
        Instance parentInstance) throws Exception
    {
        log.debug(
            "Searching for existance of Maintainer user account with username: {} and/or email: {} in instance id: {}",
            username, email, parentInstance.getId());

        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreCase();

        UserAccount probe = new UserAccount();
        probe.setUsername(username);
        probe.setParentInstance(parentInstance);
        probe.setRole(UserAccountRoles.USER_ROLE_MAINTAINER);
        probe.setDeleted(false);

        Boolean check = userAccountRepository
            .exists(Example.of(probe, matcher));

        if (check)
            return true;

        if (StringUtils.hasText(email))
        {
            probe.setUsername(null);
            probe.setEmail(email);

            // Now check email.
            return userAccountRepository.exists(Example.of(probe, matcher));
        }

        return false;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // DTO Mapping methods.
    // -----------------------------------------------------------------------------------------------------------------

    protected OwnerDto getOwnerDto(UserAccount ua) throws Exception
    {
        OwnerDto dto = new OwnerDto();

        dto.setId(ua.getId());
        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());
        dto.setInstanceIds(Optional.ofNullable(ua.getInstances())
            .map(Set::stream).orElseGet(Stream::empty).map(i -> i.getId())
            .collect(Collectors.toSet()));

        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }

    protected MaintainerDto getMaintainerDto(UserAccount ua) throws Exception
    {
        MaintainerDto dto = new MaintainerDto();

        dto.setId(ua.getId());
        dto.setParentInstanceId(
            ua.getParentInstance() != null ? ua.getParentInstance().getId()
                : null);
        dto.setCharacterIds(Optional.ofNullable(ua.getCharacters())
            .map(Set::stream).orElseGet(Stream::empty).map(c -> c.getId())
            .collect(Collectors.toSet()));
        dto.setCreateDate(ua.getCreateDate());

        if (ua.getRole() == UserAccountRoles.USER_ROLE_TRANSIENT)
        {
            dto.setIsTransient(true);
            dto.setInviteToken(ua.getInviteToken());
        }
        else
        {
            dto.setIsTransient(false);
            dto.setUsername(ua.getUsername());
            dto.setEmail(ua.getEmail());
            dto.setDisplayname(ua.getDisplayname());
        }

        return dto;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Exception handling methods.
    // -----------------------------------------------------------------------------------------------------------------

    private UserAccountServiceException generateException(String message,
        UserAccountServiceException.Codes code)
    {
        log.error(message);

        return new UserAccountServiceException(code, message);
    }

    private UserAccountServiceException generateException(String message,
        UserAccountServiceException.Codes code, Exception ex)
    {
        log.error(message, ex);

        return new UserAccountServiceException(code, message, ex);
    }
}
