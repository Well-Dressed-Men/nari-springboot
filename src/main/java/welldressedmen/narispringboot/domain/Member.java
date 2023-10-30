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
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_idx")
    private long memberIdx;

    @Column(unique = true)
    private String memberId; //[provider]+_+[providerID] ex : google_35363453655346

    private String memberPwd;

    private String memberEmail;

    private String memberProviderId;
    private String memberProvider;

    private String memberSex;

    private String memberCold;
    private String memberHot;

    private String memberBody;

    private String memberPreferences; //사용자 선호 스타일이 공백으로 이어져있는 String
    private String memberRoles; //사용자 역할이 공백으로 이어져있는 String

    public List<String> getPreferenceList() {
        return Arrays.asList(this.memberPreferences.split(" "));
    }

    public List<String> getRoleList() {
        return Arrays.asList(this.memberRoles.split(" "));
    }
}
