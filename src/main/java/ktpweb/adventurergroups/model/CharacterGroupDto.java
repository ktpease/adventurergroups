package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;

import lombok.Data;

@Data
@JsonFilter("characterGroupFilter")
public class CharacterGroupDto
{
    private Long id;

    private String name;
    private String description;
    private Integer colorPrimary;

    private InstanceDto instance;
    private Set<CharacterDto> characters;

    private LocalDateTime createDate;
}
