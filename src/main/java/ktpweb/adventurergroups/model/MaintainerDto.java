package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;

import lombok.Data;

@Data
@JsonFilter("maintainerFilter")
public class MaintainerDto
{
    private Long id;

    private String username;
    private String email;
    private String displayname;
    private String avatarFilename;

    private LocalDateTime createDate;

    private Long parentInstanceId;

    private Set<Long> characterIds;

    private Boolean isRegistered;
    private String inviteToken;
}
