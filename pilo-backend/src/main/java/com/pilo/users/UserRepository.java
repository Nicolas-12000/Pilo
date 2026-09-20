package com.pilo.users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmailIgnoreCase(String email);

	List<User> findAllByOrderByEmailAsc();

	long countByRoleAndEnabledTrue(Role role);
}
