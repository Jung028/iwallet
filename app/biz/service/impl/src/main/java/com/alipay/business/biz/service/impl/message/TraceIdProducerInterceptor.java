package com.alipay.business.biz.service.impl.message;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

/**
 * Stamps the current request's {@code traceId} (from the logging MDC) onto every
 * outgoing Kafka record as a header, so the consuming service can restore it.
 * <p>
 * The servlet {@code TraceIdFilter} / RPC filters put {@code traceId} into the MDC
 * on the request thread; {@code KafkaProducer.send()} runs {@code onSend} on that
 * same thread, so the value is available here. Without this, the traceId is lost
 * at the Kafka boundary and downstream consumers log {@code traceId=}.
 */
public class TraceIdProducerInterceptor implements ProducerInterceptor<Object, Object> {

    private static final String TRACE_ID = "traceId";

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
        String traceId = MDC.get(TRACE_ID);
        if (traceId != null && !traceId.isEmpty()
                && record.headers().lastHeader(TRACE_ID) == null) {
            record.headers().add(TRACE_ID, traceId.getBytes(StandardCharsets.UTF_8));
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
