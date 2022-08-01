package com.bss.test.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datadog.trace.api.CorrelationIdentifier;
import datadog.trace.api.GlobalTracer;
import datadog.trace.api.Tracer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatadogUtil {

    private static final Logger log = LogManager.getLogger(DatadogUtil.class);

    private static final String INPUT_SQS_PARAM = "Input";
    private static final String DD_TRACE_ID_FIELD = "ddTraceId";
    private static final String DD_TRACE_ID_KEY = "dd.trace_id";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void fillDdTraceId(SQSEvent sqsEvent) {
        try {
            log.info("=== start tracing!!!");
            fillDdTraceId(retrieveExistingDdTraceIdOrGenerateNew(sqsEvent));
        } catch (Exception e) {
            log.warn("=== DD Tracing errored: {}", e.getMessage(), e);
        }
    }

    private static String retrieveExistingDdTraceIdOrGenerateNew(SQSEvent sqsEvent) {
        return sqsEvent.getRecords().stream()
                .findFirst()
                .map(SQSEvent.SQSMessage::getBody)
                .flatMap(DatadogUtil::convertStringToJson)
                .map(jsonNode -> jsonNode.get(INPUT_SQS_PARAM))
                .map(jsonNode -> jsonNode.get(DD_TRACE_ID_FIELD))
                .filter(jsonNode -> !jsonNode.isNull())
                .map(JsonNode::asText)
                .orElseGet(CorrelationIdentifier::getTraceId);
    }

    private static void fillDdTraceId(String ddTraceId) {
        log.info("=== ddTraceId: {}", ddTraceId);
        ThreadContext.put(DD_TRACE_ID_KEY, ddTraceId);

        log.info("=== ddTraceId after setup: {}", CorrelationIdentifier.getTraceId());
    }

    /**
     * Add DD TraceId To request
     *
     * @param input input json
     */
    public static String addDdTraceIdToRequest(String input) {
        Tracer tracer = GlobalTracer.get();
//        tracer.addTraceInterceptor()
        String traceId = tracer.getTraceId();
        String spanId = tracer.getSpanId();
        log.info("==== !!!! traceId: {}, spanId: {}", traceId, spanId);

        return addDdTraceId(input, CorrelationIdentifier.getTraceId());
    }

    private static String addDdTraceId(String input, String ddTraceId) {
        return Optional.ofNullable(input)
                .filter(value -> !StringUtils.isNullOrEmpty(value))
                .flatMap(DatadogUtil::convertStringToJson)
                .map(jsonNode -> ((ObjectNode) jsonNode).put(DD_TRACE_ID_FIELD, ddTraceId))
                .map(ObjectNode::toString)
                .orElse(input);
    }

    private static Optional<JsonNode> convertStringToJson(String jsonString) {
        try {
            return Optional.ofNullable(OBJECT_MAPPER.readTree(jsonString));
        } catch (Exception e) {
            log.info("=== Error during convertStringToJson. Message: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
