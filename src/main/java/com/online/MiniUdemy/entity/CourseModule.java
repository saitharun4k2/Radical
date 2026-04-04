package com.online.MiniUdemy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "modules")
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
 // A single module has many lessons
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    private java.util.List<Lesson> lessons = new java.util.ArrayList<>();

    // Add the getters and setters for the lessons list at the very bottom:
    public java.util.List<Lesson> getLessons() {
        if (lessons == null) {
            return new java.util.ArrayList<>();
        }
        return lessons;
    }
    public void setLessons(java.util.List<Lesson> lessons) {
        this.lessons = lessons;
    }
    
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Lesson> lessons1 = new java.util.ArrayList<>();

    // This is the crucial link back to the Course!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public java.util.List<Lesson> getLessons1() {
		return lessons1;
	}
	public void setLessons1(java.util.List<Lesson> lessons1) {
		this.lessons1 = lessons1;
	}
	// Constructors
    public CourseModule() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}