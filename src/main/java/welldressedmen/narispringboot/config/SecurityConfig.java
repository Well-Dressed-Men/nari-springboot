package welldressedmen.narispringboot.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import welldressedmen.narispringboot.config.jwt.JwtAuthorizationFilter;
import welldressedmen.narispringboot.repository.UserRepository;

@Configuration
@EnableWebSecurity // 시큐리티 활성화 -> 기본 스프링 필터체인에 등록
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CorsConfig corsConfig;
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.addFilter(corsConfig.corsFilter())
				.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
				.formLogin().disable() //Rest API 의 형식으로 Json 으로만 데이터를 주고 받는 Stateless 한 통신방식을 사용할 예정이기 때문
				.httpBasic().disable()

				.addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository)) //순서가 정해져있는 스프링필터를 상속받는 경우 -> addFilter(), 순서가 정해져있지 않는 스프링필터를 상속받는 경우 -> addFilterBefore(), addFilterAfter()

				.authorizeRequests()
				.antMatchers("/user/**")
					.access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
				.antMatchers("/manager/**")
					.access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
				.antMatchers("/admin/**")
					.access("hasRole('ROLE_ADMIN')")
				.anyRequest().permitAll();
	}
}






