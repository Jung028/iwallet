package com.alipay.business.rpc;

import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import com.alipay.sofa.rpc.filter.Filter;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import org.slf4j.MDC;

/**
 * Consumer-side SOFA RPC filter that propagates the current request's traceId
 * (set in the logging MDC by the servlet TraceIdFilter) to downstream services
 * via RPC baggage, so callees (e.g. iaccount) log the same traceId and the
 * trace can be correlated across services.
 */
@Extension("traceIdConsumer")
@AutoActive(consumerSide = true)
public class TraceIdConsumerFilter extends Filter {

    private static final String TRACE_ID = "traceId";

    @Override
    public SofaResponse invoke(FilterInvoker invoker, SofaRequest request) throws SofaRpcException {
        String traceId = MDC.get(TRACE_ID);
        if (traceId != null && !traceId.isEmpty()) {
            RpcInvokeContext.getContext().putRequestBaggage(TRACE_ID, traceId);
        }
        return invoker.invoke(request);
    }
}
