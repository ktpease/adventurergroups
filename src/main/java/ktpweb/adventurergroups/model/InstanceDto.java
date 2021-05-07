package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class InstanceDto
{
    private Long id;

    private Boolean isActive;

    private String subdomainName;
    private String displayName;
    private String description;

    private Long ownerId;
    private Set<Long> maintainerIds;

    private Set<Long> characterIds;
    private Set<Long> characterGroupIds;

    private LocalDateTime createDate;
    private LocalDateTime lastActivateDate;
    private LocalDateTime lastDeactivateDate;
}
