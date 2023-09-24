package welldressedmen.narispringboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import welldressedmen.narispringboot.config.auth.PrincipalDetails;
import welldressedmen.narispringboot.model.User;
import welldressedmen.narispringboot.repository.UserRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor 
public class RestApiController {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	
	// 권환없이 접근가능
	@GetMapping("home")
	public String home() {
		return "<h1>home</h1>";
	}

	// user, manager, admin 접근가능
	@GetMapping("user")
	public String user(Authentication authentication) { //JWT사용시, UserDetailsService호출X -> @AuthenticationPrincipal 사용 불가능
		PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
		System.out.println("id : "+principal.getUser().getId());
		System.out.println("username : "+principal.getUser().getUsername());
		System.out.println("password : "+principal.getUser().getPassword());
		System.out.println("roles : "+principal.getUser().getRoles());
		return "user";
	}
	
	// manager, admin 접근 가능
	@GetMapping("manager/reports")
	public String reports() {
		return "reports";
	}
	
	// admin 접근 가능
	@GetMapping("admin/users")
	public List<User> users(){
		return userRepository.findAll();
	}
	
	@PostMapping("join")
	public String join(@RequestBody User user) {
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setRoles("ROLE_USER");
		userRepository.save(user);
		return "회원가입완료";
	}
	
}











