package com.online.MiniUdemy.service;

import com.online.MiniUdemy.entity.Comment;
import com.online.MiniUdemy.entity.Lesson;
import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.repository.CommentRepository;
import com.online.MiniUdemy.repository.LessonRepository;
import com.online.MiniUdemy.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, LessonRepository lessonRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    public Comment addComment(Long userId, Long lessonId, String content) {
        // 1. Fetch the user and lesson from the database
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + lessonId));

        // 2. Create and populate the comment entity
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setLesson(lesson);

        // 3. Save and return
        return commentRepository.save(comment);
    }
}