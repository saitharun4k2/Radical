package com.online.MiniUdemy.service;

import com.online.MiniUdemy.entity.InstructorApplication;
import com.online.MiniUdemy.repository.InstructorApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InstructorApplicationService {

    private final InstructorApplicationRepository applicationRepository;
    private final FileService fileService;

    public InstructorApplicationService(InstructorApplicationRepository applicationRepository, FileService fileService) {
        this.applicationRepository = applicationRepository;
        this.fileService = fileService;
    }

    public void submitApplication(InstructorApplication application, MultipartFile resume) throws Exception {
        // 1. Save the uploaded resume using your existing FileService
        if (resume != null && !resume.isEmpty()) {
            String fileName = fileService.saveFile(resume);
            application.setResumeFileName(fileName);
        }

        // 2. Save the application to the database
        applicationRepository.save(application);
    }
}