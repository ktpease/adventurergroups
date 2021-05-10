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
import ktpweb.adventurergroups.model.AdminDto;
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

    protected UserAccount getUserAccount(Long id) throws Exception
    {
        try
        {
            UserAccount userAccount = userAccountRepository.findById(id)
                .orElse(null);

            return (userAccount != null && !userAccount.getDeleted())
                ? userAccount
                : null;
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    protected UserAccount getUserAccount(UserAccountDto userAccountDto)
        throws Exception
    {
        return getUserAccount(userAccountDto.getId());
    }

    @Transactional
    public AdminDto createAdmin(String username, String password, String email,
        String displayname) throws UserAccountServiceException
    {
        log.info("Attempting to create Admin account with username '{}'",
            username);

        // Incorrect username.
        if (!StringUtils.hasText(username))
        {
            log.info(
                "Cannot create Admin with username '{}'. Incorrect username.",
                username);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_USERNAME,
                "Cannot create Admin with username '" + username
                    + "'. Incorrect username.");
        }

        // Incorrect password.
        if (!StringUtils.hasText(password))
        {
            log.info(
                "Cannot create Admin with username '{}'. Incorrect password.",
                username);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_PASSWORD,
                "Cannot create Admin with username '" + username
                    + "'. Incorrect password.");
        }

        // Check to see if an admin or owner with the same username and password
        // exists.
        if (accountExistsInGlobal(username, email))
        {
            log.info(
                "Account with the same username or non-null email already exists! Username = {}, email = {}",
                username, email);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS,
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'");
        }

        // Generate database entity.
        UserAccount accountEntity = new UserAccount();

        accountEntity.setUsername(username);
        accountEntity.setPassword(password);
        accountEntity.setEmail(email);
        accountEntity.setDisplayname(
            StringUtils.hasText(displayname) ? displayname : username);
        accountEntity.setRole(UserAccountRoles.USER_ROLE_ADMIN);
        accountEntity.setCreateDate(LocalDateTime.now());
        accountEntity.setDeleted(false);

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create Admin with username {}. Error writing to database.",
                username);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot create Admin with username " + username
                    + ". Error writing to database.",
                e);
        }

        log.info("Created Admin account with username '{}', id {}", username,
            accountEntity.getId());

        try
        {
            return getAdminDto(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for user account id {}. Error reading from database.",
                accountEntity.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for user account id "
                    + accountEntity.getId() + ". Error reading from database.",
                e);
        }
    }

    @Transactional
    public OwnerDto createOwner(String username, String password, String email,
        String displayname) throws UserAccountServiceException
    {
        log.info("Attempting to create Owner account with username '{}'",
            username);

        // Incorrect username.
        if (!StringUtils.hasText(username))
        {
            log.info(
                "Cannot create Owner with username '{}'. Incorrect username.",
                username);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_USERNAME,
                "Cannot create Owner with username '" + username
                    + "'. Incorrect username.");
        }

        // Incorrect password.
        if (!StringUtils.hasText(password))
        {
            log.info(
                "Cannot create Owner with username '{}'. Incorrect password.",
                username);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_PASSWORD,
                "Cannot create Owner with username '" + username
                    + "'. Incorrect password.");
        }

        // Check to see if an admin or owner with the same username and password
        // exists.
        if (accountExistsInGlobal(username, email))
        {
            log.info(
                "Account with the same username or non-null email already exists! Username = {}, email = {}",
                username, email);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS,
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'");
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
        accountEntity.setDeleted(false);

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create Owner with username {}. Error writing to database.",
                username);

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot create Owner with username " + username
                    + ". Error writing to database.",
                e);
        }

        log.info("Created Owner account with username '{}', id {}", username,
            accountEntity.getId());

        try
        {
            Hibernate.initialize(accountEntity.getInstances());

            return getOwnerDto(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for user account id {}. Error reading from database.",
                accountEntity.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for user account id "
                    + accountEntity.getId() + ". Error reading from database.",
                e);
        }
    }

    @Transactional
    public MaintainerDto createTransientMaintainer(InstanceDto parentInstance)
        throws UserAccountServiceException
    {
        if (parentInstance == null)
        {
            log.info(
                "Attempted creation of a transient maintainer on a null instance.");

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT,
                "Attempted creation of a transient maintainer on a null instance.");
        }

        log.info(
            "Attempting to create transient Maintainer account on instance id {}",
            parentInstance.getId());

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService.getInstance(parentInstance);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create transient Maintainer on instance id {}. Error reading from database.",
                parentInstance.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot create transient Maintainer on instance id "
                    + parentInstance.getId() + ". Error reading from database.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot create transient Maintainer on instance id {}. Instance not found.",
                parentInstance.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INSTANCE_NOT_FOUND,
                "Cannot create transient Maintainer on instance id "
                    + parentInstance.getId() + ". Instance not found.");
        }

        // Generate database entity.
        UserAccount accountEntity = new UserAccount();

        accountEntity.setRole(UserAccountRoles.USER_ROLE_TRANSIENT);
        accountEntity.setInviteToken("newrandomtoken");
        accountEntity.setParentInstance(instanceEntity);
        accountEntity.setCreateDate(LocalDateTime.now());
        accountEntity.setDeleted(false);

        try
        {
            accountEntity = userAccountRepository.save(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create transient Maintainer on instance id {}. Error writing to database.",
                parentInstance.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot create transient Maintainer on instance id "
                    + parentInstance.getId() + ". Error writing to database.",
                e);
        }

        log.info("Created transient Maintainer account with id {}",
            accountEntity.getId());

        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for user account id {}. Error reading from database.",
                accountEntity.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for user account id "
                    + accountEntity.getId() + ". Error reading from database.",
                e);
        }
    }

    @Transactional
    public MaintainerDto createTransientMaintainer(CharacterDto character)
        throws UserAccountServiceException
    {
        if (character == null)
        {
            log.info(
                "Attempted creation of a transient maintainer on a null character.");

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_CHARACTER_OBJECT,
                "Attempted creation of a transient maintainer on a null character.");
        }

        log.info(
            "Attempting to create transient Maintainer account for character id {}",
            character.getId());

        Character characterEntity;

        try
        {
            characterEntity = characterService.getCharacter(character);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create transient Maintainer for character id {}. Error reading from database.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Error reading from database.",
                e);
        }

        if (characterEntity == null)
        {
            log.info(
                "Cannot create transient Maintainer for character id {}. Character not found.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.CHARACTER_NOT_FOUND,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Character not found.");
        }

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService
                .getInstance(character.getInstanceId());
        }
        catch (Exception e)
        {
            log.info(
                "Cannot create transient Maintainer for character id {}. Error reading from database.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Error reading from database.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot create transient Maintainer for character id {}. Incorrect instance object.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Incorrect instance object.");
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
        catch (Exception e)
        {
            log.info(
                "Cannot create transient Maintainer for character id {}. Error writing to database.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Error writing to database.",
                e);
        }

        log.info("Created transient Maintainer account with id {}",
            accountEntity.getId());

        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for user account id {}. Error reading from database.",
                accountEntity.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for user account id "
                    + accountEntity.getId() + ". Error reading from database.",
                e);
        }
    }

    @Transactional
    public MaintainerDto registerMaintainer(MaintainerDto transientMaintainer,
        String username, String password, String email, String displayname)
        throws UserAccountServiceException
    {
        if (transientMaintainer == null)
        {
            log.info("Attempted registration of a null transient maintainer.");

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.NULL_ACCOUNT_OBJECT,
                "Attempted registration of a null transient maintainer.");
        }

        log.info(
            "Attempting to register Maintainer account id {} with username '{}'",
            transientMaintainer.getId(), username);

        Instance instanceEntity;

        try
        {
            instanceEntity = instanceService
                .getInstance(transientMaintainer.getParentInstanceId());
        }
        catch (Exception e)
        {
            log.info(
                "Cannot register Maintainer with id {}. Error reading from database.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId()
                    + ". Error reading from database.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot register Maintainer with id {}. Incorrect instance object.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId()
                    + ". Incorrect instance object.");
        }

        // Incorrect username.
        if (!StringUtils.hasText(username))
        {
            log.info(
                "Cannot register Maintainer with id {}. Incorrect username.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_USERNAME,
                "Cannot create Owner with username '"
                    + transientMaintainer.getId() + "'. Incorrect username.");
        }

        // Incorrect password.
        if (!StringUtils.hasText(password))
        {
            log.info(
                "Cannot register Maintainer with id {}. Incorrect password.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_PASSWORD,
                "Cannot register Maintainer with id '"
                    + transientMaintainer.getId() + "'. Incorrect password.");
        }

        // Check to see if an account with the same username and password
        // exists.
        if (accountExistsInInstance(username, email, instanceEntity))
        {
            log.info(
                "Account with the same username or non-null email already exists! Username = {}, email = {}",
                username, email);
            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS,
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'");
        }

        // Read user account from database.
        UserAccount accountEntity;

        try
        {
            accountEntity = getUserAccount(transientMaintainer);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot register Maintainer with id {}. Error reading from database.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId()
                    + ". Error reading from database.",
                e);
        }

        // Check if we're editing the correct account.
        if (accountEntity == null)
        {
            log.info(
                "Cannot register Maintainer with id {}. User account not found in database.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.ACCOUNT_NOT_FOUND,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId()
                    + ". User account not found in database.");
        }

        if (accountEntity.getRole() != UserAccountRoles.USER_ROLE_TRANSIENT)
        {
            log.info("Cannot register Maintainer with id {}. Invalid role.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.ACCOUNT_ALREADY_REGISTERED,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId() + ". Invalid role.");
        }

        // Attempt to modify and save user account.
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
        catch (Exception e)
        {
            log.info(
                "Cannot register Maintainer with id {}. Error writing to database.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_WRITE,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId()
                    + ". Error writing to database.",
                e);
        }

        log.info(
            "Registered transient Maintainer account {} with username '{}'",
            accountEntity.getId(), accountEntity.getUsername());

        try
        {
            Hibernate.initialize(accountEntity.getCharacters());

            return getMaintainerDto(accountEntity);
        }
        catch (Exception e)
        {
            log.info(
                "Cannot return model for user account id {}. Error reading from database.",
                accountEntity.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR_READ,
                "Cannot return model for user account id "
                    + accountEntity.getId() + ". Error reading from database.",
                e);
        }
    }

    private Boolean accountExistsInGlobal(String username, String email)
    {
        log.debug(
            "Searching for existance of Global user account with username '{}' and/or email '{}",
            username, email);

        UserAccount probe = new UserAccount();
        probe.setUsername(username);
        probe.setDeleted(false);

        if (StringUtils.hasText(email))
            probe.setEmail(email);

        ExampleMatcher matcher = ExampleMatcher.matchingAny().withIgnoreCase()
            .withIgnorePaths("role", "deleted");

        // Check for Admins first.
        probe.setRole(UserAccountRoles.USER_ROLE_ADMIN);
        Boolean check = userAccountRepository
            .exists(Example.of(probe, matcher));

        if (check)
            return true;

        // Now check for Owners.
        probe.setRole(UserAccountRoles.USER_ROLE_OWNER);
        return userAccountRepository.exists(Example.of(probe, matcher));
    }

    private Boolean accountExistsInInstance(String username, String email,
        Instance parentInstance)
    {
        log.debug(
            "Searching for existance of Maintainer user account with username '{}' and/or email '{} in instance id {}",
            username, email, parentInstance.getId());

        UserAccount probe = new UserAccount();
        probe.setUsername(username);
        probe.setParentInstance(parentInstance);
        probe.setRole(UserAccountRoles.USER_ROLE_MAINTAINER);
        probe.setDeleted(false);

        if (StringUtils.hasText(email))
            probe.setEmail(email);

        ExampleMatcher matcher = ExampleMatcher.matchingAny().withIgnoreCase()
            .withIgnorePaths("role", "parentInstance", "deleted");

        return userAccountRepository.exists(Example.of(probe, matcher));
    }

    public AdminDto getAdminDto(UserAccount ua) throws Exception
    {
        AdminDto dto = new AdminDto();

        dto.setId(ua.getId());
        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());
        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }

    public OwnerDto getOwnerDto(UserAccount ua) throws Exception
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

    public MaintainerDto getMaintainerDto(UserAccount ua) throws Exception
    {
        MaintainerDto dto = new MaintainerDto();

        dto.setId(ua.getId());
        dto.setParentInstanceId(ua.getParentInstance().getId());
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
}
