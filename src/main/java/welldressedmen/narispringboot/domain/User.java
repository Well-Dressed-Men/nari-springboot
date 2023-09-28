package welldressedmen.narispringboot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_idx")
    private long userIdx;

    @Column(unique = true)
    private String userId; //[provider]+_+[providerID] ex : google_35363453655346

    private String userPwd;

    private String userEmail;

    private String userProviderId;
    private String userProvider;

    @OneToMany(mappedBy = "user") //-> 그림 : (일대다 그림), 읽기 : 관계노예 컬럼
    private Set<UserPreference> userPreferences = new HashSet<>();

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_idx"))
    @Enumerated(EnumType.STRING)
    private Set<Role> userRoles = new HashSet<>();

    public List<String> getRoleList() {
        List<String> roles = new ArrayList<>();
        for (Role role : this.userRoles) {
            roles.add(role.getRoleName());
        }
        return roles;
    }
}
