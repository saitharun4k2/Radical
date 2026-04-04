package com.online.MiniUdemy.entity;

import com.online.MiniUdemy.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean enabled = true;
    
 // Add this right below enrolledCourses
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "completed_lessons",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "lesson_id")
    )
    private Set<Lesson> completedLessons = new HashSet<>();

    // Add these to your Getters & Setters at the bottom:
    public Set<Lesson> getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(Set<Lesson> completedLessons) { this.completedLessons = completedLessons; }
    
 // ADD THIS NEW RELATIONSHIP FOR STUDENTS
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "course_enrollments",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private java.util.Set<Course> enrolledCourses = new java.util.HashSet<>();

    // Add the Getters and Setters for it at the bottom of the file!
    public java.util.Set<Course> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(java.util.Set<Course> enrolledCourses) { this.enrolledCourses = enrolledCourses; }
}
