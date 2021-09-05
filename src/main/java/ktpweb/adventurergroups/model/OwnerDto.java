package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;

import lombok.Data;

@Data
@JsonFilter("ownerFilter")
public class OwnerDto
{
    private Long id;

    private String username;
    private String email;
    private String displayname;

    private LocalDateTime createDate;

    private Set<InstanceDto> instances;
}
