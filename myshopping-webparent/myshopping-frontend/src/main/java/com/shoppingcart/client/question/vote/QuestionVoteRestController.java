package com.shoppingcart.client.question.vote;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoppingcart.client.ControllerHelper;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.client.vote.VoteResult;
import com.shoppingcart.client.vote.VoteType;

@RestController
public class QuestionVoteRestController {

	@Autowired private ControllerHelper controllerHelper;
	@Autowired private QuestionVoteService service;
	
	@PostMapping("/vote_question/{id}/{type}")
	public VoteResult voteQuestion(@PathVariable(name = "id") Integer questionId,
			@PathVariable(name = "type") String type, HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		if (customer == null) {
			return VoteResult.fail("You must login to vote the question.");
		}
		
		VoteType voteType = VoteType.valueOf(type.toUpperCase());
		
		return service.doVote(questionId, customer, voteType);		
	}
	
}
