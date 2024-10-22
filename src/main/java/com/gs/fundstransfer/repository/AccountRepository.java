package com.gs.fundstransfer.repository;

import com.gs.fundstransfer.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select a from Account a where a.ownerId = :id")
        Optional<Account> findByIdWithLock(Long id);
}