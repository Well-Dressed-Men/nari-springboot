package welldressedmen.narispringboot.repository;

import welldressedmen.narispringboot.domain.Member;

import java.util.Optional;

public interface MemberRepository {
	Member save(Member member);
	Optional<Member> findByMemberId(String username);
	Member update(Member member);
}
