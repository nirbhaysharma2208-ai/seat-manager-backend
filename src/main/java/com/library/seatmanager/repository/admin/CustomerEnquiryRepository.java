package com.library.seatmanager.repository.admin;


import com.library.seatmanager.entity.admin.CustomerEnquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerEnquiryRepository
        extends JpaRepository<CustomerEnquiry, Long> {

    List<CustomerEnquiry> findAllByOrderByCreatedAtDesc();

    Optional<CustomerEnquiry> findByPhone(String phone);




}