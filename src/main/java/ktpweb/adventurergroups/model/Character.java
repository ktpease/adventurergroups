package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Character {
    private Long id;

    private Instance instance;

    private UserAccount maintainer;

    private CharacterGroup characterGroup;

    private String name;
    private String snippet;
    private Integer colorPrimary;
    private Integer colorSecondary;

    private UserAccount createdBy;
    private LocalDateTime createDate;
}
