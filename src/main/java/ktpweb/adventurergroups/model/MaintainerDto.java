package ktpweb.adventurergroups.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaintainerDto extends UserAccountDto
{
    private InstanceDto parentInstance;

    private Set<CharacterDto> characters;

    private Boolean isTransient;
    private String inviteToken;
}
