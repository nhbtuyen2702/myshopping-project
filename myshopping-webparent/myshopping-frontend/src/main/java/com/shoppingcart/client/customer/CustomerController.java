package com.shoppingcart.client.customer;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shoppingcart.client.Utility;
import com.shoppingcart.common.entity.Country;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.client.security.CustomerUserDetails;
import com.shoppingcart.client.security.oauth.CustomerOAuth2User;
import com.shoppingcart.client.setting.EmailSettingBag;
import com.shoppingcart.client.setting.SettingService;

@Controller
public class CustomerController {
	
	@Autowired private CustomerService customerService;
	@Autowired private SettingService settingService;
	
	//http://localhost:8083/ShoppingCartClient/customers/create?firstName=Nguyen&lastName=Tuyen
	@GetMapping("/customers/create")
	@ResponseBody
	public String create(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName,
							HttpServletRequest request) {
		String requestURL = request.getRequestURL().toString();// http://localhost:8083/ShoppingCartClient/customers/create
		String requestURI = request.getRequestURI();// /ShoppingCartClient/customers/create
		String contextPath = request.getContextPath();// /ShoppingCartClient
		String serverName = request.getServerName();// localhost
		int serverPort = request.getServerPort();// 8083
		String servletPath = request.getServletPath();// /customers/create
		String queryString = request.getQueryString();// firstName=Nguyen&lastName=Tuyen
		String parameter1 = request.getParameter("firstName");// Nguyen
		String parameter2 = request.getParameter("lastName");// Tuyen
		
		String info = ("Request URL: " + requestURL
				+ "\nRequest URI: " + requestURI
				+ "\nContext Path: " + contextPath
				+ "\nServer Name: " + serverName
				+ "\nServer Port: " + serverPort
				+ "\nServlet Path: " + servletPath
				+ "\nQuery String: " + queryString
				+ "\nParameter 1: " + parameter1
				+ "\nParameter 2: " + parameter2
				);
		
		return info;
	}
	
	//khi nhấn vào register trong trang login
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		List<Country> listCountries = customerService.listAllCountries();//lấy tất cả countries để đổ lên dropdown
		
		model.addAttribute("listCountries", listCountries);
		model.addAttribute("pageTitle", "Customer Registration");
		model.addAttribute("customer", new Customer());
		
		return "register/register_form";
	}
	
	//khi nhấn save customer trong register_form
	@PostMapping("/create_customer")
	public String createCustomer(Customer customer, Model model,
			HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		customerService.registerCustomer(customer);
		sendVerificationEmail(request, customer);//gửi mail xác thực
		
		model.addAttribute("pageTitle", "Registration Succeeded!");
		
		return "register/register_success";//trả về trang đăng ký thành công
	}

	//khi tạo mới customer thì sẽ gửi link verify qua mail, khi customer nhấn vào link verify trong mail thì update enabled = true 
	private void sendVerificationEmail(HttpServletRequest request, Customer customer) 
			throws UnsupportedEncodingException, MessagingException {
		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
		
		String toAddress = customer.getEmail();
		String subject = emailSettings.getCustomerVerifySubject();
		String content = emailSettings.getCustomerVerifyContent();
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());//nhbtuyen2702@gmail.com, Shopping Team
		helper.setTo(toAddress);//email của customer
		helper.setSubject(subject);//tiêu đề email
		
		content = content.replace("[[name]]", customer.getFullName());//thay [[name]] trong content bằng fullname của customer
		
		String verifyURL = Utility.getSiteURL(request) + "/verify?code=" + customer.getVerificationCode();//http://localhost:8083/ShoppingCartClient/verify?code=fwOtOrKtbx2u7yI7ExYyVLPfaBXXv3vudzfqaF909CT1KEL8I5MVB2iyxX4n4i0l
		
		content = content.replace("[[URL]]", verifyURL);//thay [[URL]] trong content bằng verifyURL
		
		helper.setText(content, true);//nội dung email
		
		mailSender.send(message);//gửi mail cho customer
		
		System.out.println("to Address: " + toAddress);
		System.out.println("Verify URL: " + verifyURL);
	}	
	
	//khi customer bấm vào link verify trong mail thì sẽ so sánh verify code trong link có giống với verify code được lưu trong db hay ko -->nếu giống thì update enabled = true và xóa verify code này trong db
	@GetMapping("/verify")
	public String verifyAccount(String code, Model model) {
		boolean verified = customerService.verify(code);
		
		return "register/" + (verified ? "verify_success" : "verify_fail");
	}

	//khi customer bấm vào fullName để xem detail
	@GetMapping("/account_details")
	public String viewAccountDetails(Model model, HttpServletRequest request) {
		String email = Utility.getEmailOfAuthenticatedCustomer(request);//phương thức này sẽ lấy ra customer đang đăng nhập
		Customer customer = customerService.getCustomerByEmail(email);
		List<Country> listCountries = customerService.listAllCountries();
		
		model.addAttribute("customer", customer);
		model.addAttribute("listCountries", listCountries);
		
		return "customer/account_form";
	}
	
	//khi customer bấm save trong account_form
	@PostMapping("/update_account_details")
	public String updateAccountDetails(Model model, Customer customer, RedirectAttributes ra,
			HttpServletRequest request) {
		customerService.update(customer);
		ra.addFlashAttribute("message", "Your account details have been updated.");
		
		updateNameForAuthenticatedCustomer(customer, request);//khi thay đổi firstName hoặc lastName hoặc cả 2 thì update lại đối tượng principal để hiển thị fullName sau khi đã update
		
		String redirectOption = request.getParameter("redirect");//lấy ra param redirect trong queryString
		String redirectURL = "redirect:/account_details";//nếu ko từ trang addresses hoặc trang cart hoặc trang checkout qua thì trả về trang account_form 
		
		if ("address_book".equals(redirectOption)) {
			redirectURL = "redirect:/address_book";//nếu customer đang ở trang addresses và nhấn qua trang account_form thì sau khi update customer sẽ trả về trang addresses
		} else if ("cart".equals(redirectOption)) {
			redirectURL = "redirect:/cart";//nếu customer đang ở trang cart và nhấn qua trang account_form thì sau khi update customer sẽ trả về trang cart
		} else if ("checkout".equals(redirectOption)) {
			redirectURL = "redirect:/address_book?redirect=checkout";//nếu customer đang ở trang checkout và nhấn qua trang account_form thì sau khi update customer sẽ trả về trang checkout
		}
		
		return redirectURL;
	}

	private void updateNameForAuthenticatedCustomer(Customer customer, HttpServletRequest request) {
		Object principal = request.getUserPrincipal();
		
		if (principal instanceof UsernamePasswordAuthenticationToken 
				|| principal instanceof RememberMeAuthenticationToken) {
			CustomerUserDetails userDetails = getCustomerUserDetailsObject(principal);//lấy ra customer đang login để thay đổi firstName và lastName -->fullName của customer hiển thị trên html cũng sẽ thay đổi theo
			Customer authenticatedCustomer = userDetails.getCustomer();
			authenticatedCustomer.setFirstName(customer.getFirstName());
			authenticatedCustomer.setLastName(customer.getLastName());
		
		} else if (principal instanceof OAuth2AuthenticationToken) {
			OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
			CustomerOAuth2User oauth2User = (CustomerOAuth2User) oauth2Token.getPrincipal();
			String fullName = customer.getFirstName() + " " + customer.getLastName();
			oauth2User.setFullName(fullName);
		}		
	}
	
	private CustomerUserDetails getCustomerUserDetailsObject(Object principal) {
		CustomerUserDetails userDetails = null;
		if (principal instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
			userDetails = (CustomerUserDetails) token.getPrincipal();
		} else if (principal instanceof RememberMeAuthenticationToken) {
			RememberMeAuthenticationToken token = (RememberMeAuthenticationToken) principal;
			userDetails = (CustomerUserDetails) token.getPrincipal();
		}
		
		return userDetails;
	}
}
