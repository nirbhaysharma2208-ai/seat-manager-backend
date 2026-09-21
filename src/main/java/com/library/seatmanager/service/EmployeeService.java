package com.library.seatmanager.service;

import com.library.seatmanager.dto.CreateEmployeeRequest;
import com.library.seatmanager.dto.EmployeeResponse;
import com.library.seatmanager.dto.UpdateEmployeeRequest;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.entity.Employee;
import com.library.seatmanager.entity.Library;
import com.library.seatmanager.repository.AdminRepository;
import com.library.seatmanager.repository.EmployeeRepository;
import com.library.seatmanager.repository.LibraryRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LibraryRepository libraryRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;


    public EmployeeService(
            EmployeeRepository employeeRepository,
            LibraryRepository libraryRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository = employeeRepository;
        this.libraryRepository = libraryRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =========================================================
    // CREATE EMPLOYEE
    // =========================================================

    @Transactional
    public EmployeeResponse createEmployee(
            Long libraryId,
            CreateEmployeeRequest request,
            String adminPhone
    ) {

        Admin admin = getAdmin(adminPhone);

        Library library = getLibrary(libraryId);

        verifyLibraryOwnership(library, admin);

        validateCreateRequest(request);

        String username = request.getUsername().trim().toLowerCase();

        // Because username uniqueness is library-specific,
        // we check employees belonging to this library.
        List<Employee> employees =
                employeeRepository.findByLibraryIdOrderByNameAsc(libraryId);

        boolean usernameExists = employees.stream()
                .anyMatch(employee ->
                        employee.getUsername()
                                .equalsIgnoreCase(username)
                );

        if (usernameExists) {
            throw new RuntimeException(
                    "Username already exists in this library"
            );
        }


        Employee employee = new Employee();

        employee.setName(request.getName().trim());

        employee.setPhone(
                request.getPhone() == null
                        ? null
                        : request.getPhone().trim()
        );

        employee.setUsername(username);

        employee.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        employee.setRole(request.getRole());

        employee.setActive(true);

        employee.setLibrary(library);


        Employee savedEmployee =
                employeeRepository.save(employee);

        return EmployeeResponse.from(savedEmployee);
    }


    // =========================================================
    // GET ALL EMPLOYEES
    // =========================================================

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployees(
            Long libraryId,
            String adminPhone
    ) {

        Admin admin = getAdmin(adminPhone);

        Library library = getLibrary(libraryId);

        verifyLibraryOwnership(library, admin);


        return employeeRepository
                .findByLibraryIdOrderByNameAsc(libraryId)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }


    // =========================================================
    // GET SINGLE EMPLOYEE
    // =========================================================

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(
            Long employeeId,
            Long libraryId,
            String adminPhone
    ) {

        Admin admin = getAdmin(adminPhone);

        Library library = getLibrary(libraryId);

        verifyLibraryOwnership(library, admin);


        Employee employee =
                employeeRepository
                        .findByIdAndLibraryId(employeeId, libraryId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                )
                        );


        return EmployeeResponse.from(employee);
    }


    // =========================================================
    // UPDATE EMPLOYEE
    // =========================================================

    @Transactional
    public EmployeeResponse updateEmployee(
            Long employeeId,
            Long libraryId,
            UpdateEmployeeRequest request,
            String adminPhone
    ) {

        Admin admin = getAdmin(adminPhone);

        Library library = getLibrary(libraryId);

        verifyLibraryOwnership(library, admin);


        Employee employee =
                employeeRepository
                        .findByIdAndLibraryId(employeeId, libraryId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                )
                        );


        if (request.getName() != null
                && !request.getName().trim().isEmpty()) {

            employee.setName(
                    request.getName().trim()
            );
        }


        if (request.getPhone() != null) {

            employee.setPhone(
                    request.getPhone().trim()
            );
        }


        if (request.getUsername() != null
                && !request.getUsername().trim().isEmpty()) {

            String newUsername =
                    request.getUsername()
                            .trim()
                            .toLowerCase();


            if (!newUsername.equalsIgnoreCase(
                    employee.getUsername()
            )) {

                boolean usernameExists =
                        employeeRepository
                                .findByLibraryIdOrderByNameAsc(libraryId)
                                .stream()
                                .anyMatch(existing ->
                                        existing.getId()
                                                .longValue()
                                                != employeeId
                                                &&
                                                existing.getUsername()
                                                        .equalsIgnoreCase(
                                                                newUsername
                                                        )
                                );


                if (usernameExists) {
                    throw new RuntimeException(
                            "Username already exists in this library"
                    );
                }
            }


            employee.setUsername(newUsername);
        }


        if (request.getRole() != null) {

            employee.setRole(
                    request.getRole()
            );
        }


        if (request.getPassword() != null
                && !request.getPassword().trim().isEmpty()) {

            employee.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }


        if (request.getActive() != null) {

            employee.setActive(
                    request.getActive()
            );
        }


        Employee updated =
                employeeRepository.save(employee);


        return EmployeeResponse.from(updated);
    }


    // =========================================================
    // ACTIVATE / DEACTIVATE
    // =========================================================

    @Transactional
    public EmployeeResponse updateStatus(
            Long employeeId,
            Long libraryId,
            boolean active,
            String adminPhone
    ) {

        Admin admin = getAdmin(adminPhone);

        Library library = getLibrary(libraryId);

        verifyLibraryOwnership(library, admin);


        Employee employee =
                employeeRepository
                        .findByIdAndLibraryId(
                                employeeId,
                                libraryId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                )
                        );


        employee.setActive(active);

        Employee updated =
                employeeRepository.save(employee);


        return EmployeeResponse.from(updated);
    }


    // =========================================================
    // DELETE EMPLOYEE
    // =========================================================

    @Transactional
    public void deleteEmployee(
            Long employeeId,
            Long libraryId,
            String adminPhone
    ) {

        Admin admin = getAdmin(adminPhone);

        Library library = getLibrary(libraryId);

        verifyLibraryOwnership(library, admin);


        Employee employee =
                employeeRepository
                        .findByIdAndLibraryId(
                                employeeId,
                                libraryId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                )
                        );


        employeeRepository.delete(employee);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private Admin getAdmin(String phone) {

        return adminRepository
                .findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Admin not found"
                        )
                );
    }


    private Library getLibrary(Long libraryId) {

        return libraryRepository
                .findById(libraryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Library not found"
                        )
                );
    }


    private void verifyLibraryOwnership(
            Library library,
            Admin admin
    ) {

        if (library.getAdmin() == null
                || !library.getAdmin()
                .getId()
                .equals(admin.getId())) {

            throw new RuntimeException(
                    "You do not have access to this library"
            );
        }
    }


    private void validateCreateRequest(
            CreateEmployeeRequest request
    ) {

        if (request.getName() == null
                || request.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Employee name is required"
            );
        }


        if (request.getUsername() == null
                || request.getUsername().trim().isEmpty()) {

            throw new RuntimeException(
                    "Username is required"
            );
        }


        if (request.getPassword() == null
                || request.getPassword().trim().isEmpty()) {

            throw new RuntimeException(
                    "Password is required"
            );
        }


        if (request.getRole() == null) {

            throw new RuntimeException(
                    "Employee role is required"
            );
        }
    }
}