package com.example.SpringAWSMessage;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Used as the Spring Boot controller that handles HTTP requests. 
 */
@Controller
public class MessageController {
	
	SendReceiveMessages msgService;

	@Autowired
	public MessageController(SendReceiveMessages service) {
		this.msgService = service;
	}
	
	@GetMapping("/")
	public String root() {
		return "index";
	}

	//  Purge the queue
	@RequestMapping(value = "/purge", method = RequestMethod.GET)
	@ResponseBody
	String purgeMessages(HttpServletRequest request, HttpServletResponse response) {

		msgService.purgeMyQueue();
		return "Queue is purged";
	}


	// Get messages
	@RequestMapping(value = "/populate", method = RequestMethod.GET)
	@ResponseBody
	String getItems(HttpServletRequest request, HttpServletResponse response) {

		String xml= msgService.getMessages();
		return xml;
	}

	// Create a message
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	String addItems(HttpServletRequest request, HttpServletResponse response) {

		String user = request.getParameter("user");
		String message = request.getParameter("message");

		// Generate the ID
		UUID uuid = UUID.randomUUID();
		String msgId = uuid.toString();

		Message messageOb = new Message();
		messageOb.setId(msgId);
		messageOb.setName(user);
		messageOb.setBody(message);

		msgService.processMessage(messageOb);
		String xml= msgService.getMessages();

		return xml;
	}

	@GetMapping("/message")
	public String greetingForm(Model model) {
		model.addAttribute("greeting", new Message());
		return "message";
	}
}
