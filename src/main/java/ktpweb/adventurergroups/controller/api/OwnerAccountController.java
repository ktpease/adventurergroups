package ktpweb.adventurergroups.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.OwnerDto;
import ktpweb.adventurergroups.model.UserAccountDto;
import ktpweb.adventurergroups.modelfilter.OwnerDtoFilters;
import ktpweb.adventurergroups.security.User;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class OwnerAccountController
{
    @Autowired
    private UserAccountService userAccountService;

    //
    // Direct endpoints.
    //

    @PostMapping("/owners")
    public ResponseEntity<MappingJacksonValue> createOwnerAccount(
        @RequestBody UserAccountDto newAccount)
    {
        // TODO: Who should have authority to create new owner account?

        try
        {
            OwnerDto owner = userAccountService.createOwner(newAccount);

            MappingJacksonValue returnValue = new MappingJacksonValue(owner);
            returnValue.setFilters(OwnerDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (UserAccountServiceException ex)
        {
            switch (ex.getCode())
            {
            case INVALID_USERNAME:
            case INVALID_PASSWORD:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null,
                    ex);
            case ACCOUNT_ALREADY_EXISTS:
                throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, null, ex);
            default:
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, null, ex);
            }
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }
    }

    @GetMapping(path = "/owners/{ownerId}")
    public ResponseEntity<MappingJacksonValue> retrieveOwnerAccount(
        @PathVariable String ownerId)
    {
        try
        {
            OwnerDto owner = userAccountService
                .retrieveOwner(Long.parseLong(ownerId));

            MappingJacksonValue returnValue = new MappingJacksonValue(owner);
            returnValue.setFilters(OwnerDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (UserAccountServiceException ex)
        {
            switch (ex.getCode())
            {
            case ACCOUNT_NOT_FOUND:
            case INVALID_ROLE:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, null,
                    ex);
            default:
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, null, ex);
            }
        }
        catch (NumberFormatException ex)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, ex);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }
    }

    @PatchMapping("/owners/{ownerId}")
    public ResponseEntity<MappingJacksonValue> updateOwnerAccount(
        @PathVariable String ownerId,
        @RequestBody UserAccountDto updatedAccount,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || !authUser.getId().equals(Long.parseLong(ownerId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            OwnerDto owner = userAccountService
                .updateOwner(Long.parseLong(ownerId), updatedAccount);

            MappingJacksonValue returnValue = new MappingJacksonValue(owner);
            returnValue.setFilters(OwnerDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (UserAccountServiceException ex)
        {
            switch (ex.getCode())
            {
            case ACCOUNT_NOT_FOUND:
            case INVALID_ROLE:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, null,
                    ex);
            case NULL_ACCOUNT_OBJECT:
            case INVALID_USERNAME:
            case INVALID_PASSWORD:
            case INVALID_EMAIL:
            case ACCOUNT_ALREADY_EXISTS:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null,
                    ex);
            default:
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, null, ex);
            }
        }
        catch (NumberFormatException ex)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, ex);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }
    }

    @DeleteMapping(path = "/owners/{ownerId}")
    public ResponseEntity<?> deleteOwnerAccount(@PathVariable String ownerId,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || !authUser.getId().equals(Long.parseLong(ownerId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            userAccountService.deleteOwner(Long.parseLong(ownerId));

            return ResponseEntity.noContent().build();
        }
        catch (UserAccountServiceException ex)
        {
            switch (ex.getCode())
            {
            case ACCOUNT_NOT_FOUND:
            case INVALID_ROLE:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, null,
                    ex);
            default:
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, null, ex);
            }
        }
        catch (NumberFormatException ex)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, ex);
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }
    }
}
