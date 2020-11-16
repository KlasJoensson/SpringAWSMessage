package com.example.handlingformsubmission;

import org.springframework.stereotype.Component;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primaryPartitionKey;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

/**
 * A Java class that injects data into a DynamoDB table by using the DynamoDB enhanced client API
 */
//TODO Use another region? Set it in a properties file instead? Refaktor the GreetingsItems-class. 
@Component("DynamoDBEnhanced")
public class DynamoDBEnhanced {

	private final ProvisionedThroughput DEFAULT_PROVISIONED_THROUGHPUT =
			ProvisionedThroughput.builder()
			.readCapacityUnits(50L)
			.writeCapacityUnits(50L)
			.build();

	private final TableSchema<GreetingItems> TABLE_SCHEMA =
			StaticTableSchema.builder(GreetingItems.class)
			.newItemSupplier(GreetingItems::new)
			.addAttribute(String.class, a -> a.name("idblog")
					.getter(GreetingItems::getId)
					.setter(GreetingItems::setId)
					.tags(primaryPartitionKey()))
			.addAttribute(String.class, a -> a.name("author")
					.getter(GreetingItems::getName)
					.setter(GreetingItems::setName))
			.addAttribute(String.class, a -> a.name("title")
					.getter(GreetingItems::getTitle)
					.setter(GreetingItems::setTitle))
			.addAttribute(String.class, a -> a.name("body")
					.getter(GreetingItems::getMessage)
					.setter(GreetingItems::setMessage))
			.build();

	// Uses the enhanced client to inject a new post into a DynamoDB table
	public void injectDynamoItem(Greeting item){

		Region region = Region.US_EAST_1;
		DynamoDbClient ddb = DynamoDbClient.builder()
				.region(region)
				.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
				.build();

		try {

			DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
					.dynamoDbClient(ddb)
					.build();

			// Create a DynamoDbTable object
			DynamoDbTable<GreetingItems> mappedTable = enhancedClient.table("Greeting", TABLE_SCHEMA);
			GreetingItems gi = new GreetingItems();
			gi.setName(item.getName());
			gi.setMessage(item.getBody());
			gi.setTitle(item.getTitle());
			gi.setId(item.getId());

			PutItemEnhancedRequest enReq = PutItemEnhancedRequest.builder(GreetingItems.class)
					.item(gi)
					.build();

			mappedTable.putItem(enReq);

		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	public class GreetingItems {

		// Set up data members that correspond to columns in the Work table
		private String id;
		private String name;
		private String message;
		private String title;

		public GreetingItems()
		{
		}

		public String getId() {
			return this.id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMessage(){
			return this.message;
		}

		public void setMessage(String message){
			this.message = message;
		}

		public String getTitle() {
			return this.title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}
}
