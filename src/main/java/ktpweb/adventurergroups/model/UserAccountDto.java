package ktpweb.adventurergroups.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserAccountDto
{
    private Long id;

    private String username;
    private String email;
    private String displayname;

    private LocalDateTime createDate;
}
