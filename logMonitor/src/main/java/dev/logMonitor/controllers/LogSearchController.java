package dev.logMonitor.controllers;

import dev.logMonitor.dto.LogEvent;
import dev.logMonitor.service.LogAnalysisService;
import dev.logMonitor.service.LogSearchService;
import dev.logMonitor.service.SseNotificationService;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/logWatchman")
public class LogSearchController {

    private final LogSearchService logSearchService;
    private final LogAnalysisService logAnalysisService;
    private final SseNotificationService sseNotificationService;

    public LogSearchController(LogSearchService logSearchService, LogAnalysisService logAnalysisService,
                               SseNotificationService sseNotificationService) {
        this.logSearchService = logSearchService;
        this.logAnalysisService = logAnalysisService;
        this.sseNotificationService = sseNotificationService;
    }


    //Invoke-RestMethod -Uri "http://localhost:8080/logWatchman/search?query=Database connection timeout&topK=3" -Method Get
    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchLogs(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "topK", defaultValue = "5") int topK) {

        List<Document> results = logSearchService.searchSimilarLogs(query, topK);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeError(@RequestBody LogEvent logEvent) {

        String analysis = logAnalysisService.analyzeErrorAndSuggestFix(
                logEvent.message(),
                logEvent.stackTrace()
        );
        return ResponseEntity.ok(analysis);
    }

    @GetMapping(value = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        return sseNotificationService.registerClient();
    }
}
