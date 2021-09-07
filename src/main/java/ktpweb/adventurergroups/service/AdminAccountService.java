package ktpweb.adventurergroups.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import ktpweb.adventurergroups.entity.AdminAccount;
import ktpweb.adventurergroups.exception.AdminAccountServiceException;
import ktpweb.adventurergroups.model.AdminAccountDto;
import ktpweb.adventurergroups.repository.AdminAccountRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AdminAccountService
{
    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // -----------------------------------------------------------------------------------------------------------------
    // Admin-related public methods.
    // -----------------------------------------------------------------------------------------------------------------

    private final String EXCEPTION_ADMIN_CREATE = "Cannot create Admin account with username: ";
    private final String EXCEPTION_ADMIN_RETRIEVE = "Cannot retrieve Admin account with user id: ";
    private final String EXCEPTION_ADMIN_UPDATE = "Cannot update Admin account with user id: ";
    private final String EXCEPTION_ADMIN_DELETE = "Cannot delete Admin account with user id: ";
    private final String EXCEPTION_ADMIN_CHECKIFEXIST = "Cannot check if Admin account exists";

    private final String EXCEPTION_ADMIN_MODEL = "Cannot return model for Admin account with user id: ";

    @Transactional
    public AdminAccountDto createAdmin(String username, String password,
        String email, String displayname) throws AdminAccountServiceException
    {
        // Invalid username.
        if (!StringUtils.hasText(username))
        {
            throw generateException(
                EXCEPTION_ADMIN_CREATE + username + ". Invalid username",
                AdminAccountServiceException.Codes.INVALID_USERNAME);
        }

        // Invalid password.
        if (!StringUtils.hasText(password))
        {
            throw generateException(
                EXCEPTION_ADMIN_CREATE + username + ". Invalid password",
                AdminAccountServiceException.Codes.INVALID_PASSWORD);
        }

        // Check to see if an admin with the same username and email exists.
        Boolean accountExists;

        try
        {
            accountExists = accountExists(username, email);

        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_CREATE + username
                    + ". Error checking for matching accounts from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountExists)
        {
            throw generateException(
                "Account with the same username or non-null email already exists! Username = "
                    + username + ", email = " + email,
                AdminAccountServiceException.Codes.ACCOUNT_ALREADY_EXISTS);
        }

        // Generate database entity.
        AdminAccount accountEntity = new AdminAccount();

        accountEntity.setUsername(username);
        accountEntity.setPassword(passwordEncoder.encode(password));
        accountEntity.setEmail(email);
        accountEntity.setDisplayname(
            StringUtils.hasText(displayname) ? displayname : username);
        accountEntity.setCreateDate(LocalDateTime.now());

        try
        {
            accountEntity = adminAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_CREATE + username
                    + ". Error writing to database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Created Admin account with username: {}, id: {}", username,
            accountEntity.getId());

        try
        {
            return getAdminAccountDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public AdminAccountDto retrieveAdmin(Long userId)
        throws AdminAccountServiceException
    {
        AdminAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getAdminAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_RETRIEVE + userId
                    + ". Error reading from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_ADMIN_RETRIEVE + userId + ". User account not found",
                AdminAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Return full DTO.
        try
        {
            return getAdminAccountDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public AdminAccountDto updateAdmin(AdminAccountDto admin)
        throws AdminAccountServiceException
    {
        if (admin == null)
        {
            throw generateException("Attempted update of a null Admin account",
                AdminAccountServiceException.Codes.NULL_ACCOUNT_OBJECT);
        }

        AdminAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getAdminAccountEntity(admin.getId());
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_UPDATE + admin.getId()
                    + ". Error reading from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_ADMIN_UPDATE + admin.getId()
                    + ". User account not found",
                AdminAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Attempt to modify and save user account.
        accountEntity.setUsername(admin.getUsername());
        accountEntity.setEmail(admin.getEmail());
        accountEntity.setDisplayname(admin.getDisplayname());

        try
        {
            accountEntity = adminAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_UPDATE + admin.getId()
                    + ". Error writing to database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Updated Admin account with id: {}", admin.getId());

        try
        {
            return getAdminAccountDto(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_MODEL + accountEntity.getId()
                    + ". Error reading from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ_MAPPING,
                ex);
        }
    }

    @Transactional
    public void deleteAdmin(Long userId) throws AdminAccountServiceException
    {
        AdminAccount accountEntity;

        // Attempt to read from the database.
        try
        {
            accountEntity = getAdminAccountEntity(userId);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_DELETE + userId
                    + ". Error reading from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }

        if (accountEntity == null)
        {
            throw generateException(
                EXCEPTION_ADMIN_DELETE + userId + ". User account not found",
                AdminAccountServiceException.Codes.ACCOUNT_NOT_FOUND);
        }

        // Attempt to soft-delete user account.
        accountEntity.setDeleted(true);
        accountEntity.setDeleteDate(LocalDateTime.now());

        try
        {
            accountEntity = adminAccountRepository.save(accountEntity);
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_DELETE + userId + ". Error writing to database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_WRITE, ex);
        }

        log.info("Deleted Admin account with id: {}", userId);
    }

    public boolean checkIfAdminExists() throws AdminAccountServiceException
    {
        // Attempt to read from the database.
        try
        {
            return anyAccountExists();
        }
        catch (Exception ex)
        {
            throw generateException(
                EXCEPTION_ADMIN_CHECKIFEXIST + ". Error reading from database",
                AdminAccountServiceException.Codes.DATABASE_ERROR_READ, ex);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // General methods, not to be used with business layer.
    // -----------------------------------------------------------------------------------------------------------------

    protected AdminAccount getAdminAccountEntity(Long id) throws Exception
    {
        try
        {
            AdminAccount adminAccount = adminAccountRepository.findById(id)
                .orElse(null);

            return (adminAccount == null || adminAccount.getDeleted()) ? null
                : adminAccount;
        }
        catch (IllegalArgumentException iae)
        {
            return null;
        }
    }

    protected AdminAccount getAdminAccountEntity(
        AdminAccountDto adminAccountDto) throws Exception
    {
        return getAdminAccountEntity(adminAccountDto.getId());
    }

    private Boolean anyAccountExists() throws Exception
    {
        log.debug("Searching for existance of Admin user account.");

        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreCase();

        AdminAccount probe = new AdminAccount();
        probe.setDeleted(false);

        return adminAccountRepository.exists(Example.of(probe, matcher));
    }

    private Boolean accountExists(String username, String email)
        throws Exception
    {
        log.debug(
            "Searching for existance of Admin account with username: {} and/or email: {}",
            username, email);

        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreCase();

        AdminAccount probe = new AdminAccount();
        probe.setUsername(username);
        probe.setDeleted(false);

        Boolean check = adminAccountRepository
            .exists(Example.of(probe, matcher));

        if (check)
            return true;

        if (StringUtils.hasText(email))
        {
            probe.setUsername(null);
            probe.setEmail(email);

            check = adminAccountRepository.exists(Example.of(probe, matcher));
        }
        return check;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // DTO Mapping methods.
    // -----------------------------------------------------------------------------------------------------------------

    protected AdminAccountDto getAdminAccountDto(AdminAccount ua)
        throws Exception
    {
        AdminAccountDto dto = new AdminAccountDto();

        dto.setId(ua.getId());
        dto.setUsername(ua.getUsername());
        dto.setEmail(ua.getEmail());
        dto.setDisplayname(ua.getDisplayname());
        dto.setCreateDate(ua.getCreateDate());

        return dto;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Exception handling methods.
    // -----------------------------------------------------------------------------------------------------------------

    private AdminAccountServiceException generateException(String message,
        AdminAccountServiceException.Codes code)
    {
        log.error(message);

        return new AdminAccountServiceException(code, message);
    }

    private AdminAccountServiceException generateException(String message,
        AdminAccountServiceException.Codes code, Exception ex)
    {
        log.error(message, ex);

        return new AdminAccountServiceException(code, message, ex);
    }
}
