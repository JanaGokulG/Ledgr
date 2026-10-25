package com.ledgr.sandbox.controller;

import com.ledgr.sandbox.entity.TransactionEntity;
import com.ledgr.sandbox.service.RealtimeTransactionSimulatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    private static final Logger log = LoggerFactory.getLogger(SimulatorController.class);

    private final RealtimeTransactionSimulatorService simulatorService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SimulatorController(RealtimeTransactionSimulatorService simulatorService) {
        this.simulatorService = simulatorService;

        // Broadcast any simulated transactions to connected SSE clients
        this.simulatorService.registerListener(this::broadcastToSse);
    }

    private void broadcastToSse(TransactionEntity txn) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("transaction")
                        .data(txn));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    /**
     * Trigger a single instant simulated transaction
     */
    @PostMapping("/transactions/generate")
    public ResponseEntity<?> generateTransaction(
            @RequestParam(required = false) String accountNumber) {
        try {
            TransactionEntity txn = simulatorService.generateAndPersistTransaction(accountNumber);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Simulated transaction generated and persisted.",
                    "transaction", txn
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Start continuous live background transaction stream
     */
    @PostMapping("/stream/start")
    public ResponseEntity<Map<String, Object>> startStream(
            @RequestParam(required = false, defaultValue = "1200") int minDelayMs,
            @RequestParam(required = false, defaultValue = "3200") int maxDelayMs,
            @RequestParam(required = false) Integer intervalSeconds) {
        if (intervalSeconds != null && intervalSeconds > 0) {
            simulatorService.startLiveStreaming(intervalSeconds);
        } else {
            simulatorService.startLiveStreamingWithRandomDelay(minDelayMs, maxDelayMs);
        }
        return ResponseEntity.ok(Map.of(
                "status", "STARTED",
                "active", true,
                "minDelayMs", simulatorService.getMinDelayMs(),
                "maxDelayMs", simulatorService.getMaxDelayMs(),
                "mode", "RANDOM_DELAY_WITH_REPETITION",
                "message", "Live transaction stream active with random delays between " 
                        + simulatorService.getMinDelayMs() + "ms and " + simulatorService.getMaxDelayMs() + "ms."
        ));
    }

    /**
     * Stop continuous live background transaction stream
     */
    @PostMapping("/stream/stop")
    public ResponseEntity<Map<String, Object>> stopStream() {
        simulatorService.stopLiveStreaming();
        return ResponseEntity.ok(Map.of(
                "status", "STOPPED",
                "active", false,
                "message", "Live transaction stream stopped."
        ));
    }

    /**
     * Get status of the live transaction stream
     */
    @GetMapping("/stream/status")
    public ResponseEntity<Map<String, Object>> getStreamStatus() {
        boolean active = simulatorService.isStreamingActive();
        return ResponseEntity.ok(Map.of(
                "active", active,
                "mode", "RANDOM_DELAY_WITH_REPETITION",
                "minDelayMs", simulatorService.getMinDelayMs(),
                "maxDelayMs", simulatorService.getMaxDelayMs(),
                "lastDelayMs", simulatorService.getLastDelayMs(),
                "totalStreamedCount", simulatorService.getTotalStreamedCount(),
                "connectedSseClients", emitters.size()
        ));
    }

    /**
     * Polling fallback endpoint for live transactions feed
     */
    @GetMapping("/stream/feed")
    public ResponseEntity<Map<String, Object>> getStreamFeed(
            @RequestParam(required = false, defaultValue = "20") int limit) {
        return ResponseEntity.ok(Map.of(
                "active", simulatorService.isStreamingActive(),
                "totalStreamedCount", simulatorService.getTotalStreamedCount(),
                "lastDelayMs", simulatorService.getLastDelayMs(),
                "recentTransactions", simulatorService.getRecentStreamTransactions(limit)
        ));
    }

    /**
     * Real-time Server-Sent Events (SSE) stream for live transactions
     */
    @GetMapping(value = "/stream/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeLiveStream() {
        // 0L timeout = infinite SSE lifetime so the stream connection never expires
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "status", "CONNECTED",
                            "message", "Connected to Ledgr Live Transaction Stream (SSE)",
                            "mode", "RANDOM_DELAY_WITH_REPETITION",
                            "minDelayMs", simulatorService.getMinDelayMs(),
                            "maxDelayMs", simulatorService.getMaxDelayMs()
                    )));
        } catch (IOException ignored) {}

        return emitter;
    }
}
