package ktpweb.adventurergroups.entity;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Entity
@Data
public class Instance
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private Boolean isDeleted;

    private String subdomainName;
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @OneToMany(mappedBy = "parentInstance", cascade = CascadeType.ALL)
    private Set<UserAccount> maintainers;
    
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL)
    private Set<CharacterGroup> characterGroup;

    // Logging
    private LocalDateTime createDate;
    private LocalDateTime deleteDate;
}
