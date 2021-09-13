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

import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.security.User;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class MaintainerAccountController
{
    @Autowired
    private UserAccountService userAccountService;

    //
    // Direct endpoints.
    //

    @GetMapping("/instances/{instanceId}/maintainers")
    public ResponseEntity<MappingJacksonValue> retrieveMaintainerList(
        @PathVariable String instanceId)
    {
        return ResponseEntity.notFound().build();
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
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/instances/{instanceId}/maintainers/{maintainerId}")
    public ResponseEntity<MappingJacksonValue> retrieveMaintainer(
        @PathVariable String instanceId, @PathVariable String maintainerId)
    {
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/instances/{instanceId}/maintainers/{maintainerId}")
    public ResponseEntity<MappingJacksonValue> updateMaintainer(
        @PathVariable String instanceId, @PathVariable String maintainerId,
        @RequestBody InstanceDto updatedInstance,
        @AuthenticationPrincipal User authUser)
    {
        // TODO: Who should have authority to register user account?

        try
        {
            if (authUser == null || authUser.getId() == null
                || (!authUser.getId().equals(Long.parseLong(maintainerId))
                    && !userAccountService.ownerOwnsInstance(authUser.getId(),
                        Long.parseLong(instanceId))))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }

        return ResponseEntity.notFound().build();
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
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }
        
        return ResponseEntity.notFound().build();
    }
}
