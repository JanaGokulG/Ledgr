package com.ledgr.repository;

import com.ledgr.entity.RecurringRenewalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringRenewalRepository extends JpaRepository<RecurringRenewalEntity, String> {

    List<RecurringRenewalEntity> findByActiveTrueOrderByDueDateAsc();

    List<RecurringRenewalEntity> findByFundingAccountNumberAndActiveTrueOrderByDueDateAsc(String fundingAccountNumber);
}
