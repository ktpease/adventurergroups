package ktpweb.adventurergroups.model;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Owner extends UserAccount
{
    private Set<Instance> instances;
}
