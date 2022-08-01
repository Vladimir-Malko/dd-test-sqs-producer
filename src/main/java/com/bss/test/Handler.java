package com.bss.test;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.bss.test.handler.SqsRequestHandlerWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Handler implements SqsRequestHandlerWrapper<Void> {

    private static final Logger log = LogManager.getLogger(Handler.class);


    private final AmazonSQS sqs;

    public Handler() {

        sqs = AmazonSQSClient.builder()
                .withRegion("us-east-2")
                .build();
    }

    public Void handleRequest(SQSEvent sqsEvent) {
        log.info("=== start");
        log.info("=== sending to SQS");
        sqs.sendMessage("https://sqs.us-east-2.amazonaws.com/666359764528/dd-test-consumer", "{\"param\":\"hi\"}");
        log.info("=== end of sending");
        return null;
    }
}
