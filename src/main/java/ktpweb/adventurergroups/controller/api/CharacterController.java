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

import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.security.User;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class CharacterController
{
    @Autowired
    private UserAccountService userAccountService;
    
    //
    // Direct endpoints.
    //

    @GetMapping("/instances/{instanceId}/characters")
    public ResponseEntity<MappingJacksonValue> retrieveCharacterList(
        @PathVariable String instanceId)
    {
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/instances/{instanceId}/characters")
    public ResponseEntity<MappingJacksonValue> createCharacter(
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

    @GetMapping("/instances/{instanceId}/characters/{characterId}")
    public ResponseEntity<MappingJacksonValue> retrieveCharacter(
        @PathVariable String instanceId, @PathVariable String characterId)
    {
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/instances/{instanceId}/characters/{characterId}")
    public ResponseEntity<MappingJacksonValue> updateCharacter(
        @PathVariable String instanceId, @PathVariable String characterId,
        @RequestBody CharacterDto updatedCharacter,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || !userAccountService.userOwnsCharacter(authUser.getId(),
                    Long.parseLong(characterId)))
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

    @DeleteMapping("/instances/{instanceId}/characters/{characterId}")
    public ResponseEntity<?> deleteCharacter(@PathVariable String instanceId,
        @PathVariable String characterId,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || !userAccountService.userOwnsCharacter(authUser.getId(),
                    Long.parseLong(characterId)))
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

    //
    // Endpoints from /maintainers
    //

    @GetMapping("/instances/{instanceId}/maintainers/{maintainerId}/characters")
    public ResponseEntity<MappingJacksonValue> retrieveCharacterListForMaintainer(
        @PathVariable String instanceId, @PathVariable String maintainerId)
    {
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/instances/{instanceId}/maintainers/{maintainerId}/characters")
    public ResponseEntity<MappingJacksonValue> createCharacterForMaintainer(
        @PathVariable String instanceId, @PathVariable String maintainerId,
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
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                null, ex);
        }

        return ResponseEntity.notFound().build();
    }

    //
    // Endpoints from /groups
    //

    @GetMapping("/instances/{instanceId}/groups/{groupId}/characters")
    public ResponseEntity<MappingJacksonValue> retrieveCharacterListForGroup(
        @PathVariable String instanceId, @PathVariable String maintainerId)
    {
        return ResponseEntity.notFound().build();
    }
}
