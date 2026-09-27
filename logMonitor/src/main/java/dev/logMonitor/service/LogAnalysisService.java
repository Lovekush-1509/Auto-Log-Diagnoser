package dev.logMonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Invoke-RestMethod -Uri http://localhost:8080/logWatchman/analyze -Method Post -ContentType "application/json" -Body '{"serviceName":"payment-service","logLevel":"ERROR","message":"Database connection timeout while processing payment","stackTrace":"java.sql.SQLException: Connection timeout after 3000ms","traceId":"trace-abc-123"}'

@Service
public class LogAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(LogAnalysisService.class);
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    public LogAnalysisService(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    public String analyzeErrorAndSuggestFix(String errorMessage, String stackTrace) {
        logger.info("Running Ollama AI Root Cause Analysis for error: '{}'", errorMessage);

        // 1. Retrieve similar historical logs using pgvector for context
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(errorMessage).withTopK(3)
        );

        String historicalContext = similarDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n---\n"));

        logger.info("making prompt");
        // 2. Build RAG prompt for Ollama
        String prompt = String.format("""
            You are an expert DevOps and Reliability Engineer.
            
            Analyze the following incoming error log:
            - Error Message: %s
            - Stack Trace: %s
            
            Historical Context (Past similar incidents):
            %s
            
            Provide a clear, structured response with:
            1. ROOT CAUSE SUMMARY: (1-2 clear sentences)
            2. SEVERITY LEVEL: (LOW, MEDIUM, HIGH, or CRITICAL)
            3. RECOMMENDED FIX: (Actionable resolution steps)
            
                        Return ONLY valid JSON.
                            Do not use markdown.
                            Do not use ```json or ```.
                        
                            The JSON must follow exactly this structure:
                            {
                              "rootCauseSummary": "1-2 clear sentences explaining the likely root cause",
                              "severityLevel": "(LOW, MEDIUM, HIGH, or CRITICAL)",
                              "recommendedFix": [
                                "Actionable resolution step 1",
                                "Actionable resolution step 2",
                                "Actionable resolution step 3"
                              ]
                            }
            
            """,
                errorMessage,
                stackTrace != null ? stackTrace : "N/A",
                historicalContext.isEmpty() ? "No historical context found." : historicalContext
        );

        logger.info("calling chat model with prompt:{}", prompt);

        // 3. Call Ollama (phi)
        String chatModelResponse =  chatModel.call(prompt);

        logger.info("chat model response:{}", chatModelResponse);
        return chatModelResponse;
    }
}
