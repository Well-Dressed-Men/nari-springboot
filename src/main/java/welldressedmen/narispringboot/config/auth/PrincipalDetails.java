package welldressedmen.narispringboot.config.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import welldressedmen.narispringboot.domain.Member;

import java.util.ArrayList;
import java.util.Collection;

public class PrincipalDetails implements UserDetails{

	private Member member;

    public PrincipalDetails(Member member){
        this.member = member;
    }

    public Member getUser() {
		return member;
	}

    @Override
    public String getPassword() {
        return member.getMemberPwd();
    }

    @Override
    public String getUsername() {
        return member.getMemberId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        member.getRoleList().forEach(r -> {
        	authorities.add(()->{ return r;});
        });
        return authorities;
    }
}
