package com.example.handlingformsubmission;

import org.apache.log4j.LogManager;
>>>>>>> mytemp
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
@Component("PublishTextSMS")
public class PublishTextSMS {

	private Environment env;
	
	private static Logger log = LogManager.getLogger(PublishTextSMS.class);
	
	@Autowired
	public PublishTextSMS(Environment env) {
		this.env =env;
	}
	
	public void sendMessage(String id) {
		
		Region region = Region.of(env.getProperty("aws.region"));
		
		SnsClient snsClient = SnsClient.builder()
				.region(region)
				.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
				.build();
		String message = "A new item with ID value "+ id +" was added to the DynamoDB table";
		String phoneNumber=env.getProperty("phone");
		if (phoneNumber == null) {
			log.debug("No phonenumber found...");
		}
		try {
			PublishRequest request = PublishRequest.builder()
					.message(message)
					.phoneNumber(phoneNumber)
					.build();

			PublishResponse result = snsClient.publish(request);
			
			log.debug("SMS sent from region " + region.toString() + " with the result: " + result.toString());
		
		} catch (SnsException e) {

			log.error("An error occurred trying to sS from the region " + region.toString() + ": " + e.awsErrorDetails().errorMessage());
			
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
