package com.library.seatmanager.repository;

import com.library.seatmanager.dto.AccountStatus;
import com.library.seatmanager.dto.AdminRole;
import com.library.seatmanager.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByPhone(String phone);
    List<Admin> findAllByRole(AdminRole role);

    long countByRole(AdminRole role);

    long countByRoleAndStatus(
            AdminRole role,
            AccountStatus status
    );

}
