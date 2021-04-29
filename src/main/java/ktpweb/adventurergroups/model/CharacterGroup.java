package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class CharacterGroup {
    private Long id;

    private Instance instance;

    private String name;
    private String description;
    private Integer colorPrimary;

    private Set<Character> characters;

    private LocalDateTime createDate;
}
