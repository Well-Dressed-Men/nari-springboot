
package welldressedmen.narispringboot.repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import welldressedmen.narispringboot.domain.Member;

import java.util.Optional;

@Repository
@Transactional
@Slf4j
public class MemberRepositoryImpl implements MemberRepository {
    @PersistenceContext
    private EntityManager em;
    @Override
    public Member save(Member member){
        em.persist(member);
        return member;
    }

    @Override
    public Optional<Member> findByMemberId(String userId) { //상황 : 인증, 시큐리티 세션 등록된 사용자 대상으로 사용자 정보가져올때
        // jpql생성
        String jpql = "SELECT u FROM Member u WHERE u.memberId = :userId";

        // TypedQuery 생성
        TypedQuery<Member> query = em.createQuery(jpql, Member.class);
        query.setParameter("userId", userId);

        //쿼리 실행
        try{
            return Optional.of(query.getSingleResult());
        }catch(NoResultException e){
            return Optional.empty();
        }catch(Exception e) { //데이터베이스 오류
            log.error("Unexpected error during database query", e);
            throw new RuntimeException("Unexpected error during database query", e);
        }
    }
    // NonUniqueResultException 예외처리 X이유 : User테이블내 userId가 유일한 것이 보장되어있음(<- @Column(unique = true) private String userId)

    @Override
    public Member update(Member member) {
        return em.merge(member);
    }
}

