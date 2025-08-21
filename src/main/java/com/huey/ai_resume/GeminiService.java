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
    private final String apiKey;
    private final String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public GeminiService(WebClient.Builder webClientBuilder, ResumeService resumeService, @Value("${gemini.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(apiUrl)
                .defaultHeader("X-goog-api-key", apiKey)
                .build();
        this.resumeService = resumeService;
        this.apiKey = apiKey;
    }

    public Mono<String> getChatResponse(String userQuestion) {
        String resumeContent = resumeService.getResumeText();

        // gemini에게 보낼 프롬프트 구성
        String prompt = """
                당신은 나의 이력서를 바탕으로 질문에 답변하는 AI 챗봇입니다.
                아래에 제공된 이력서 내용만을 근거로 답변해야 합니다
                이력서에 없는 내용은 "이력서에 해당 내용이 없습니다." 라고 명확하게 답변해주세요. 절대로 내용을 추측하거나 지어내지 마세요.

                --- 이력서 내용 시작 ---
                $s
                --- 이력서 내용 끝 ---
                [질문]: %s
                """.formatted(resumeContent, userQuestion);

        // gemini api 요청 본문 생성
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)  // 응답을 Map으로 받음
                .map(this::extraTextFromResponse);  // 받은 응답에서 텍스트만 추출
    }

    // API 응답 구조에 맞춰 텍스트를 파싱하는 메소드
    private String extraTextFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
            return parts.get(0).get("text");
        } catch (Exception e) {
            return "답변을 파싱하는 중 오류가 발생하였습니다.";
        }
    }
}
