package com.huey.ai_resume;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final ResumeService resumeService;
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public GeminiService(WebClient.Builder webClientBuilder, ResumeService resumeService, @Value("${gemini.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(apiUrl)
                .defaultHeader("X-goog-api-key", apiKey)
                .build();
        this.resumeService = resumeService;
    }

    public Mono<String> getChatResponse(String userQuestion) {
        byte[] resumeBytes = resumeService.getResumeBytes();
        if (resumeBytes == null) {
            return Mono.just("이력서 파일을 불러오는데 실패했습니다.");
        }
        String base64Resume = Base64.getEncoder().encodeToString(resumeBytes);

        String prompt = "첨부된 이력서를 바탕으로 다음 질문에 답변해 주세요. 이력서에 없는 내용은 \"이력서에 해당 내용이 없습니다.\" 라고 명확하게 답변해주세요. 절대로 내용을 추측하거나 지어내지 마세요. 질문: " + userQuestion;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inlineData", Map.of(
                                        "mimeType", "application/pdf",
                                        "data", base64Resume
                                ))
                        ))
                )
        );

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extraTextFromResponse)
                .onErrorResume(e -> Mono.just("Gemini API 호출 중 오류가 발생했습니다: " + e.getMessage()));
    }

    private String extraTextFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "Gemini API로부터 유효한 답변을 받지 못했습니다. 응답: " + responseBody;
            }
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
            return parts.get(0).get("text");
        } catch (Exception e) {
            return "답변을 파싱하는 중 오류가 발생하였습니다. 응답: " + responseBody;
        }
    }
}