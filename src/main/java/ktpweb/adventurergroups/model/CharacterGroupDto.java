package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class CharacterGroupDto
{
    private Long id;

    private InstanceDto instance;

    private String name;
    private String description;
    private Integer colorPrimary;

    private Set<Character> characters;

    private LocalDateTime createDate;
}
