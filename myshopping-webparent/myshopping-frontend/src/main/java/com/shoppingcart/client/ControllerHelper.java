package com.shoppingcart.client;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.client.customer.CustomerService;

@Component
public class ControllerHelper {
	
	@Autowired private CustomerService customerService;
	
	//phương thức này sẽ lấy ra customer từ db dựa vào email
	public Customer getAuthenticatedCustomer(HttpServletRequest request) {
		String email = Utility.getEmailOfAuthenticatedCustomer(request);				
		return customerService.getCustomerByEmail(email);
	}
}
