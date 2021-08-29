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

    private Long instanceId;
    private Long maintainerId;
    private Long characterGroupId;

    private LocalDateTime createDate;
}
