package com.example.api1updaterspring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

@SpringBootApplication
public class Api1UpdaterSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(Api1UpdaterSpringApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("commandLineRunner");

			// TODO: Loop

			// handler to broadcast queue data to websocket clients
			SocketTextHandler socketTextHandler = ctx.getBean("socketTextHandler", SocketTextHandler.class);

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
			var receiveMsgResult = sqs.receiveMessage(receive_request);

			System.out.println(String.format("received %d msgs", receiveMsgResult.getMessages().size()));

			// iterate over message body data, passing it to handler for broadcast
			for (Message msg : receiveMsgResult.getMessages()) {
				var body = msg.getBody();
				System.out.println(String.format("body: %s", body));
				socketTextHandler
						.broadcastJsonString(body);
			}
		};
	}

}
