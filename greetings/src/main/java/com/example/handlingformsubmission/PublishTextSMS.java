package com.example.handlingformsubmission;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

/**
 * A Java class that sends a text message.
 */
//TODO refactor region and phone number to a properties file? Use an other region? 
@Component("PublishTextSMS")
public class PublishTextSMS {

	public void sendMessage(String id) {
		Region region = Region.US_EAST_1;
		SnsClient snsClient = SnsClient.builder()
				.region(region)
				.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
				.build();
		String message = "A new item with ID value "+ id +" was added to the DynamoDB table";
		String phoneNumber="+46 10 123456789"; // TODO Replace with a real phone number before use!

		try {
			PublishRequest request = PublishRequest.builder()
					.message(message)
					.phoneNumber(phoneNumber)
					.build();

			PublishResponse result = snsClient.publish(request);

		} catch (SnsException e) {

			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
