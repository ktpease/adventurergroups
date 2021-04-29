package ktpweb.adventurergroups.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Maintainer extends UserAccount
{
    private Instance parentInstance;

    private Set<Character> characters;

    private Boolean isTransient;
    private String inviteToken;
}
