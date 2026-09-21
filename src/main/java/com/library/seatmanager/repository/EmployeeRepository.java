package com.library.seatmanager.repository;

import com.library.seatmanager.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {

    Optional<Employee> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Employee> findByLibraryIdOrderByNameAsc(
            Long libraryId
    );

    Optional<Employee> findByIdAndLibraryId(
            Long id,
            Long libraryId
    );

    boolean existsByLibraryIdAndUsernameIgnoreCase(
            Long libraryId,
            String username
    );
}