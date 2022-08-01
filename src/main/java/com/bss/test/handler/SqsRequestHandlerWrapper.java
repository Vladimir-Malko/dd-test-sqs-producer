package com.bss.test.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.datadoghq.datadog_lambda_java.DDLambda;

import static com.bss.test.handler.DatadogUtil.fillDdTraceId;

public interface SqsRequestHandlerWrapper<O> extends RequestHandlerWrapper<SQSEvent, O> {

    /**
     * Handle Request with distributed tracing logic
     *
     * @param sqsEvent SQS Event: {@link DDLambda}.
     * @param context  context: {@link Context}.
     * @return response
     */
    @Override
    default O handleRequest(SQSEvent sqsEvent, Context context) {
        DDLambda ddLambda = new DDLambda(sqsEvent, context);
//        fillDdTraceId(sqsEvent);
        return handleRequest(sqsEvent, ddLambda);
    }
}
