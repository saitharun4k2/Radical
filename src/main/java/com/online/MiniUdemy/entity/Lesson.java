package com.online.MiniUdemy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Stores the written article/text (Using TEXT type for long paragraphs)
    @Column(columnDefinition = "TEXT")
    private String textContent; 

    // Stores the path to the PDF if they upload one
    @Column
    private String fileUrl; 

    @Column(nullable = false)
    private String contentType; // Will be either "Text" or "PDF"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC") // Keeps the chat in chronological order
    private java.util.List<Comment> comments = new java.util.ArrayList<>();

    public java.util.List<Comment> getComments() { return comments; }
    public void setComments(java.util.List<Comment> comments) { this.comments = comments; }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public CourseModule getModule() { return module; }
    public void setModule(CourseModule module) { this.module = module; }
}