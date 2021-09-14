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
import ktpweb.adventurergroups.exception.UserAccountServiceException;
import ktpweb.adventurergroups.model.CharacterDto;
import ktpweb.adventurergroups.model.CharacterGroupDto;
import ktpweb.adventurergroups.model.InstanceDto;
import ktpweb.adventurergroups.model.MaintainerDto;
import ktpweb.adventurergroups.modelfilter.CharacterDtoFilters;
import ktpweb.adventurergroups.security.User;
import ktpweb.adventurergroups.service.CharacterService;
import ktpweb.adventurergroups.service.InstanceService;
import ktpweb.adventurergroups.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class CharacterController
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

    @GetMapping("/instances/{instanceId}/characters")
    public ResponseEntity<MappingJacksonValue> retrieveCharacterList(
        @PathVariable String instanceId)
    {
        try
        {
            InstanceDto instance = instanceService
                .retrieveInstance(Long.parseLong(instanceId));

            MappingJacksonValue returnValue = new MappingJacksonValue(
                instance.getCharacters());
            returnValue.setFilters(CharacterDtoFilters.simpleFilterProvider);

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

            InstanceDto instance = instanceService
                .retrieveInstance(Long.parseLong(instanceId));

            CharacterDto character = characterService.createCharacter(instance);

            MappingJacksonValue returnValue = new MappingJacksonValue(
                character);
            returnValue.setFilters(CharacterDtoFilters.fullFilterProvider);

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

    @GetMapping("/instances/{instanceId}/characters/{characterId}")
    public ResponseEntity<MappingJacksonValue> retrieveCharacter(
        @PathVariable String instanceId, @PathVariable String characterId)
    {
        try
        {
            CharacterDto character = characterService
                .retrieveCharacter(Long.parseLong(characterId));

            if (!character.getInstance().getId()
                .equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            MappingJacksonValue returnValue = new MappingJacksonValue(
                character);
            returnValue.setFilters(CharacterDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (CharacterServiceException ex)
        {
            switch (ex.getCode())
            {
            case CHARACTER_NOT_FOUND:
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

    @PutMapping("/instances/{instanceId}/characters/{characterId}")
    public ResponseEntity<MappingJacksonValue> updateCharacter(
        @PathVariable String instanceId, @PathVariable String characterId,
        @RequestBody CharacterDto updatedCharacter,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            // Check basic authorization.
            if (authUser == null || authUser.getId() == null)
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            // Retrieve the character to check if it is in the right instance
            // and if the user is authorized to edit it.
            CharacterDto character = characterService
                .retrieveCharacter(Long.parseLong(characterId));

            if (!character.getInstance().getId()
                .equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            else if (!userAccountService.userOwnsCharacter(authUser.getId(),
                character.getId()))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            // Edit and return it.
            character = characterService.updateCharacter(character.getId(),
                updatedCharacter);

            MappingJacksonValue returnValue = new MappingJacksonValue(
                character);
            returnValue.setFilters(CharacterDtoFilters.fullFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (CharacterServiceException ex)
        {
            switch (ex.getCode())
            {
            case CHARACTER_NOT_FOUND:
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, null,
                    ex);
            case NULL_CHARACTER_OBJECT:
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

    @DeleteMapping("/instances/{instanceId}/characters/{characterId}")
    public ResponseEntity<?> deleteCharacter(@PathVariable String instanceId,
        @PathVariable String characterId,
        @AuthenticationPrincipal User authUser)
    {
        try
        {
            // Check basic authorization.
            if (authUser == null || authUser.getId() == null)
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            // Retrieve the character to check if it is in the right instance
            // and if the user is authorized to delete it.
            CharacterDto character = characterService
                .retrieveCharacter(Long.parseLong(characterId));

            if (!character.getInstance().getId()
                .equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            else if (!userAccountService.userOwnsCharacter(authUser.getId(),
                character.getId()))
            {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            // Delete it and return 204.
            characterService.deleteCharacter(character.getId());

            return ResponseEntity.noContent().build();
        }
        catch (CharacterServiceException ex)
        {
            switch (ex.getCode())
            {
            case CHARACTER_NOT_FOUND:
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
    // Endpoints from /maintainers
    //

    @GetMapping("/instances/{instanceId}/maintainers/{maintainerId}/characters")
    public ResponseEntity<MappingJacksonValue> retrieveCharacterListForMaintainer(
        @PathVariable String instanceId, @PathVariable String maintainerId)
    {
        try
        {
            MaintainerDto maintainer = userAccountService
                .retrieveMaintainer(Long.parseLong(maintainerId));

            if (!maintainer.getInstance().getId()
                .equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            MappingJacksonValue returnValue = new MappingJacksonValue(
                maintainer.getCharacters());
            returnValue.setFilters(CharacterDtoFilters.simpleFilterProvider);

            return ResponseEntity.ok(returnValue);
        }
        catch (UserAccountServiceException ex)
        {
            switch (ex.getCode())
            {
            case ACCOUNT_NOT_FOUND:
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

            MaintainerDto maintainer = userAccountService
                .retrieveMaintainer(Long.parseLong(maintainerId));

            if (!maintainer.getInstance().getId()
                .equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            CharacterDto character = characterService
                .createCharacterForMaintainer(maintainer);

            MappingJacksonValue returnValue = new MappingJacksonValue(
                character);
            returnValue.setFilters(CharacterDtoFilters.fullFilterProvider);

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

    //
    // Endpoints from /groups
    //

    @GetMapping("/instances/{instanceId}/groups/{groupId}/characters")
    public ResponseEntity<MappingJacksonValue> retrieveCharacterListForGroup(
        @PathVariable String instanceId, @PathVariable String groupId)
    {
        try
        {
            CharacterGroupDto group = characterService
                .retrieveCharacterGroup(Long.parseLong(groupId));

            if (!group.getInstance().getId().equals(Long.parseLong(instanceId)))
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            MappingJacksonValue returnValue = new MappingJacksonValue(
                group.getCharacters());
            returnValue.setFilters(CharacterDtoFilters.simpleFilterProvider);

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
}
