package com.online.MiniUdemy.repository;

import com.online.MiniUdemy.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}