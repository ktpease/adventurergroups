package ktpweb.adventurergroups.entity;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import ktpweb.adventurergroups.util.UserAccountUtils.UserAccountRoles;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Entity
@Data
public class UserAccount
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private Boolean isDeleted;

    @Enumerated(EnumType.ORDINAL)
    private UserAccountRoles role;

    private String username;
    private String password;
    private String email;
    private String displayname;

    // Multi-instance (Owners)
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Instance> instances;

    // Single-instance (Maintainers)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private Instance parentInstance;

    @OneToMany(mappedBy = "maintainer", cascade = CascadeType.ALL)
    private Set<Character> characters;

    private String inviteToken;

    // Logging
    private LocalDateTime createDate;
    private LocalDateTime deleteDate;
}
