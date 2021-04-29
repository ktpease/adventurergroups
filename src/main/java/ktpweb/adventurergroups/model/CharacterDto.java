package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CharacterDto {
    private Long id;

    private InstanceDto instance;

    private UserAccountDto maintainer;

    private CharacterGroupDto characterGroup;

    private String name;
    private String description;
    private Integer colorPrimary;
    private Integer colorSecondary;

    private UserAccountDto createdBy;
    private LocalDateTime createDate;
}
