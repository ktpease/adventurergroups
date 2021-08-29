package ktpweb.adventurergroups.model;

import lombok.Data;

@Data
public class UserAccountDto
{
    private String username;
    private String password;
    private String email;
    private String displayname;
    private String avatarFilename;
}