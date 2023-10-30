package welldressedmen.narispringboot.repository;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import welldressedmen.narispringboot.domain.Member;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MemberRepositoryImpl.class))
@ActiveProfiles("test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberRepositoryImplTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager tem;

    @BeforeAll
    void setUp() {

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        Member testMember = Member.builder()
                .userId("google_123456")
                .userPwd(null)
                .userEmail("heenam4225@gmail.com")
                .userProvider("google")
                .userProviderId("123456")

                .userRoles(roles)
                .build();

        memberRepository.save(testMember);
    }

    @Test
    public void findByUserId() {
        // given
        String expectedUserId = "google_123456";
        String expectedEmail = "heenam4225@gmail.com";
        Set<Role> expectedRoles = new HashSet<>();
        expectedRoles.add(Role.ROLE_USER);

        // when
        Member foundMember = memberRepository.findByMemberId(expectedUserId).get();

        // then
        assertNotNull(foundMember, "User should not be null");
        assertEquals(expectedUserId, foundMember.getMemberId(), "User ID should match");
        assertEquals(expectedEmail, foundMember.getMemberEmail(), "User email should match");
        assertEquals(expectedRoles, foundMember.getMemberRoles(), "User roles should match");
    }


    @Test
    public void update() {
        // given
        String initialUserId = "google_123456";

        Optional<Member> optionalUser = memberRepository.findByMemberId(initialUserId);
        assertTrue(optionalUser.isPresent(), "User should be found for ID: " + initialUserId);

        Member memberToUpdate = optionalUser.get();

        // 업데이트 필드
        String userColdToUpdate = "매우 그렇지 않다";
        String userHotToUpdate = "그렇지 않다";
        String userBodyToUpdate = "근육있는";
        Set<UserPreference> userPreferences = createUserPreferences();

        updateUserFields(memberToUpdate, userColdToUpdate, userHotToUpdate, userBodyToUpdate, userPreferences);

        // when
        Member updatedMember = memberRepository.update(memberToUpdate);

        // then
        assertNotNull(updatedMember);
        assertEquals(userColdToUpdate, updatedMember.getMemberCold(), "UserCold should be updated");
        assertEquals(userHotToUpdate, updatedMember.getMemberHot(), "UserHot should be updated");
        assertEquals(userBodyToUpdate, updatedMember.getMemberBody(), "UserBody should be updated");
        assertEquals(userPreferences, updatedMember.getMemberPreferences(), "UserPreferences should be updated");
    }

}
