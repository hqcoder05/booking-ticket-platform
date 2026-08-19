package com.booking_ticket_platform.auth.repository;

import com.booking_ticket_platform.auth.entity.User;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM User u WHERE
        (:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM bookings WHERE user_id = :userId
                UNION ALL SELECT 1 FROM refresh_tokens WHERE user_id = :userId
                UNION ALL SELECT 1 FROM notifications WHERE user_id = :userId
            )
            """, nativeQuery = true)
    boolean hasRelatedRecords(@Param("userId") UUID userId);
}
