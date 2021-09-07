package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import ktpweb.adventurergroups.entity.Character;
import ktpweb.adventurergroups.entity.CharacterGroup;
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

    @Autowired
    private Hashids tokenHashids;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // -----------------------------------------------------------------------------------------------------------------
    // Owner-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String EXCEPTION_OWNER_CREATE = "Cannot create Owner account with username: ";
    private final String EXCEPTION_OWNER_RETRIEVE = "Cannot retrieve Owner account with user id: ";
    private final String EXCEPTION_OWNER_UPDATE = "Cannot update Owner account with user id: ";
    private final String EXCEPTION_OWNER_DELETE = "Cannot delete Owner account with user id: ";

    private final String EXCEPTION_OWNER_MODEL = "Cannot return model for Owner account with user id: ";

    @Transactional
    public OwnerDto createOwner(String username, String password, String email)
        throws UserAccountServiceException
    {
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
        accountEntity.setPassword(passwordEncoder.encode(password));
        accountEntity.setEmail(email);
        accountEntity.setDisplayname(username);
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

    public OwnerDto createOwner(UserAccountDto newAccount)
        throws UserAccountServiceException
    {
        if (newAccount == null)
        {
            throw generateException(
                "Attempted creation of an Owner account with a null object",
                UserAccountServiceException.Codes.NULL_ACCOUNT_OBJECT);
        }

        return createOwner(newAccount.getUsername(), newAccount.getPassword(),
            newAccount.getEmail());
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

        // Return full DTO.
        try
        {
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
    public OwnerDto updateOwner(Long userId, UserAccountDto accountUpdate)
        throws UserAccountServiceException
    {
        // Attempt to read from the database.
        if (accountUpdate == null)
        {
            throw generateException("Attempted update of a null Owner account",
                UserAccountServiceException.Codes.NULL_ACCOUNT_OBJECT);
        }

        UserAccount accountEntity;

        try
        {
            accountEntity = getUserAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + userId
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + userId + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_OWNER)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // Updating username and/or email, check for collisions!
        if (accountUpdate.getUsername() != null
            || accountUpdate.getEmail() != null)
        {
            if (accountUpdate.getUsername() != null
                && !StringUtils.hasText(accountUpdate.getUsername()))
            {
                throw generateException(
                    EXCEPTION_OWNER_UPDATE + userId + ". Invalid username",
                    UserAccountServiceException.Codes.INVALID_USERNAME);
            }

            if (accountUpdate.getEmail() != null
                && !StringUtils.hasText(accountUpdate.getEmail()))
            {
                throw generateException(
                    EXCEPTION_OWNER_UPDATE + userId + ". Invalid email",
                    UserAccountServiceException.Codes.INVALID_EMAIL);
            }

            Boolean accountExists;

            // Check for owner.
            try
            {
                accountExists = accountExistsInGlobal(
                    StringUtils.hasText(accountUpdate.getUsername())
                        ? accountUpdate.getUsername()
                        : accountEntity.getUsername(),
                    StringUtils.hasText(accountUpdate.getEmail())
                        ? accountUpdate.getEmail()
                        : accountEntity.getEmail());
            }
            catch (Exception ex)
            {
                throw generateException(EXCEPTION_OWNER_UPDATE + userId
                    + ". Error checking for matching accounts from database",
                    UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
            }

            if (accountExists)
            {
                throw generateException(
                    "Another owner with the same username or non-null email already exists! Username = "
                        + (StringUtils.hasText(accountUpdate.getUsername())
                            ? accountUpdate.getUsername()
                            : accountEntity.getUsername())
                        + ", email = "
                        + (StringUtils.hasText(accountUpdate.getEmail())
                            ? accountUpdate.getEmail()
                            : accountEntity.getEmail()),
                    UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
            }

            // Check for each instance's maintainers
            for (Instance i : accountEntity.getInstances())
            {
                try
                {
                    accountExists = accountExistsInInstance(
                        StringUtils.hasText(accountUpdate.getUsername())
                            ? accountUpdate.getUsername()
                            : accountEntity.getUsername(),
                        StringUtils.hasText(accountUpdate.getEmail())
                            ? accountUpdate.getEmail()
                            : accountEntity.getEmail(),
                        i);
                }
                catch (Exception ex)
                {
                    throw generateException(EXCEPTION_OWNER_UPDATE + userId
                        + ". Error checking for matching accounts from database",
                        UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                        ex);
                }

                if (accountExists)
                {
                    throw generateException(
                        "Another maintainer with the same username or non-null email already exists! Username = "
                            + (StringUtils.hasText(accountUpdate.getUsername())
                                ? accountUpdate.getUsername()
                                : accountEntity.getUsername())
                            + ", email = "
                            + (StringUtils.hasText(accountUpdate.getEmail())
                                ? accountUpdate.getEmail()
                                : accountEntity.getEmail()),
                        UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
                }
            }
        }

        // Attempt to modify and save user account.

        if (accountUpdate.getUsername() != null)
        {
            accountEntity.setUsername(accountUpdate.getUsername());
        }
        if (accountUpdate.getPassword() != null)
        {
            if (!StringUtils.hasText(accountUpdate.getPassword()))
            {
                throw generateException(
                    EXCEPTION_OWNER_UPDATE + userId + ". Invalid password",
                    UserAccountServiceException.Codes.INVALID_PASSWORD);
            }

            accountEntity.setPassword(
                passwordEncoder.encode(accountUpdate.getPassword()));
        }
        if (accountUpdate.getEmail() != null)
        {
            accountEntity.setEmail(accountUpdate.getEmail());
        }
        if (accountUpdate.getDisplayname() != null)
        {
            accountEntity.setDisplayname(accountUpdate.getDisplayname());
        }

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + userId + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Owner account with id: {}", userId);

        // Return full DTO.
        try
        {
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
        // Attempt to read from the database.
        UserAccount accountEntity;

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

        // Soft-delete all associated Instances, Maintainers, Characters, and
        // Character Groups.
        for (Instance i : accountEntity.getInstances())
        {
            for (UserAccount m : i.getMaintainers())
            {
                m.setDeleted(true);
                m.setDeleteDate(LocalDateTime.now());
            }

            for (Character c : i.getCharacters())
            {
                c.setDeleted(true);
                c.setDeleteDate(LocalDateTime.now());
            }

            for (CharacterGroup cg : i.getCharacterGroups())
            {
                cg.setDeleted(true);
                cg.setDeleteDate(LocalDateTime.now());
            }

            i.setDeleted(true);
            i.setDeleteDate(LocalDateTime.now());
        }

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

    private final String EXCEPTION_MAINTAINER_CREATE_FOR_INSTANCE = "Cannot create Maintainer account on Instance id: ";
    private final String EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER = "Cannot create Maintainer account for Character id: ";
    private final String EXCEPTION_MAINTAINER_RETRIEVE = "Cannot retrieve Maintainer account with user id: ";
    private final String EXCEPTION_MAINTAINER_UPDATE = "Cannot update Maintainer account with user id: ";
    private final String EXCEPTION_MAINTAINER_DELETE = "Cannot delete Maintainer account with user id: ";

    private final String EXCEPTION_MAINTAINER_MODEL = "Cannot return model for Maintainer account with user id: ";

    @Transactional
    public MaintainerDto createUnregisteredMaintainer(
        InstanceDto parentInstance) throws UserAccountServiceException
    {
        // Load and validate Instance.
        if (parentInstance == null)
        {
            throw generateException(
                "Attempted creation of an unregistered Maintainer account on a null Instance",
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

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

        // Generate and save database entity, first pass.
        UserAccount accountEntity = new UserAccount();

        accountEntity.setRole(UserAccountRoles.USER_ROLE_UNREGISTERED);
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

        // Use ID generated by first pass to create invite token, then re-save.
        accountEntity
            .setInviteToken(tokenHashids.encode(accountEntity.getId()));

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_INSTANCE
                    + parentInstance.getId() + ". Error writing to database. Unregistered maintainer without token!",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created unregistered Maintainer account with id: {}",
            accountEntity.getId());

        // Return full DTO.
        try
        {
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
    public MaintainerDto createUnregisteredMaintainer(CharacterDto character)
        throws UserAccountServiceException
    {
        // Load and validate Character and its Instance.
        if (character == null)
        {
            throw generateException(
                "Attempted creation of an unregistered Maintainer acount on a null Character",
                UserAccountServiceException.Codes.INVALID_CHARACTER_OBJECT);
        }

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
                .getInstanceEntity(character.getInstance());
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

        // Generate and save database entity, first pass.
        UserAccount accountEntity = new UserAccount();

        accountEntity.setRole(UserAccountRoles.USER_ROLE_UNREGISTERED);
        accountEntity.setParentInstance(instanceEntity);
        accountEntity.setCreateDate(LocalDateTime.now());

        accountEntity.setCharacters(Stream.of(characterEntity)
            .collect(Collectors.toCollection(HashSet::new)));

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

        // Use ID generated by first pass to create invite token, then re-save.
        accountEntity
            .setInviteToken(tokenHashids.encode(accountEntity.getId()));

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_CREATE_FOR_CHARACTER + character.getId()
                    + ". Error writing to database. Unregistered maintainer without token!",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created unregistered Maintainer account with id: {}",
            accountEntity.getId());

        // Return full DTO.
        try
        {
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
    public MaintainerDto retrieveMaintainer(Long userId)
        throws UserAccountServiceException
    {
        // Attempt to read from the database.
        UserAccount accountEntity;

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

        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_RETRIEVE + userId
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_MAINTAINER
            && accountEntity
                .getRole() != UserAccountRoles.USER_ROLE_UNREGISTERED)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_RETRIEVE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // Return full DTO.
        try
        {
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
    public MaintainerDto registerOrUpdateMaintainer(Long userId,
        UserAccountDto maintainerUpdate) throws UserAccountServiceException
    {
        if (maintainerUpdate == null)
        {
            throw generateException(
                "Attempted update of a null Maintainer account",
                UserAccountServiceException.Codes.NULL_ACCOUNT_OBJECT);
        }

        UserAccount accountEntity;

        try
        {
            accountEntity = getUserAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + userId
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + userId
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_MAINTAINER
            && accountEntity
                .getRole() != UserAccountRoles.USER_ROLE_UNREGISTERED)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService
                .getInstanceEntity(accountEntity.getParentInstance().getId());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + userId
                    + ". Error reading Instance from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (instanceEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + userId
                    + ". Invalid Instance object",
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT);
        }

        // Check if we're registering and do additional validation checks.
        Boolean isRegistering = accountEntity
            .getRole() == UserAccountRoles.USER_ROLE_UNREGISTERED;

        if (isRegistering)
        {
            if (maintainerUpdate.getUsername() == null
                || maintainerUpdate.getPassword() == null)
            {
                throw generateException(EXCEPTION_MAINTAINER_UPDATE + userId
                    + ". Required username and password fields for unregistered maintainer.",
                    UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT);
            }
        }

        // If we're updating username or email, check to see if it's valid and
        // an account with the same username and email does not already exist.
        if (maintainerUpdate.getUsername() != null
            && !StringUtils.hasText(maintainerUpdate.getUsername()))
        {
            throw generateException(
                EXCEPTION_OWNER_UPDATE + userId + ". Invalid username",
                UserAccountServiceException.Codes.INVALID_USERNAME);
        }

        if (StringUtils.hasText(maintainerUpdate.getUsername())
            || StringUtils.hasText(maintainerUpdate.getEmail()))
        {
            // Check if it matches the instance's Owner.
            UserAccount instanceOwnerEntity = instanceEntity.getOwner();

            // Username
            if (StringUtils.hasText(maintainerUpdate.getUsername()))
            {
                if (instanceOwnerEntity.getUsername()
                    .equalsIgnoreCase(maintainerUpdate.getUsername()))
                {
                    throw generateException(
                        "Owner account with the same username already exists! Username = "
                            + instanceOwnerEntity.getUsername(),
                        UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
                }
            }

            // Email (if updated)
            if (StringUtils.hasText(maintainerUpdate.getEmail()))
            {
                if (instanceOwnerEntity.getEmail()
                    .equalsIgnoreCase(maintainerUpdate.getEmail()))
                {
                    throw generateException(
                        "Owner account with the same email already exists! Email = "
                            + instanceOwnerEntity.getEmail(),
                        UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
                }
            }
            // Email (if not updated)
            else if (StringUtils.hasText(accountEntity.getEmail())
                && instanceOwnerEntity.getEmail()
                    .equalsIgnoreCase(accountEntity.getEmail()))
            {
                throw generateException(
                    "Owner account with the same email already exists! Email = "
                        + instanceOwnerEntity.getEmail(),
                    UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
            }

            // Check if it matches other maintainers.
            Boolean accountExists;

            try
            {
                accountExists = accountExistsInInstance(
                    maintainerUpdate.getUsername(),
                    StringUtils.hasText(maintainerUpdate.getEmail())
                        ? maintainerUpdate.getEmail()
                        : accountEntity.getEmail(),
                    instanceEntity);
            }
            catch (Exception ex)
            {
                throw generateException(EXCEPTION_MAINTAINER_UPDATE + userId
                    + ". Error checking for matching accounts from database",
                    UserAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
            }

            if (accountExists)
            {
                throw generateException(
                    "Maintainer account with the same username or non-null email already exists! Username = "
                        + maintainerUpdate.getUsername() + ", email = "
                        + (StringUtils.hasText(maintainerUpdate.getEmail())
                            ? maintainerUpdate.getEmail()
                            : accountEntity.getEmail()),
                    UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
            }
        }

        // Attempt to modify and save user account.
        if (maintainerUpdate.getUsername() != null)
        {
            accountEntity.setUsername(maintainerUpdate.getUsername());
        }

        if (maintainerUpdate.getPassword() != null)
        {
            if (!StringUtils.hasText(maintainerUpdate.getPassword()))
            {
                throw generateException(
                    EXCEPTION_OWNER_UPDATE + userId + ". Invalid password",
                    UserAccountServiceException.Codes.INVALID_PASSWORD);
            }

            accountEntity.setPassword(
                passwordEncoder.encode(maintainerUpdate.getPassword()));
        }

        if (maintainerUpdate.getEmail() != null)
        {
            accountEntity.setEmail(maintainerUpdate.getEmail());
        }

        if (maintainerUpdate.getDisplayname() != null)
        {
            accountEntity.setDisplayname(maintainerUpdate.getDisplayname());
        }
        else if (isRegistering)
        {
            accountEntity.setDisplayname(maintainerUpdate.getUsername());
        }

        // Registering account.
        if (isRegistering)
        {
            accountEntity.setRole(UserAccountRoles.USER_ROLE_MAINTAINER);
        }

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_UPDATE + userId
                    + ". Error writing to database",
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        if (isRegistering)
        {
            log.info("Registered Maintainer account id: {} with username: {}",
                userId, accountEntity.getUsername());
        }
        else
        {
            log.info("Updated Maintainer account id: {}", userId);
        }

        try
        {
            return getMaintainerDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_MODEL + userId
                    + ". Error reading from database",
                UserAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public void deleteMaintainer(Long userId) throws UserAccountServiceException
    {
        // Attempt to read from the database.
        UserAccount accountEntity;

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

        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_DELETE + userId
                    + ". User account not found",
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Check if the account is in the correct role.
        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_MAINTAINER
            || accountEntity
                .getRole() != UserAccountRoles.USER_ROLE_UNREGISTERED)
        {
            throw generateException(
                EXCEPTION_MAINTAINER_DELETE + userId + ". Invalid role",
                UserAccountServiceException.Codes.INVALID_ROLE);
        }

        // Unassign all of the group's characters, if there are any.
        for (Character c : accountEntity.getCharacters())
        {
            c.setMaintainer(null);
        }

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

    protected UserAccount getUserAccountEntity(OwnerDto ownerDto)
        throws Exception
    {
        return getUserAccountEntity(ownerDto.getId());
    }

    protected UserAccount getUserAccountEntity(MaintainerDto maintainerDto)
        throws Exception
    {
        return getUserAccountEntity(maintainerDto.getId());
    }

    private Boolean accountExistsInGlobal(String username, String email)
        throws Exception
    {
        log.debug(
            "Searching for existance of Owner user account with username: {} and/or email: {}",
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

    protected UserAccount getUserAccountEntityFromInviteToken(String token,
        Instance instance) throws Exception
    {
        try
        {
            UserAccount userAccount = userAccountRepository
                .findByInviteToken(token).orElse(null);

            return (userAccount == null || userAccount.getDeleted()
                || userAccount
                    .getRole() != UserAccountRoles.USER_ROLE_UNREGISTERED
                || instance == null || userAccount.getParentInstance() == null
                || userAccount.getParentInstance().getId() != instance.getId())
                    ? null
                    : userAccount;
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // DTO Mapping methods.
    // -----------------------------------------------------------------------------------------------------------------

    protected OwnerDto getOwnerDto(UserAccount ua) throws Exception
    {
        return getOwnerDto(ua, false);
    }

    protected OwnerDto getOwnerDto(UserAccount ua, Boolean skipNested)
        throws Exception
    {
        OwnerDto dto = new OwnerDto();

        dto.setId(ua.getId());

        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());

        if (!skipNested)
        {
            dto.setInstances(Optional.ofNullable(ua.getInstances())
                .map(Set::stream).orElseGet(Stream::empty).map(i -> {
                    try
                    {
                        return instanceService.getInstanceDto(i, true);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet()));
        }

        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }

    protected MaintainerDto getMaintainerDto(UserAccount ua) throws Exception
    {
        return getMaintainerDto(ua, false);
    }

    protected MaintainerDto getMaintainerDto(UserAccount ua, Boolean skipNested)
        throws Exception
    {
        MaintainerDto dto = new MaintainerDto();

        dto.setId(ua.getId());

        dto.setCreateDate(ua.getCreateDate());

        if (ua.getRole() == UserAccountRoles.USER_ROLE_UNREGISTERED)
        {
            dto.setIsRegistered(false);
            dto.setInviteToken(ua.getInviteToken());
        }
        else
        {
            dto.setIsRegistered(true);
            dto.setUsername(ua.getUsername());
            dto.setEmail(ua.getEmail());
            dto.setDisplayname(ua.getDisplayname());
        }

        if (!skipNested)
        {
            dto.setInstance(ua.getParentInstance() != null
                ? instanceService.getInstanceDto(ua.getParentInstance(), true)
                : null);

            dto.setCharacters(Optional.ofNullable(ua.getCharacters())
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
