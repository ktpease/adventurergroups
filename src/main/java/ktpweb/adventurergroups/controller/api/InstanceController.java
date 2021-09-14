package ktpweb.adventurergroups.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import ktpweb.adventurergroups.exception.InstanceServiceException;
import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.OwnerDto;
import ktpweb.adventurergroups.modelfilter.InstanceDtoFilters;
import ktpweb.adventurergroups.security.User;
import ktpweb.adventurergroups.service.InstanceService;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class InstanceController
{
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private InstanceService instanceService;

    //
    // Direct endpoints.
    //

    @GetMapping("/instances/{instanceId}")
    public ResponseEntity<MappingJacksonValue> retrieveInstance(
        @PathVariable String instanceId)
    {
        try
        {
            InstanceDto instance = instanceService
                .retrieveInstance(Long.parseLong(instanceId));

            MappingJacksonValue returnValue = new MappingJacksonValue(instance);
            returnValue.setFilters(InstanceDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (InstanceServiceException ex)
        {
            switch (ex.getCode())
            {
            case INSTANCE_NOT_FOUND:
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

    @PutMapping("/instances/{instanceId}")
    public ResponseEntity<MappingJacksonValue> updateInstance(
        @PathVariable String instanceId,
        @RequestBody InstanceDto updatedInstance,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || !userAccountService.ownerOwnsInstance(authUser.getId(),
                    Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            InstanceDto instance = instanceService
                .updateInstance(Long.parseLong(instanceId), updatedInstance);

            MappingJacksonValue returnValue = new MappingJacksonValue(instance);
            returnValue.setFilters(InstanceDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (InstanceServiceException ex)
        {
            switch (ex.getCode())
            {
            case INSTANCE_NOT_FOUND:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, null,
                    ex);
            case NULL_INSTANCE_OBJECT:
            case INVALID_SUBDOMAINNAME:
            case INSTANCE_ALREADY_EXISTS:
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

    @DeleteMapping("/instances/{instanceId}")
    public ResponseEntity<?> deleteInstance(@PathVariable String instanceId,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || !userAccountService.ownerOwnsInstance(authUser.getId(),
                    Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            instanceService.deleteInstance(Long.parseLong(instanceId));

            return ResponseEntity.noContent().build();
        }
        catch (InstanceServiceException ex)
        {
            switch (ex.getCode())
            {
            case INSTANCE_NOT_FOUND:
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

    //
    // Endpoints from /owners
    //

    @GetMapping("/owners/{ownerId}/instances")
    public ResponseEntity<MappingJacksonValue> retrieveInstanceListForOwner(
        @PathVariable String ownerId)
    {
        try
        {
            OwnerDto owner = userAccountService
                .retrieveOwner(Long.parseLong(ownerId));

            MappingJacksonValue returnValue = new MappingJacksonValue(
                owner.getInstances());
            returnValue.setFilters(InstanceDtoFilters.simpleFilterProvider);

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

    @PostMapping("/owners/{ownerId}/instances")
    public ResponseEntity<MappingJacksonValue> createInstanceForOwner(
        @PathVariable String ownerId, @RequestBody InstanceDto newInstance,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || authUser.getId() != Long.parseLong(ownerId))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            OwnerDto owner = userAccountService
                .retrieveOwner(Long.parseLong(ownerId));

            InstanceDto instance = instanceService.createInstance(owner,
                newInstance);

            MappingJacksonValue returnValue = new MappingJacksonValue(instance);
            returnValue.setFilters(InstanceDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (InstanceServiceException ex)
        {
            switch (ex.getCode())
            {
            case INSTANCE_NOT_FOUND:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, null,
                    ex);
            case NULL_INSTANCE_OBJECT:
            case INVALID_SUBDOMAINNAME:
            case INSTANCE_ALREADY_EXISTS:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null,
                    ex);
            default:
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, null, ex);
            }
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
