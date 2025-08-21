package com.huey.ai_resume;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
        String resumeContent = resumeService.getResumeText();
        if (resumeContent == null || resumeContent.isBlank()) {
            return Mono.just("이력서 내용을 불러오는데 실패했습니다.");
        }

        String prompt = String.format(
            "당신은 제공된 이력서 내용을 바탕으로 질문에 답변하는 AI 챗봇입니다. " +
            "아래에 제공된 이력서 내용만을 근거로 답변해야 합니다. " +
            "이력서에 없는 내용은 \"이력서에서 해당 내용을 찾을 수 없습니다.\" 라고 명확하게 답변해주세요. " +
            "만약 여러 프로젝트나 항목에 대해 답변할 때, 질문과 관련된 내용이 없는 프로젝트나 항목은 답변에서 아예 제외하고 언급하지 마세요. " +
            "절대로 내용을 추측하거나 지어내지 마세요.\n\n" +
            "--- 이력서 내용 시작 ---\n%s\n--- 이력서 내용 끝 ---\n\n" +
            "[질문]: %s",
            resumeContent, userQuestion
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
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
