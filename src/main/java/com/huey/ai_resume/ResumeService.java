package com.huey.ai_resume;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class ResumeService {

    private String resumeText;

    public String getResumeText() {
        return resumeText;
    }

    @PostConstruct
    public void init() {
        // Setup Chrome to run in headless mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = null;
        try {
            driver = new ChromeDriver(options);
            String notionUrl = "https://www.notion.so/hyoin11/1cd81129ea3e80648c18ee29f17a0368?source=copy_link";
            driver.get(notionUrl);

            // Wait for the page to load. A static wait is simple but brittle.
            // In a real-world scenario, WebDriverWait would be better.
            Thread.sleep(5000); // Wait 5 seconds for dynamic content

            String htmlContent = driver.getPageSource();
            this.resumeText = Jsoup.parse(htmlContent).text();

            System.out.println("Selenium으로 Notion 페이지 로딩 및 파싱 성공. 추출된 텍스트 길이: " + (this.resumeText != null ? this.resumeText.length() : 0));
            System.out.println("추출된 내용 (앞 200자): " + this.resumeText.substring(0, Math.min(200, this.resumeText.length())));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("페이지 로딩 대기 중 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Selenium 처리 중 오류 발생: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}