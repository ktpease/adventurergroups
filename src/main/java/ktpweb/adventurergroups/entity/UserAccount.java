package ktpweb.adventurergroups.entity;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import ktpweb.adventurergroups.util.UserAccountUtils.UserAccountRoles;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserAccount
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private Boolean deleted;

    @Enumerated(EnumType.ORDINAL)
    private UserAccountRoles role;

    @Column(length = 20)
    private String username;

    private String password;

    @Column(length = 50)
    private String email;

    @Column(length = 50)
    private String displayname;

    // Multi-instance (Owners)
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Instance> instances;

    // Single-instance (Maintainers)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    private Instance parentInstance;

    @OneToMany(mappedBy = "maintainer", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Character> characters;

    private String inviteToken;

    // Logging
    private LocalDateTime createDate;
    private LocalDateTime deleteDate;
}
