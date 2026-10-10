
package com.library.seatmanager.controller.admin;

import com.library.seatmanager.dto.AdminRole;
import com.library.seatmanager.dto.admin.OwnerCustomerResponse;
import com.library.seatmanager.entity.Admin;
import com.library.seatmanager.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerCustomerController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<OwnerCustomerResponse>> getCustomers() {
        List<OwnerCustomerResponse> customers =
                adminRepository.findAllByRole(AdminRole.ADMIN)
                        .stream()
                        .map(OwnerCustomerResponse::from)
                        .toList();

        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<OwnerCustomerResponse> getCustomer(
            @PathVariable Long id) {

        Admin admin = findCustomer(id);

        return ResponseEntity.ok(OwnerCustomerResponse.from(admin));
    }

    @PatchMapping("/{id}/phone")
    @Transactional
    public ResponseEntity<?> changeCustomerPhone(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String phone = request.get("phone");

        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Phone is required"));
        }

        phone = phone.trim();

        if (adminRepository.findByPhone(phone).isPresent()
                && !phone.equals(findCustomer(id).getPhone())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Phone is already registered"));
        }

        Admin customer = findCustomer(id);
        customer.setPhone(phone);
        adminRepository.save(customer);

        return ResponseEntity.ok(Map.of(
                "message", "Customer phone updated successfully"
        ));
    }

    @PatchMapping("/{id}/password")
    @Transactional
    public ResponseEntity<?> resetCustomerPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String newPassword = request.get("newPassword");

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Password must contain at least 8 characters"
                    ));
        }

        Admin customer = findCustomer(id);

        // Store only the encoded password, never plaintext.
        customer.setPassword(passwordEncoder.encode(newPassword));

        // Invalidate any pending OTP when credentials change.
        customer.setOtp(null);
        customer.setOtpExpiry(null);

        adminRepository.save(customer);

        return ResponseEntity.ok(Map.of(
                "message", "Customer password reset successfully"
        ));
    }

    private Admin findCustomer(Long id) {
        return adminRepository.findById(id)
                .filter(admin -> admin.getRole() == AdminRole.ADMIN)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Customer not found"
                        ));
    }
}
