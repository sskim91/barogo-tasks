package com.barogo.app.repository;

import com.barogo.app.domain.Delivery;
import com.barogo.app.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByIdAndUser(Long id, User user);

    Page<Delivery> findByUserAndRequestedAtBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);

}
