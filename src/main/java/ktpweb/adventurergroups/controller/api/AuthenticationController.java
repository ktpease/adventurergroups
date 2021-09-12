package ktpweb.adventurergroups.controller.api;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import ktpweb.adventurergroups.model.UserAccountDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AuthenticationController
{
    @Autowired
    private AuthenticationManager customAuthenticationManager;

    @PostMapping("/api/auth")
    public ResponseEntity<String> authenticate(
        @RequestParam(name = "instance", required = false) String instanceId,
        @RequestBody UserAccountDto loginDetails, HttpSession session)
    {
        StringBuilder complexUsername = new StringBuilder();

        if (instanceId == null)
        {
            complexUsername.append("O-");
        }
        else
        {
            try
            {
                complexUsername.append(Long.valueOf(instanceId)).append("-");
            }
            catch (NumberFormatException ex)
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid instance ID.", ex);
            }
        }

        complexUsername.append(loginDetails.getUsername());

        log.debug("Attempting to login with complexUsername: {}",
            complexUsername);

        Authentication authentication = customAuthenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(complexUsername,
                loginDetails.getPassword()));

        if (authentication != null
            && !(authentication instanceof AnonymousAuthenticationToken)
            && authentication.isAuthenticated())
        {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.ok(session.getId());
        }
        else
        {
            return ResponseEntity.badRequest().build();
        }
    }
}
