package com.codesync.service;

import com.codesync.model.Operation;
import com.codesync.util.OTAlgorithm;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages Operational Transformation for collaborative editing sessions.
 * Maintains operation history per document and handles transformation.
 */
@Service
public class OTService {

    private final OTAlgorithm otAlgorithm;
    private final RedisTemplate<String, Object> redisTemplate;

    // In-memory fallback for operation history per document
    private final Map<Long, List<Operation>> operationHistory = new ConcurrentHashMap<>();

    private static final String OPERATION_HISTORY_KEY = "operation_history:";
    private static final String DOCUMENT_VERSION_KEY = "document_version:";

    public OTService(OTAlgorithm otAlgorithm, RedisTemplate<String, Object> redisTemplate) {
        this.otAlgorithm = otAlgorithm;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Process an incoming operation from a client.
     * Transform it against all concurrent operations and apply it.
     */
    public Operation processOperation(Operation operation) {
        Long documentId = operation.getDocumentId();
        List<Operation> history = getOperationHistory(documentId);

        // Transform the incoming operation against all operations that happened after
        // its version
        List<Operation> concurrentOps = history.stream()
                .filter(op -> op.getVersion() >= operation.getVersion())
                .sorted(Comparator.comparing(Operation::getVersion))
                .collect(Collectors.toList());

        Operation transformedOp = operation;
        for (Operation concurrentOp : concurrentOps) {
            transformedOp = otAlgorithm.transform(transformedOp, concurrentOp);
        }

        // Assign new version number
        long newVersion = history.size() + 1;
        transformedOp.setVersion(newVersion);

        // Add to history
        addToHistory(documentId, transformedOp);

        // Update document version in Redis
        redisTemplate.opsForValue().set(DOCUMENT_VERSION_KEY + documentId, newVersion);

        return transformedOp;
    }

    /**
     * Get the current version of a document.
     */
    public long getCurrentVersion(Long documentId) {
        Integer version = (Integer) redisTemplate.opsForValue()
                .get(DOCUMENT_VERSION_KEY + documentId);
        if (version != null) {
            return version.longValue();
        }
        List<Operation> history = operationHistory.get(documentId);
        return history != null ? history.size() : 0;
    }

    /**
     * Get operation history for a document.
     */
    public List<Operation> getOperationHistory(Long documentId) {
        // Try Redis first
        List<Operation> redisHistory = (List<Operation>) redisTemplate.opsForValue()
                .get(OPERATION_HISTORY_KEY + documentId);
        if (redisHistory != null) {
            return redisHistory;
        }
        // Fallback to in-memory
        return operationHistory.getOrDefault(documentId, Collections.synchronizedList(new ArrayList<>()));
    }

    /**
     * Add an operation to the history.
     */
    private void addToHistory(Long documentId, Operation operation) {
        operationHistory.computeIfAbsent(documentId,
                k -> Collections.synchronizedList(new ArrayList<>())).add(operation);

        // Also try to cache in Redis
        String key = OPERATION_HISTORY_KEY + documentId;
        List<Operation> redisHistory = (List<Operation>) redisTemplate.opsForValue().get(key);
        if (redisHistory == null) {
            redisHistory = new ArrayList<>();
        }
        redisHistory.add(operation);
        redisTemplate.opsForValue().set(key, redisHistory);
    }

    /**
     * Clear operation history for a document (e.g., when saving).
     */
    public void clearHistory(Long documentId) {
        operationHistory.remove(documentId);
        redisTemplate.delete(OPERATION_HISTORY_KEY + documentId);
    }
}
