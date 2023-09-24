package welldressedmen.narispringboot.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import welldressedmen.narispringboot.domain.User;

public interface UserRepository extends JpaRepository<User, Long>{
	User findByUsername(String username);
}
