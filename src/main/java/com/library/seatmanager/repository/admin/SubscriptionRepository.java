package com.library.seatmanager.repository.admin;




import com.library.seatmanager.dto.SubscriptionStatus;
import com.library.seatmanager.entity.admin.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByAdminIdOrderByExpiryDateDesc(Long adminId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    long countByStatus(SubscriptionStatus status);
}