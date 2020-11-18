package com.example.handlingformsubmission;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A Java class that represents the controller for this application.
 */
@Controller
public class GreetingController {

	private DynamoDBEnhanced dde;
	
	private PublishTextSMS msg;
	
	private static Logger log = LogManager.getLogger(GreetingController.class);

	@Autowired
	public GreetingController(DynamoDBEnhanced dDB, PublishTextSMS smsMessage) {
		this.dde = dDB;
		this.msg = smsMessage;
	}

	@GetMapping("/")
	public String greetingForm(Model model) {
		log.debug("Got a get call to deal with...");
		model.addAttribute("greeting", new Greeting());

		return "greeting";
	}

	@PostMapping("/greeting")
	public String greetingSubmit(@ModelAttribute Greeting greeting) {
		log.debug("Got a post call to deal with...");
		//Persist submitted data into a DynamoDB table using the enhanced client
		dde.injectDynamoItem(greeting);
		// Send a mobile notification
		msg.sendMessage(greeting.getId());

		return "result";
	}
}
