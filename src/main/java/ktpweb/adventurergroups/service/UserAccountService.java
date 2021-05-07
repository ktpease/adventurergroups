package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
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

    public UserAccount getUserAccount(Long id) throws Exception
    {
        try
        {
            return userAccountRepository.findById(id).orElse(null);
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }

    }

    public UserAccount getUserAccount(AdminDto adminDto) throws Exception
    {
        return getUserAccount(adminDto.getId());
    }

    public UserAccount getUserAccount(OwnerDto ownerDto) throws Exception
    {
        return getUserAccount(ownerDto.getId());
    }

    public UserAccount getUserAccount(MaintainerDto maintainerDto)
        throws Exception
    {
        return getUserAccount(maintainerDto.getId());
    }

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
                UserAccountServiceException.Codes.ACCOUNT_EXISTS,
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'");
        }

        // Generate database entity.
        UserAccount newAccount = new UserAccount();

        newAccount.setUsername(username);
        newAccount.setPassword(password);
        newAccount.setEmail(email);
        newAccount.setDisplayname(
            StringUtils.hasText(displayname) ? displayname : username);
        newAccount.setRole(UserAccountRoles.USER_ROLE_ADMIN);
        newAccount.setCreateDate(LocalDateTime.now());

        newAccount = userAccountRepository.save(newAccount);

        log.info("Created Admin account with username '{}', id {}", username,
            newAccount.getId());

        return getAdminDto(newAccount);
    }

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
                UserAccountServiceException.Codes.ACCOUNT_EXISTS,
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'");
        }

        // Generate database entity.
        UserAccount newAccount = new UserAccount();

        newAccount.setUsername(username);
        newAccount.setPassword(password);
        newAccount.setEmail(email);
        newAccount.setDisplayname(
            StringUtils.hasText(displayname) ? displayname : username);
        newAccount.setRole(UserAccountRoles.USER_ROLE_OWNER);
        newAccount.setCreateDate(LocalDateTime.now());

        newAccount = userAccountRepository.save(newAccount);

        log.info("Created Owner account with username '{}', id {}", username,
            newAccount.getId());

        return getOwnerDto(newAccount);
    }

    public MaintainerDto createTransientMaintainer(InstanceDto parentInstance)
        throws UserAccountServiceException
    {
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
                "Cannot create transient Maintainer on instance id {}. Database error.",
                parentInstance.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR,
                "Cannot create transient Maintainer on instance id "
                    + parentInstance.getId() + ". Database error.",
                e);
        }

        if (instanceEntity == null)
        {
            log.info(
                "Cannot create transient Maintainer on instance id {}. Incorrect instance object.",
                parentInstance.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_INSTANCE_OBJECT,
                "Cannot create transient Maintainer on instance id "
                    + parentInstance.getId() + ". Incorrect instance object.");
        }

        // Generate database entity.
        UserAccount newAccount = new UserAccount();

        newAccount.setRole(UserAccountRoles.USER_ROLE_TRANSIENT);
        newAccount.setInviteToken("newrandomtoken");
        newAccount.setParentInstance(instanceEntity);
        newAccount.setCreateDate(LocalDateTime.now());

        newAccount = userAccountRepository.save(newAccount);

        log.info("Created transient Maintainer account with id {}",
            newAccount.getId());

        return getMaintainerDto(newAccount);
    }

    public MaintainerDto createTransientMaintainer(CharacterDto character)
        throws UserAccountServiceException
    {
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
                "Cannot create transient Maintainer for character id {}. Database error.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Database error.",
                e);
        }

        if (characterEntity == null)
        {
            log.info(
                "Cannot create transient Maintainer for character id {}. Incorrect character object.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.INVALID_CHARACTER_OBJECT,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Incorrect character object.");
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
                "Cannot create transient Maintainer for character id {}. Database error.",
                character.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR,
                "Cannot create transient Maintainer for character id "
                    + character.getId() + ". Database error.",
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
        UserAccount newAccount = new UserAccount();

        newAccount.setRole(UserAccountRoles.USER_ROLE_TRANSIENT);
        // TODO: Generate randomized token.
        newAccount.setInviteToken("newrandomtoken");
        newAccount.setParentInstance(instanceEntity);
        newAccount.setCharacters(Stream.of(characterEntity)
            .collect(Collectors.toCollection(HashSet::new)));
        newAccount.setCreateDate(LocalDateTime.now());

        newAccount = userAccountRepository.save(newAccount);

        log.info("Created transient Maintainer account with id {}",
            newAccount.getId());

        return getMaintainerDto(newAccount);
    }

    public MaintainerDto registerMaintainer(MaintainerDto transientMaintainer,
        String username, String password, String email, String displayname)
        throws UserAccountServiceException
    {
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
            log.info("Cannot register Maintainer with id {}. Database error.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId() + ". Database error.",
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

        // Check to see if an admin or owner with the same username and password
        // exists.
        if (accountExistsInInstance(username, email, instanceEntity))
        {
            log.info(
                "Account with the same username or non-null email already exists! Username = {}, email = {}",
                username, email);
            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.ACCOUNT_EXISTS,
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'");
        }

        UserAccount accountEntity;

        try
        {
            accountEntity = getUserAccount(transientMaintainer);
        }
        catch (Exception e)
        {
            log.info("Cannot register Maintainer with id {}. Database error.",
                transientMaintainer.getId());

            throw new UserAccountServiceException(
                UserAccountServiceException.Codes.DATABASE_ERROR,
                "Cannot register Maintainer with id "
                    + transientMaintainer.getId() + ". Database error.",
                e);
        }

        accountEntity.setUsername(username);
        accountEntity.setPassword(password);
        accountEntity.setEmail(email);
        accountEntity.setDisplayname(
            StringUtils.hasText(displayname) ? displayname : username);
        accountEntity.setRole(UserAccountRoles.USER_ROLE_OWNER);
        accountEntity.setCreateDate(LocalDateTime.now());

        accountEntity = userAccountRepository.save(accountEntity);

        log.info(
            "Registered transient Maintainer account {} with username '{}'",
            accountEntity.getId(), accountEntity.getUsername());

        return getMaintainerDto(accountEntity);
    }

    private Boolean accountExistsInGlobal(String username, String email)
    {
        log.debug(
            "Searching for existance of Global user account with username '{}' and/or email '{}",
            username, email);

        UserAccount probe = new UserAccount();
        probe.setUsername(username);

        if (StringUtils.hasText(email))
            probe.setEmail(email);

        ExampleMatcher matcher = ExampleMatcher.matchingAny().withIgnoreCase()
            .withIgnorePaths("role");

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

        if (StringUtils.hasText(email))
            probe.setEmail(email);

        ExampleMatcher matcher = ExampleMatcher.matchingAny().withIgnoreCase()
            .withIgnorePaths("role", "parentInstance");

        return userAccountRepository.exists(Example.of(probe, matcher));
    }

    private AdminDto getAdminDto(UserAccount ua)
    {
        AdminDto dto = new AdminDto();

        dto.setId(ua.getId());
        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());
        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }

    private OwnerDto getOwnerDto(UserAccount ua)
    {
        OwnerDto dto = new OwnerDto();

        dto.setId(ua.getId());
        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());
        dto.setInstanceIds(ua.getInstances().stream().map(i -> i.getId())
            .collect(Collectors.toSet()));

        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }

    private MaintainerDto getMaintainerDto(UserAccount ua)
    {
        MaintainerDto dto = new MaintainerDto();

        dto.setId(ua.getId());
        dto.setParentInstanceId(ua.getParentInstance().getId());
        dto.setCharacterIds(ua.getCharacters().stream().map(c -> c.getId())
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
