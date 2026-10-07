package com.ledgr.sandbox.repository;

import com.ledgr.sandbox.entity.ConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentRepository extends JpaRepository<ConsentEntity, String> {
    List<ConsentEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
