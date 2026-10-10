package com.ledgr.repository;

import com.ledgr.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    List<AccountEntity> findByFiType(String fiType);

    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.transactions ORDER BY a.accountNumber ASC")
    List<AccountEntity> findAllWithTransactions();
}
