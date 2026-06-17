package com.alipay.business.biz.service.impl.message;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

/**
 * Restores the {@code traceId} from the incoming Kafka record header into the
 * logging MDC before the {@code @KafkaListener} method runs, and clears it after.
 * <p>
 * The {@code @KafkaListener} threads are not covered by the servlet/RPC trace
 * filters, so without this every consumed-message log line prints {@code traceId=}.
 * Spring Boot auto-applies a single {@link RecordInterceptor} bean to the
 * default listener container factory — no extra wiring needed.
 */
@Component
public class TraceIdRecordInterceptor implements RecordInterceptor<Object, Object> {

    private static final String TRACE_ID = "traceId";

    @Override
    public ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> record,
                                                    Consumer<Object, Object> consumer) {
        Header header = record.headers().lastHeader(TRACE_ID);
        String traceId = (header != null) ? new String(header.value(), StandardCharsets.UTF_8) : null;
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(TRACE_ID, traceId);
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        MDC.remove(TRACE_ID);
    }
}
