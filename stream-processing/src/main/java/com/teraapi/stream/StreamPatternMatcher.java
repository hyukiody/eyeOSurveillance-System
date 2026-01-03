package com.teraapi.stream;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight CEP engine - indexed query system
 * Implements "queries are persistent, data is transient" model
 */
public class StreamPatternMatcher {
    private final Map<String, EventPattern<DetectionEvent>> patterns = new ConcurrentHashMap<>();
    private final Sinks.Many<DetectionEvent> eventSink = Sinks.many().multicast().onBackpressureBuffer();
    private final AtomicInteger matchCount = new AtomicInteger(0);

    /**
     * Register a persistent query (Use Case C: Search on Streams)
     */
    public void registerPattern(EventPattern<DetectionEvent> pattern) {
        patterns.put(pattern.getPatternId(), pattern);
        System.out.println("Registered pattern: " + pattern.getDescription());
    }

    /**
     * Remove a query
     */
    public void unregisterPattern(String patternId) {
        patterns.remove(patternId);
    }

    /**
     * Publish event to stream - runs past all registered queries
     */
    public void publishEvent(DetectionEvent event) {
        eventSink.tryEmitNext(event);
    }

    /**
     * Get matched events as Flux (reactive stream)
     */
    public Flux<DetectionEvent> getMatchedEvents(String patternId) {
        EventPattern<DetectionEvent> pattern = patterns.get(patternId);
        if (pattern == null) {
            return Flux.empty();
        }

        return eventSink.asFlux()
                .filter(pattern::matches)
                .doOnNext(event -> {
                    matchCount.incrementAndGet();
                    System.out.println("Pattern '" + patternId + "' matched: " + event);
                });
    }

    /**
     * Stream Analytics (Use Case A): Get all events with rolling window
     */
    public Flux<DetectionEvent> getAllEventsWithWindow(long windowMillis) {
        return eventSink.asFlux()
                .buffer(java.time.Duration.ofMillis(windowMillis))
                .flatMapIterable(events -> events);
    }

    /**
     * Get metrics
     */
    public int getTotalMatches() {
        return matchCount.get();
    }

    public int getRegisteredPatternCount() {
        return patterns.size();
    }
}
