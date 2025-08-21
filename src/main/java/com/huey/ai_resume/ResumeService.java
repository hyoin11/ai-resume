package com.huey.ai_resume;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ResumeService {

    private byte[] resumeBytes;

    public byte[] getResumeBytes() {
        return resumeBytes;
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("static/hyoin_resume.pdf");
            this.resumeBytes = resource.getInputStream().readAllBytes();
            System.out.println("이력서 PDF 파일을 바이트로 로딩 성공. 크기: " + this.resumeBytes.length + " bytes");
        } catch (IOException e) {
            System.err.println("이력서 파일을 읽는 중 오류 발생: " + e.getMessage());
            // In a real app, you might want to throw a runtime exception
            // to prevent the app from starting in a bad state.
            this.resumeBytes = null;
        }
    }
}