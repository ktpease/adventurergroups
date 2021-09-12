package ktpweb.adventurergroups.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.UserAccountDto;
import ktpweb.adventurergroups.service.UserAccountService;

@Service
public class UserDetailsService
    implements org.springframework.security.core.userdetails.UserDetailsService
{
    @Autowired
    private UserAccountService userAccountService;

    public UserDetails loadUserByUsername(String username)
    {
        UserAccountDto userAccount;

        try
        {
            userAccount = userAccountService
                .retrieveUserAccountForLogin(username);
        }
        catch (UserAccountServiceException ex)
        {
            throw new AuthenticationServiceException(ex.getMessage());
        }

        if (userAccount == null)
        {
            throw new UsernameNotFoundException(
                "No user found with complex username: " + username);
        }

        return new User(userAccount.getId(), username,
            userAccount.getPassword());
    }
}