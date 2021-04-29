package ktpweb.adventurergroups.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Character is a reserved keyword in some databases.
@Entity(name = "characterobject")
@Getter
@Setter
@NoArgsConstructor
public class Character
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    private Instance instance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintainer_id")
    private UserAccount maintainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private CharacterGroup characterGroup;

    @Column(length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    private Integer colorPrimary;

    private Integer colorSecondary;

    // Logging

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private UserAccount createdBy;

    private LocalDateTime createDate;
    private LocalDateTime deleteDate;
}