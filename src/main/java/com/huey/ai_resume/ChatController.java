package com.huey.ai_resume;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    private final GeminiService geminiService;

    public ChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/")
    public String chatPage() {
        return "chat";  // templates/chat.html 파일 보여줌
    }

    @PostMapping("/ask")
    public String askQuestion(@RequestParam String question, Model model) {
        // GeminiService를 통해 답변을 비동기적으로 가져옴
        String answer = geminiService.getChatResponse(question).block();    // .block()으로 동기적으로 결과를 기다림

        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        return "chat";  // 동일한 페이지에 질문과 답변을 전달하여 리프레시
    }
}
