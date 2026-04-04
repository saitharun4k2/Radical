package com.online.MiniUdemy.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String difficulty;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CourseModule> modules1 = new java.util.ArrayList<>();

    // Relates to the User Entity (Instructor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    public java.util.List<CourseModule> getModules1() {
		return modules1;
	}

	public void setModules1(java.util.List<CourseModule> modules1) {
		this.modules1 = modules1;
	}

	// Relates to the Category Entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    // A single course can have many modules (Chapters/Sections)
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CourseModule> modules;

    // ----------------------------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------------------------

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getTitle() { 
        return title; 
    }
    
    public void setTitle(String title) { 
        this.title = title; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public String getDifficulty() { 
        return difficulty; 
    }
    
    public void setDifficulty(String difficulty) { 
        this.difficulty = difficulty; 
    }
    
    public User getInstructor() { 
        return instructor; 
    }
    
    public void setInstructor(User instructor) { 
        this.instructor = instructor; 
    }
    
    public Category getCategory() { 
        return category; 
    }
    
    public void setCategory(Category category) { 
        this.category = category; 
    }

    public java.util.List<CourseModule> getModules() {
        // If the database returns null, hand Thymeleaf an empty list instead so it doesn't crash!
        if (modules1 == null) {
            return new java.util.ArrayList<>();
        }
        return modules1;
    }

    public void setModules(List<CourseModule> modules) {
        this.modules1 = modules;
    }
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Review> reviews = new java.util.ArrayList<>();

    public java.util.List<Review> getReviews() { return reviews; }
    public void setReviews(java.util.List<Review> reviews) { this.reviews = reviews; }

    // Helper method to calculate the average rating on the fly!
    public double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Review r : reviews) {
            sum += r.getRating();
        }
        // Returns a nice rounded number like 4.5
        return Math.round((sum / reviews.size()) * 10.0) / 10.0; 
    }
}