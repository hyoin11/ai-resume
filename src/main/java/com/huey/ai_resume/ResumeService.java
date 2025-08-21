package com.huey.ai_resume;

import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ResumeService {

    private String resumeText;

    public String getResumeText() {
        return resumeText;
    }

    // 어플리케이션이 시작될 때 이 메서드가 자동으로 실행
    @PostConstruct
    public void init() {
        // resources/static/hyoin_resume.pdf 파일 읽기
        ClassPathResource resource = new ClassPathResource("static/hyoin_resume.pdf");

        try (InputStream inputStream = resource.getInputStream();
            PDDocument document = Loader.loadPDF(inputStream.readAllBytes());) {

            PDFTextStripper pdfTextStripper = new PDFTextStripper();

            // PDF의 텍스트를 추출해 변수에 저장
            this.resumeText = pdfTextStripper.getText(document);
            System.out.println("추출된 이력서 내용: " + this.resumeText);

            System.out.println("이력서 로딩 성공");
        } catch (IOException e) {
            System.err.println("이력서 파일을 읽는 중 오류 발생");
        }
    }
}
