package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ktpweb.adventurergroups.entity.UserAccount;
import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.AdminDto;
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

    public UserAccount getUserAccount(AdminDto adminDto)
    {
        try
        {
            return userAccountRepository.findById(adminDto.getId())
                .orElse(null);
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            return null;
        }
    }

    public UserAccount getUserAccount(OwnerDto ownerDto)
    {
        try
        {
            return userAccountRepository.findById(ownerDto.getId())
                .orElse(null);
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            return null;
        }
    }

    public UserAccount getUserAccount(MaintainerDto maintainerDto)
    {
        try
        {
            return userAccountRepository.findById(maintainerDto.getId())
                .orElse(null);
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            return null;
        }
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
                "Cannot create Admin with username '" + username
                    + "'. Incorrect username.",
                UserAccountServiceException.Codes.INVALID_USERNAME);
        }

        // Incorrect password.
        if (!StringUtils.hasText(password))
        {
            log.info(
                "Cannot create Admin with username '{}'. Incorrect password.",
                username);

            throw new UserAccountServiceException(
                "Cannot create Admin with username '" + username
                    + "'. Incorrect password.",
                UserAccountServiceException.Codes.INVALID_PASSWORD);
        }

        // Check to see if an admin or owner with the same username and password
        // exists.
        if (accountExistsInGlobal(username, email))
        {
            log.info(
                "Account with the same username or non-null email already exists! Username = {}, email = {}",
                username, email);

            throw new UserAccountServiceException(
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'",
                UserAccountServiceException.Codes.ACCOUNT_EXISTS);
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

        return getAdminDtoFromUserAccount(newAccount);
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
                "Cannot create Owner with username '" + username
                    + "'. Incorrect username.",
                UserAccountServiceException.Codes.INVALID_USERNAME);
        }

        // Incorrect password.
        if (!StringUtils.hasText(password))
        {
            log.info(
                "Cannot create Owner with username '{}'. Incorrect password.",
                username);

            throw new UserAccountServiceException(
                "Cannot create Owner with username '" + username
                    + "'. Incorrect password.",
                UserAccountServiceException.Codes.INVALID_PASSWORD);
        }

        // Check to see if an admin or owner with the same username and password
        // exists.
        if (accountExistsInGlobal(username, email))
        {
            log.info(
                "Account with the same username or non-null email already exists! Username = {}, email = {}",
                username, email);

            throw new UserAccountServiceException(
                "Account with the same username or non-null email already exists! Username = '"
                    + username + "', email = '" + email + "'",
                UserAccountServiceException.Codes.ACCOUNT_EXISTS);
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

        return getOwnerDtoFromUserAccount(newAccount);
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

    private AdminDto getAdminDtoFromUserAccount(UserAccount ua)
    {
        AdminDto dto = new AdminDto();

        dto.setId(ua.getId());
        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());
        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }

    private OwnerDto getOwnerDtoFromUserAccount(UserAccount ua)
    {
        OwnerDto dto = new OwnerDto();

        dto.setId(ua.getId());
        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());
        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }
}
