// UserRepository is the layer that allows your application to communicate 
// with the users database table without you writing SQL manually using JPA

package com.rentalapp.user;

// jpaRepository includes CRUD and database operations needed for DB
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

//  SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
