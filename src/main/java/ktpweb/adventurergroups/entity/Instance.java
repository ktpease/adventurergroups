package ktpweb.adventurergroups.entity;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Instance
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private Boolean active = false;
    
    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(length = 20)
    private String subdomainName;

    @Column(length = 50)
    private String displayName;

    @Column(length = 200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @OneToMany(mappedBy = "parentInstance", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<UserAccount> maintainers;
    
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Character> characters;

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<CharacterGroup> characterGroups;

    // Logging
    private LocalDateTime createDate;
    private LocalDateTime lastActivateDate;
    private LocalDateTime lastDeactivateDate;
}
