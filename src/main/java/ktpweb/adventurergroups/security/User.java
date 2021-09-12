package ktpweb.adventurergroups.security;

import java.util.ArrayList;

public class User extends org.springframework.security.core.userdetails.User
{
    private final Long id;

    public User(Long id, String username, String password)
    {
        super(username, password, new ArrayList<>());
        this.id = id;
    }

    public Long getId()
    {
        return this.id;
    }
}
