package ktpweb.adventurergroups.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AdminAccount
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 20)
    private String username;

    private String password;

    @Column(length = 50)
    private String email;

    @Column(length = 50)
    private String displayname;

    // Logging
    private LocalDateTime createDate;
    private LocalDateTime deleteDate;
    private LocalDateTime lastActivateDate;
    private LocalDateTime lastDeactivaeDate;
}
