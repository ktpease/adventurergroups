package ktpweb.adventurergroups.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaintainerDto extends UserAccountDto
{
    private Long parentInstanceId;

    private Set<Long> characterIds;

    private Boolean isTransient;
    private String inviteToken;
}
