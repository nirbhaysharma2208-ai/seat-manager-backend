package com.library.seatmanager.repository.admin;



import com.library.seatmanager.entity.admin.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {
}