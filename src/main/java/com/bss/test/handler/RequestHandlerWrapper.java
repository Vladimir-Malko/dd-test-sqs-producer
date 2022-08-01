package com.bss.test.handler;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.datadoghq.datadog_lambda_java.DDLambda;

/**
 * Basic Request Handler Wrapper.
 * Wrap 'handleRequest' process by the Datadog Distributed Tracing instructions
 *
 * @param <I> - request input type
 * @param <O> - response output type
 */
public interface RequestHandlerWrapper<I, O> extends RequestHandler<I, O> {

    /**
     * Handle Request
     *
     * @param i request
     * @return response
     */
    O handleRequest(I i);

    /**
     * Handle Request wrapped by the Datadog Distributed Tracing instructions
     *
     * @param i        request
     * @param ddLambda dd lambda: {@link DDLambda}.
     * @return response
     */
    default O handleRequest(I i, DDLambda ddLambda) {
        O o = handleRequest(i);
        ddLambda.finish();
        return o;
    }
}
