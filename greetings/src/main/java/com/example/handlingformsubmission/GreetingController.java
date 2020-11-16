package com.example.handlingformsubmission;

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
	@Autowired
	private DynamoDBEnhanced dde;

	@Autowired
	private PublishTextSMS msg;

	@GetMapping("/")
	public String greetingForm(Model model) {
		model.addAttribute("greeting", new Greeting());
		
		return "greeting";
	}
	
	@PostMapping("/greeting")
	public String greetingSubmit(@ModelAttribute Greeting greeting) {
	      //Persist submitted data into a DynamoDB table using the enhanced client
	      dde.injectDynamoItem(greeting);
	      // Send a mobile notification
	      msg.sendMessage(greeting.getId());

	      return "result";
	}
}
