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

import ktpweb.adventurergroups.exception.CharacterServiceException;
import ktpweb.adventurergroups.exception.InstanceServiceException;
import ktpweb.adventurergroups.model.CharacterGroupDto;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.modelfilter.CharacterGroupDtoFilters;
import ktpweb.adventurergroups.security.User;
import ktpweb.adventurergroups.service.CharacterService;
import ktpweb.adventurergroups.service.InstanceService;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class CharacterGroupController
{
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private InstanceService instanceService;

    //
    // Direct endpoints.
    //

    @GetMapping("/instances/{instanceId}/groups")
    public ResponseEntity<MappingJacksonValue> retrieveGroupList(
        @PathVariable String instanceId)
    {
        try
        {
            InstanceDto instance = instanceService
                .retrieveInstance(Long.parseLong(instanceId));

            MappingJacksonValue returnValue = new MappingJacksonValue(
                instance.getCharacterGroups());
            returnValue
                .setFilters(CharacterGroupDtoFilters.simpleFilterProvider);

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

    @PostMapping("/instances/{instanceId}/groups")
    public ResponseEntity<MappingJacksonValue> createGroup(
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

            CharacterGroupDto characterGroup = characterService
                .createCharacterGroup(instance);

            MappingJacksonValue returnValue = new MappingJacksonValue(
                characterGroup);
            returnValue.setFilters(CharacterGroupDtoFilters.fullFilterProvider);

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

    @GetMapping("/instances/{instanceId}/groups/{groupId}")
    public ResponseEntity<MappingJacksonValue> retrieveGroup(
        @PathVariable String instanceId, @PathVariable String groupId)
    {
        try
        {
            CharacterGroupDto group = characterService
                .retrieveCharacterGroup(Long.parseLong(groupId));

            if (!group.getInstance().getId().equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            MappingJacksonValue returnValue = new MappingJacksonValue(group);
            returnValue.setFilters(CharacterGroupDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (CharacterServiceException ex)
        {
            switch (ex.getCode())
            {
            case CHARACTER_GROUP_NOT_FOUND:
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

    @PutMapping("/instances/{instanceId}/groups/{groupId}")
    public ResponseEntity<MappingJacksonValue> updateGroup(
        @PathVariable String instanceId, @PathVariable String groupId,
        @RequestBody CharacterGroupDto updatedGroup,
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

            CharacterGroupDto group = characterService
                .retrieveCharacterGroup(Long.parseLong(groupId));

            if (!group.getInstance().getId().equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            group = characterService.updateCharacterGroup(group.getId(),
                updatedGroup);

            MappingJacksonValue returnValue = new MappingJacksonValue(group);
            returnValue.setFilters(CharacterGroupDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (CharacterServiceException ex)
        {
            switch (ex.getCode())
            {
            case CHARACTER_GROUP_NOT_FOUND:
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

    @DeleteMapping("/instances/{instanceId}/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable String instanceId,
        @PathVariable String groupId, @AuthenticationPrincipal User authUser)
    {
        try
        {
            if (authUser == null || authUser.getId() == null
                || !userAccountService.ownerOwnsInstance(authUser.getId(),
                    Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            // Retrieve the group to check if it is in the right instance.
            CharacterGroupDto group = characterService
                .retrieveCharacterGroup(Long.parseLong(groupId));

            if (!group.getInstance().getId().equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            // Delete it and return 204.
            characterService.deleteCharacterGroup(group.getId());

            return ResponseEntity.noContent().build();
        }
        catch (CharacterServiceException ex)
        {
            switch (ex.getCode())
            {
            case CHARACTER_GROUP_NOT_FOUND:
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
