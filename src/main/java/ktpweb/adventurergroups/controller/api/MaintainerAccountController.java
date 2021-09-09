package ktpweb.adventurergroups.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import ktpweb.adventurergroups.model.InstanceDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/v1")
public class MaintainerAccountController
{
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
        @PathVariable String instanceId)
    {
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
        @RequestBody InstanceDto updatedInstance)
    {
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/instances/{instanceId}/maintainers/{maintainerId}")
    public ResponseEntity<?> deleteMaintainer(@PathVariable String instanceId,
        @PathVariable String maintainerId)
    {
        return ResponseEntity.notFound().build();
    }
}
