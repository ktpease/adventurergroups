package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdminAccountDto
{
    private Long id;

    private Boolean active;

    private String username;
    private String email;
    private String displayname;

    private LocalDateTime createDate;
    private LocalDateTime lastActivateDate;
    private LocalDateTime lastDeactivateDate;
}
