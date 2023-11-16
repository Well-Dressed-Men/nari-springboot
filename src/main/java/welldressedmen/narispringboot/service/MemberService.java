package welldressedmen.narispringboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import welldressedmen.narispringboot.domain.Member;
import welldressedmen.narispringboot.dto.UpdateRequestDTO;
import welldressedmen.narispringboot.dto.MemberInfo;
import welldressedmen.narispringboot.repository.MemberRepository;

@Service
@Slf4j
public class MemberService {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ObjectMapper objectMapper;

    //memberId를 통해서 사용자 정보(Member객체) 추출
    public MemberInfo getUserInfo(String userId){
        Member member = memberRepository.findByMemberId(userId).get();
        return buildMemberInfo(member);
    }

    private MemberInfo buildMemberInfo(Member member) {
        return MemberInfo.builder()
                .memberIdx(member.getMemberIdx())
                .memberSex(member.getMemberSex()) //ex : "남"
                .memberCold(member.getMemberCold()) //ex : "매우 그렇다"
                .memberHot(member.getMemberHot()) //ex : "매우 그렇지 않다"
                .memberPreferences(member.getMemberPreferences()) //ex : "캐쥬얼 스트릿"
                .build();
    }

    //userSex, userCold, userHot, userPreferences를 저장
    public void updateMemberInfo(UpdateRequestDTO updateRequestDTO, String memberId){
        //사용자 정보 가져오기
        Member memberToUpdate = memberRepository.findByMemberId(memberId).get();

        // 사용자 정보 업데이트(추위성향, 더위성향, 체형)
        memberToUpdate.setMemberSex(updateRequestDTO.getMemberSex());
        memberToUpdate.setMemberCold(updateRequestDTO.getMemberCold());
        memberToUpdate.setMemberHot(updateRequestDTO.getMemberHot());
        memberToUpdate.setMemberBody(updateRequestDTO.getMemberBody());
        memberToUpdate.setMemberPreferences(updateRequestDTO.getMemberPreferences());

        memberRepository.update(memberToUpdate);
    }
    //Member못찾는 예외처리 X이유 : 인증과정을 완료하여, 시큐리티 세션에 등록된 사용자를 대상하므로, user가 null이 됨으로인한 NullPointerException이 발생할 수 없다.


}
