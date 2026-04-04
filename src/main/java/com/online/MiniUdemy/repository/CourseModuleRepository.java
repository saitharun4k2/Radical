package com.online.MiniUdemy.repository;

import com.online.MiniUdemy.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    
    // ADD THIS LINE: Allows us to fetch modules directly!
    List<CourseModule> findByCourseId(Long courseId);
}