package com.example.api1updaterspring;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.google.gson.Gson;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

@Component
public class QueuePollerLauncher {

  private ExecutorService executorService;

  @Autowired
  private SocketTextHandler socketTextHandler;

  @PostConstruct
  public void init() {

    BasicThreadFactory factory = new BasicThreadFactory.Builder()
        .namingPattern("socket-listener-%d").build();

    executorService = Executors.newSingleThreadExecutor(factory);
    executorService.execute(new Runnable() {

      @Override
      public void run() {

        for (;;) {

          System.out.println("start a long poll");

          // queue of updated albums
          final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
          var queueUrl = "http://localstack:4566/000000000000/album-queue";

          // Enable long polling
          SetQueueAttributesRequest set_attrs_request = new SetQueueAttributesRequest()
              .withQueueUrl(queueUrl)
              .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");
          sqs.setQueueAttributes(set_attrs_request);

          // get 0 or more messages
          ReceiveMessageRequest receive_request = new ReceiveMessageRequest()
              .withQueueUrl(queueUrl)
              .withWaitTimeSeconds(20);
          ReceiveMessageResult receiveMsgResult = sqs.receiveMessage(receive_request);

          System.out.println(String.format("received %d msgs", receiveMsgResult.getMessages().size()));

          // iterate over message body data, passing it to handler for broadcast
          for (Message msg : receiveMsgResult.getMessages()) {

            Gson gson = new Gson();
            var queueMessageBodyMap = gson.fromJson(msg.getBody(), Map.class);
            var messagePayload = (String) queueMessageBodyMap.get("Message");

            System.out.println(String.format("message: %s", messagePayload));
          
            try {
              socketTextHandler.broadcastJsonString(messagePayload);
            } catch (InterruptedException | IOException e) {
              e.printStackTrace();
            }

            sqs.deleteMessage(queueUrl, msg.getReceiptHandle()); // what if multiple instances of updater?
          }
        }

      }
    });

    executorService.shutdown();

  }

  @PreDestroy
  public void shutdown() {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

}
