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

import ktpweb.adventurergroups.model.CharacterGroupDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class CharacterGroupController
{
    //
    // Direct endpoints.
    //

    @GetMapping("/instances/{instanceId}/groups")
    public ResponseEntity<MappingJacksonValue> retrieveGroupList(
        @PathVariable String instanceId)
    {
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/instances/{instanceId}/groups")
    public ResponseEntity<MappingJacksonValue> createGroup(
        @PathVariable String instanceId)
    {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/instances/{instanceId}/groups/{groupId}")
    public ResponseEntity<MappingJacksonValue> retrieveGroup(
        @PathVariable String instanceId, @PathVariable String groupId)
    {
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/instances/{instanceId}/groups/{groupId}")
    public ResponseEntity<MappingJacksonValue> updateGroup(
        @PathVariable String instanceId, @PathVariable String groupId,
        @RequestBody CharacterGroupDto updatedGroup)
    {
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/instances/{instanceId}/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable String instanceId,
        @PathVariable String groupId)
    {
        return ResponseEntity.notFound().build();
    }
}
