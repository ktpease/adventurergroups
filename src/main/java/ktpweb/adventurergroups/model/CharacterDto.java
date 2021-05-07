package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CharacterDto
{
    private Long id;

    private Long instanceId;
    private Long maintainerId;
    private Long characterGroupId;

    private String name;
    private String description;
    private Integer colorPrimary;
    private Integer colorSecondary;

    private UserAccountDto createdBy;
    private LocalDateTime createDate;
}
