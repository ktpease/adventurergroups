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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CharacterGroup
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    private Instance instance;

    private Boolean deleted;

    @Column(length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    private Integer colorPrimary;

    @OneToMany(mappedBy = "characterGroup", cascade = CascadeType.ALL)
    private Set<Character> characters;

    // Logging
    private LocalDateTime createDate;
    private LocalDateTime deleteDate;
}
