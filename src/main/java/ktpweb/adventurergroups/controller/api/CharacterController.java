package ktpweb.adventurergroups.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import ktpweb.adventurergroups.model.CharacterDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class CharacterController
{
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
        @PathVariable String instanceId)
    {
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
        @RequestBody CharacterDto updatedCharacter)
    {
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/instances/{instanceId}/characters/{characterId}")
    public ResponseEntity<?> deleteCharacter(@PathVariable String instanceId,
        @PathVariable String characterId)
    {
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
        @PathVariable String instanceId, @PathVariable String maintainerId)
    {
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
