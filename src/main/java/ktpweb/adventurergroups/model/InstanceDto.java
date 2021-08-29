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

    private Long ownerId;
    private Set<Long> maintainerIds;

    private Set<Long> characterIds;
    private Set<Long> characterGroupIds;

    private LocalDateTime createDate;
    private LocalDateTime lastActivateDate;
    private LocalDateTime lastDeactivateDate;
}
