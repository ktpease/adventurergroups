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

import ktpweb.adventurergroups.exception.InstanceServiceException;
import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.MaintainerDto;
import ktpweb.adventurergroups.model.UserAccountDto;
import ktpweb.adventurergroups.modelfilter.MaintainerDtoFilters;
import ktpweb.adventurergroups.security.User;
import ktpweb.adventurergroups.service.InstanceService;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class MaintainerAccountController
{
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private InstanceService instanceService;

    //
    // Direct endpoints.
    //

    @GetMapping("/instances/{instanceId}/maintainers")
    public ResponseEntity<MappingJacksonValue> retrieveMaintainerList(
        @PathVariable String instanceId)
    {
        try
        {
            InstanceDto instance = instanceService
                .retrieveInstance(Long.parseLong(instanceId));

            MappingJacksonValue returnValue = new MappingJacksonValue(
                instance.getMaintainers());
            returnValue.setFilters(MaintainerDtoFilters.simpleFilterProvider);

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

    @PostMapping("/instances/{instanceId}/maintainers")
    public ResponseEntity<MappingJacksonValue> createMaintainer(
        @PathVariable String instanceId, @AuthenticationPrincipal User authUser)
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
                .retrieveInstance(Long.parseLong(instanceId));

            MaintainerDto maintainer = userAccountService
                .createUnregisteredMaintainer(instance);

            MappingJacksonValue returnValue = new MappingJacksonValue(
                maintainer);
            returnValue.setFilters(MaintainerDtoFilters.fullFilterProvider);

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

    @GetMapping("/instances/{instanceId}/maintainers/{maintainerId}")
    public ResponseEntity<MappingJacksonValue> retrieveMaintainer(
        @PathVariable String instanceId, @PathVariable String maintainerId)
    {
        try
        {
            MaintainerDto maintainer = userAccountService
                .retrieveMaintainer(Long.parseLong(maintainerId));

            if (maintainer.getInstance().getId()
                .equals(Long.parseLong(maintainerId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            MappingJacksonValue returnValue = new MappingJacksonValue(
                maintainer);
            returnValue.setFilters(MaintainerDtoFilters.fullFilterProvider);

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

    @PatchMapping("/instances/{instanceId}/maintainers/{maintainerId}")
    public ResponseEntity<MappingJacksonValue> updateMaintainer(
        @PathVariable String instanceId, @PathVariable String maintainerId,
        @RequestBody UserAccountDto updatedAccount,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || (!authUser.getId().equals(Long.parseLong(maintainerId))
                    && !userAccountService.ownerOwnsInstance(authUser.getId(),
                        Long.parseLong(instanceId))))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            MaintainerDto maintainer = userAccountService
                .retrieveMaintainer(Long.parseLong(maintainerId));

            if (!maintainer.getInstance().getId()
                .equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            maintainer = userAccountService
                .registerOrUpdateMaintainer(maintainer.getId(), updatedAccount);

            MappingJacksonValue returnValue = new MappingJacksonValue(
                maintainer);
            returnValue.setFilters(MaintainerDtoFilters.fullFilterProvider);

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

    @DeleteMapping("/instances/{instanceId}/maintainers/{maintainerId}")
    public ResponseEntity<?> deleteMaintainer(@PathVariable String instanceId,
        @PathVariable String maintainerId,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || (!authUser.getId().equals(Long.parseLong(maintainerId))
                    && !userAccountService.ownerOwnsInstance(authUser.getId(),
                        Long.parseLong(instanceId))))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            MaintainerDto maintainer = userAccountService
                .retrieveMaintainer(Long.parseLong(maintainerId));

            if (!maintainer.getInstance().getId()
                .equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            userAccountService.deleteMaintainer(maintainer.getId());

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
