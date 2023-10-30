package welldressedmen.narispringboot.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import welldressedmen.narispringboot.config.jwt.JwtProperties;
import welldressedmen.narispringboot.config.oauth.provider.GoogleUser;
import welldressedmen.narispringboot.config.oauth.provider.OAuthUserInfo;
import welldressedmen.narispringboot.domain.Member;
import welldressedmen.narispringboot.repository.MemberRepository;

import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class JwtCreateController {
	private final MemberRepository memberRepository;

	// 발동상황 : 프론트에서 Oauth2.0 로그인 성공이후 사용자 정보를 SpringAPI로 전달
	@PostMapping("/oauth/jwt/google")
	public ResponseEntity<Map<String, String>> jwtCreate(@RequestBody Map<String, Object> data) {
		System.out.println("[in jwtCreate method] 실행");
		System.out.println(data); //credential, clientId, select_by
		/*
		data - profileObj - provider ex)google
							providerId ex)35378
							email ex)heenamgoogleId@gmail.com
							name ex)강희남
		 */
		OAuthUserInfo googleUser = new GoogleUser((Map<String, Object>)data.get("profileObj"));

		//해당 사용자의 정보가 DB에 있는지 확인(for 최초로그인 여부 확인)
		Optional<Member> userOptionalEntity = memberRepository.findByMemberId(googleUser.getProvider()+"_"+googleUser.getProviderId());

		Member memberEntity;

		// 상황 : 최초로그인
		if(userOptionalEntity.isEmpty()) {
			log.info("userEntity == null -> 최초로그인");
			Member memberRequest = buildUser(googleUser);

			memberEntity = memberRepository.save(memberRequest);
		} else {
			memberEntity = userOptionalEntity.get();
		}

		Map<String, String> responseMap = createToken(memberEntity);

		return ResponseEntity.ok(responseMap); //생성한 JWT토큰을 프론트로 반환 by ResponseEntity사용해서 JSON형태

	}

	static Member buildUser(OAuthUserInfo googleUser){

		return Member.builder()
				.memberId(googleUser.getProvider()+"_"+googleUser.getProviderId())
				.memberPwd(null)
				.memberEmail(googleUser.getEmail())
				.memberProvider(googleUser.getProvider())
				.memberProviderId(googleUser.getProviderId())
				.memberRoles("ROLE_USER")
				.build();
	}

	static Map<String, String> createToken(Member memberEntity){
		//토큰 생성
		String jwtToken = JWT.create()
				.withSubject(memberEntity.getMemberId())
				.withExpiresAt(new Date(System.currentTimeMillis()+ JwtProperties.EXPIRATION_TIME))
				.withClaim("id", memberEntity.getMemberIdx())
				.withClaim("username", memberEntity.getMemberId())
				.sign(Algorithm.HMAC512(JwtProperties.SECRET));

		Date tokenExpired = new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME);
		System.out.println("토큰 만료시간 : "+tokenExpired);
		System.out.println("토큰 : "+jwtToken);

		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("jwtToken", jwtToken);
		return responseMap;
	}
}
/*
		refreshToken 기능 보류

		String refreshToken = JWT.create()
				.withSubject(userEntity.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.REFRESH_EXPIRATION_TIME)) // 긴 만료 시간 설정
				.withClaim("id", userEntity.getId())
				.sign(Algorithm.HMAC512(JwtProperties.REFRESH_SECRET)); // 다른 secret을 사용하여 토큰을 서명
		 */
