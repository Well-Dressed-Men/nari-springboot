package welldressedmen.narispringboot.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_idx")
    private Integer preferenceIdx;

    private String preference;

    @OneToMany(mappedBy = "preference") //-> 그림 : (일대다 그림), 읽기 : 관계노예 컬럼(노예 : 주인X)
    private Set<UserPreference> userPreferences = new HashSet<>();

}