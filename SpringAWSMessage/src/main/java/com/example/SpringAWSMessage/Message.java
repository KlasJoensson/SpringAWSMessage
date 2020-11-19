package com.example.SpringAWSMessage;

/**
 * Used as the model for this application.
 */
public class Message {
	
	private String id;
	private String body;
	private String name;


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
