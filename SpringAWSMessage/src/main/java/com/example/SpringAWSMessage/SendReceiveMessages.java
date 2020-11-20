package com.example.SpringAWSMessage;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * Uses the Amazon SQS API to process messages.
 */
@Component
public class SendReceiveMessages {

	private String queueName;
	private Region region;
	private Logger logger = LoggerFactory.getLogger(SendReceiveMessages.class);
	
	@Autowired
	public SendReceiveMessages(Environment env) {
		this.region = Region.of(env.getProperty("aws.region"));
		this.queueName = env.getProperty("queue.name");
	}
	
	private SqsClient getClient() {
		
		SqsClient sqsClient = SqsClient.builder()
				.region(region)
				.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
				.build();

		return sqsClient;
	}


	public void purgeMyQueue() {

		SqsClient sqsClient = getClient();

		GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
				.queueName(queueName)
				.build();

		PurgeQueueRequest queueRequest = PurgeQueueRequest.builder()
				.queueUrl(sqsClient.getQueueUrl(getQueueRequest).queueUrl())
				.build();

		sqsClient.purgeQueue(queueRequest);
	}

	public String getMessages() {

		List<String> attr = new ArrayList<String>();
		attr.add("Name");

		SqsClient sqsClient = getClient();

		try {

			GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
					.queueName(queueName)
					.build();

			String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();

			// Receive messages from the queue
			ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
					.queueUrl(queueUrl)
					.maxNumberOfMessages(10)
					.messageAttributeNames(attr)
					.build();
			List<software.amazon.awssdk.services.sqs.model.Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

			Message myMessage;

			ArrayList<Message> allMessages = new ArrayList<Message>();

			// Push the messages to a list
			for (software.amazon.awssdk.services.sqs.model.Message m : messages) {

				myMessage=new Message();
				myMessage.setBody(m.body());

				Map<String, MessageAttributeValue> map = m.messageAttributes();
				MessageAttributeValue val=(MessageAttributeValue)map.get("Name");
				myMessage.setName(val.stringValue());

				allMessages.add(myMessage);
			}

			return convertToString(toXml(allMessages));

		} catch (SqsException e) {
			logger.error("An error occurred when retrieving the the queue: " + e.getLocalizedMessage());
		}
		
		return "";
	}

	public void processMessage(Message msg) {

		SqsClient sqsClient = SqsClient.builder()
				.region(region)
				.build();


		try {

			// Get user
			MessageAttributeValue attributeValue = MessageAttributeValue.builder()
					.stringValue(msg.getName())
					.dataType("String")
					.build();

			HashMap<String, MessageAttributeValue> myMap = new HashMap<String, MessageAttributeValue>();
			myMap.put("Name", attributeValue);


			GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
					.queueName(queueName)
					.build();

			String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();

			// Generate the work item ID
			UUID uuid = UUID.randomUUID();
			String msgId1 = uuid.toString();

			SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
					.queueUrl(queueUrl)
					.messageAttributes(myMap)
					.messageGroupId("GroupA")
					.messageDeduplicationId(msgId1)
					.messageBody(msg.getBody())
					.build();
			sqsClient.sendMessage(sendMsgRequest);


		} catch (SqsException e) {
			logger.error("An error occurred when processing the message with id '" + 
					msg.getId() + "': " + e.getLocalizedMessage());
		}
	}

	// Convert item data retrieved from the message queue
	// into XML to pass back to the view
	private Document toXml(List<Message> itemList) {


		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			// Start building the XML
			Element root = doc.createElement( "Messages" );
			doc.appendChild( root );

			// Get the elements from the collection
			int custCount = itemList.size();

			// Iterate through the collection
			for ( int index=0; index < custCount; index++) {

				// Get the WorkItem object from the collection
				Message myMessage = itemList.get(index);

				Element item = doc.createElement( "Message" );
				root.appendChild( item );

				// Set the ID
				Element id = doc.createElement( "Data" );
				id.appendChild( doc.createTextNode(myMessage.getBody()));
				item.appendChild( id );

				// Set the name
				Element name = doc.createElement( "User" );
				name.appendChild( doc.createTextNode(myMessage.getName() ) );
				item.appendChild( name );

			}

			return doc;
		} catch(ParserConfigurationException e) {
			logger.error("An error occurred while the messages were parsed: " + e.getLocalizedMessage());
		}
		return null;
	}

	private String convertToString(Document xml) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(xml);
			transformer.transform(source, result);
			return result.getWriter().toString();

		} catch(TransformerException ex) {
			logger.error("An error occurred while converting the XLM document to a string" 
					+ ex.getLocalizedMessage());
		}
		return null;
	}

}
