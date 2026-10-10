package com.library.seatmanager.repository.admin;


import com.library.seatmanager.entity.admin.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<com.library.seatmanager.entity.admin.Owner> findByEmail(String email);

    boolean existsByEmail(String email);
}
