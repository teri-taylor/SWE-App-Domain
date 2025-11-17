package org.example;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountName(String name);
    boolean existsByAccountNumber(String number);

    Optional<Account> findByAccountNumber(String number);

    List<Account> findByAccountNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrSubcategoryContainingIgnoreCase(
            String name, String number, String category, String subcategory);
}
