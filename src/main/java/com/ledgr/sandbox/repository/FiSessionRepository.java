package com.ledgr.sandbox.repository;

import com.ledgr.sandbox.entity.FiSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiSessionRepository extends JpaRepository<FiSessionEntity, String> {
    Optional<FiSessionEntity> findBySessionId(String sessionId);
}
