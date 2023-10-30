package welldressedmen.narispringboot.dto;


import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberInfo {
    private long memberIdx; //회원 고유번호

    private String memberSex; //성별
    private String memberCold; //추위성향
    private String memberHot; //더위성향
    private String memberPreferences; //선호스타일 목록

}