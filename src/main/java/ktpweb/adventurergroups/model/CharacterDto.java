package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFilter;

import lombok.Data;

@Data
@JsonFilter("characterFilter")
public class CharacterDto
{
    private Long id;

    private String name;
    private String description;
    private Integer colorPrimary;
    private Integer colorSecondary;
    private String avatarFilename;

    private InstanceDto instance;
    private MaintainerDto maintainer;
    private CharacterGroupDto characterGroup;

    private LocalDateTime createDate;
}
