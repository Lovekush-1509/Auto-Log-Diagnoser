package dev.logMonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogSearchService {
    private static final Logger log = LoggerFactory.getLogger(LogSearchService.class);
    private final VectorStore vectorStore;

    public LogSearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Performs a semantic similarity search across stored log vectors in pgvector.
     *
     * @param query The natural language query or error snippet
     * @param topK  Maximum number of matching logs to return
     * @return List of matching Document objects containing content and metadata
     */
    public List<Document> searchSimilarLogs(String query, int topK) {
        log.info("Executing vector similarity search for query: '{}' with topK={}", query, topK);

        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(0.5); // Adjust threshold as needed (0.0 to 1.0)

        return vectorStore.similaritySearch(searchRequest);
    }
}
