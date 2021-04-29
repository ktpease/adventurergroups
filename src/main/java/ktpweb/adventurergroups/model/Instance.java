package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class Instance {
    private Long id;

    private Boolean isActive;

    private String subdomainName;
    private String displayName;
    private String description;

    private Owner owner;
    private Set<Maintainer> maintainers;

    private Set<Character> characters;
    private Set<CharacterGroup> characterGroups;

    private LocalDateTime createDate;
    private LocalDateTime lastInactiveDate;
}
