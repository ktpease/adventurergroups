package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class InstanceDto {
    private Long id;

    private Boolean isActive;

    private String subdomainName;
    private String displayName;
    private String description;

    private OwnerDto owner;
    private Set<MaintainerDto> maintainers;

    private Set<Character> characters;
    private Set<CharacterGroupDto> characterGroups;

    private LocalDateTime createDate;
    private LocalDateTime lastInactiveDate;
}
