package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;

import lombok.Data;

@Data
@JsonFilter("instanceFilter")
public class InstanceDto
{
    private Long id;

    private Boolean active;

    private String subdomainName;
    private String displayName;
    private String description;

    private OwnerDto owner;
    private Set<MaintainerDto> maintainers;

    private Set<CharacterDto> characters;
    private Set<CharacterGroupDto> characterGroups;

    private LocalDateTime createDate;
    private LocalDateTime lastActivateDate;
    private LocalDateTime lastDeactivateDate;
}
