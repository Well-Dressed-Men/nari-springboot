package welldressedmen.narispringboot.domain;


import javax.persistence.*;

@Entity
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userPreferenceIdx;

    @ManyToOne //-> 그림 : (다대일 그림), 읽기 : 관계주인 컬럼
    @JoinColumn(name = "user_idx", referencedColumnName = "user_idx") //-> 그림 : (외래키이름 : 'user_idx', 피참조컬럼이름 : "user_idx")
    private User user;

    @ManyToOne
    @JoinColumn(name = "preference_idx", referencedColumnName = "preference_idx")
    private Preference preference;

}