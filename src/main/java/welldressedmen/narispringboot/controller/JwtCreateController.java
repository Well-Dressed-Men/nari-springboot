package welldressedmen.narispringboot.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import welldressedmen.narispringboot.config.jwt.JwtProperties;
import welldressedmen.narispringboot.config.oauth.provider.GoogleUser;
import welldressedmen.narispringboot.config.oauth.provider.OAuthUserInfo;
import welldressedmen.narispringboot.model.User;
import welldressedmen.narispringboot.repository.UserRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwtCreateController {
	private final UserRepository userRepository;

	// 발동상황 : 프론트에서 Oauth2.0 로그인 성공이후 사용자 정보를 SpringAPI로 전달
	@PostMapping("/oauth/jwt/google")
	public ResponseEntity<Map<String, String>> jwtCreate(@RequestBody Map<String, Object> data) {
		System.out.println("[in jwtCreate method] 실행");
		System.out.println(data); //credential, clientId, select_by
		/*
		data - profileObj - provider ex)google
							providerId ex)35378
							email ex)heenam4225@gmail.com
							name ex)강희남
		 */
		OAuthUserInfo googleUser = new GoogleUser((Map<String, Object>)data.get("profileObj"));


		//해당 사용자의 정보가 DB에 있는지 확인(for 최초로그인 여부 확인)
		User userEntity = userRepository.findByUsername(googleUser.getProvider()+"_"+googleUser.getProviderId());

		//상황 : 최초로그인
		if(userEntity == null) {
			System.out.println("userEntity == null -> 최초로그인");
			User userRequest = User.builder()
					.username(googleUser.getProvider()+"_"+googleUser.getProviderId())
					.password(null)
					.email(googleUser.getEmail())
					.provider(googleUser.getProvider())
					.providerId(googleUser.getProviderId())
					.roles("ROLE_USER")
					.build();
			
			userEntity = userRepository.save(userRequest);
		}

		//토큰 생성
		String jwtToken = JWT.create()
				.withSubject(userEntity.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis()+ JwtProperties.EXPIRATION_TIME))
				.withClaim("id", userEntity.getId())
				.withClaim("username", userEntity.getUsername())
				.sign(Algorithm.HMAC512(JwtProperties.SECRET));

		Date tokenExpired = new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME);
		System.out.println("토큰 만료시간 : "+tokenExpired);
		System.out.println("토큰 : "+jwtToken);

		/*
		refreshToken 기능 보류

		String refreshToken = JWT.create()
				.withSubject(userEntity.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.REFRESH_EXPIRATION_TIME)) // 긴 만료 시간 설정
				.withClaim("id", userEntity.getId())
				.sign(Algorithm.HMAC512(JwtProperties.REFRESH_SECRET)); // 다른 secret을 사용하여 토큰을 서명
		 */

		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("jwtToken", jwtToken);

		// ResponseEntity를 사용하여 JSON 형태로 응답 반환
		return ResponseEntity.ok(responseMap); //생성한 JWT토큰을 프론트로 반환
	}
}
