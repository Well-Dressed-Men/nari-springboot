package welldressedmen.narispringboot.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import welldressedmen.narispringboot.config.auth.PrincipalDetails;
import welldressedmen.narispringboot.domain.Member;
import welldressedmen.narispringboot.repository.MemberRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 인가 관련
// 역할 : 모든 http요청에 대해 발동, 프론트에서 Authorization헤더에 토큰유형+토큰값과 함께 요청해올때, 인증인가과정 수행
// - 요청 헤더중 Authorization 헤더값을 추출해서 Bearer(지정한 토큰 유형)으로 요청했는지 확인
// - 토큰 검증(ifO) / 다음 필터 넘어가기(ifX)
// - 시큐리티 세션에 사용자 정보 등록(Authentication 객체 삽입)
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
	private MemberRepository memberRepository;
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
		super(authenticationManager);
		this.memberRepository = memberRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String header = request.getHeader(JwtProperties.HEADER_STRING);
		log.info("[in JWT인가필터] Authorization 헤더 검사 : = {}",header);

		if (!validateHeader(header)) {
			chain.doFilter(request, response);
			return;
		}

		String username = verifyToken(header);

		//상황 : 검증완료 상황
		if (username != null) authenticateUser(username);

		chain.doFilter(request, response); //다음 필터 이동
	}

	private boolean validateHeader(String header){
		return !(header == null) && header.startsWith(JwtProperties.TOKEN_TYPE);
	}

	private String verifyToken(String header){
		//순수JWT토큰값 추출(토큰유형 값 제거)(for 검증)
		String token = header.replace(JwtProperties.TOKEN_TYPE, "");

		// 토큰 검증 &username클레임 추출(해당코드에서 인증역할도 수행하기 때문에 AuthenticationManager 필요X)
		return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token)
				.getClaim("username").asString();
	}

	 private void authenticateUser(String username) {
		Member member = memberRepository.findByMemberId(username).get();

		// 권한부여를 위해 시큐리티 세션에 저장(인증은 토큰으로 이미 검증됨)(-> Controller에서 DI하여 사용 가능)
		PrincipalDetails principalDetails = new PrincipalDetails(member); //DB사용자 정보로 PrincipalDetails객체 생성

		// 인증되지 않은 상태로 생성됨(Authentication manager에 의해 인증과정을 맞춘뒤 '인증된'새로운 Authentication객체가 생성)
		Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails,
				null, // 패스워드 삽입X(해당 토큰으로는 인증에 사용되지 않음, jwt토큰에 의해 이미 검증, 인증되지 않은 Authentication객체 생성되도 상관X)
				principalDetails.getAuthorities());

		//'능동적으로'시큐리티 세션에 접근후 인증객체 저장(vs UserDetailService loadUserByUsername : return UserDetails시 자동으로 시큐리티세션에 저장)
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
