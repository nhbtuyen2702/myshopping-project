package com.shoppingcart.client.checkout;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.shoppingcart.client.ControllerHelper;
import com.shoppingcart.client.Utility;
import com.shoppingcart.client.address.AddressService;
import com.shoppingcart.client.checkout.paypal.PayPalApiException;
import com.shoppingcart.client.checkout.paypal.PayPalService;
import com.shoppingcart.common.entity.Address;
import com.shoppingcart.common.entity.CartItem;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.common.entity.ShippingRate;
import com.shoppingcart.common.entity.order.Order;
import com.shoppingcart.common.entity.order.PaymentMethod;
import com.shoppingcart.client.order.OrderService;
import com.shoppingcart.client.setting.CurrencySettingBag;
import com.shoppingcart.client.setting.EmailSettingBag;
import com.shoppingcart.client.setting.PaymentSettingBag;
import com.shoppingcart.client.setting.SettingService;
import com.shoppingcart.client.shipping.ShippingRateService;
import com.shoppingcart.client.shoppingcart.ShoppingCartService;

@Controller
public class CheckoutController {

	@Autowired private CheckoutService checkoutService;
	@Autowired private ControllerHelper controllerHelper;
	@Autowired private AddressService addressService;
	@Autowired private ShippingRateService shipService;
	@Autowired private ShoppingCartService cartService;
	@Autowired private OrderService orderService;
	@Autowired private SettingService settingService;
	@Autowired private PayPalService paypalService;
	
	//khi nhấn vào checkout
	@GetMapping("/checkout")
	public String showCheckoutPage(Model model, HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);//dùng email để lấy ra customer tương ứng
		
		Address defaultAddress = addressService.getDefaultAddress(customer);//lấy ra address có default = true
		ShippingRate shippingRate = null;
		
		if (defaultAddress != null) {
			model.addAttribute("shippingAddress", defaultAddress.toString());//nếu có bất kỳ address nào có default = true -->lấy address này để ship 
			shippingRate = shipService.getShippingRateForAddress(defaultAddress);
		} else {
			model.addAttribute("shippingAddress", customer.toString());//nếu tồn tại address có default = true(có thể chưa tạo mới address hoặc đã tạo nhưng ko chọn default) -->lấy primary address để ship
			shippingRate = shipService.getShippingRateForCustomer(customer);
		}
		
		if (shippingRate == null) {
			return "redirect:/cart";//nếu shippingRate == null -->ko hỗ trợ ship với address này -->trả về trang cart để update address
		}
		
		List<CartItem> cartItems = cartService.listCartItems(customer);//lấy ra tất cả cartItem của customer hiện tại
		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(cartItems, shippingRate);//tính toán tất cả chi phí dựa vào các cartItems
		
		String currencyCode = settingService.getCurrencyCode();//lấy ra currency đang chọn trong setting
		PaymentSettingBag paymentSettings = settingService.getPaymentSettings();
		String paypalClientId = paymentSettings.getClientID();
		
		model.addAttribute("paypalClientId", paypalClientId);
		model.addAttribute("currencyCode", currencyCode);
		model.addAttribute("customer", customer);
		model.addAttribute("checkoutInfo", checkoutInfo);
		model.addAttribute("cartItems", cartItems);
		
		return "checkout/checkout";
	}
	
	//khi nhấn vào Place Order with COD
	@PostMapping("/place_order")
	public String placeOrder(HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
		String paymentType = request.getParameter("paymentMethod");
		PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);//chuyển từ String thành enum
		
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		
		Address defaultAddress = addressService.getDefaultAddress(customer);//lấy ra address có default = true
		ShippingRate shippingRate = null;
		
		if (defaultAddress != null) {
			shippingRate = shipService.getShippingRateForAddress(defaultAddress);//nếu có bất kỳ address nào có default = true -->lấy address này để ship 
		} else {
			shippingRate = shipService.getShippingRateForCustomer(customer);//nếu ko có bất kỳ address nào có default = true(có thể chưa tạo mới address hoặc đã tạo nhưng ko chọn default) -->lấy primary address để ship
		}
		
		List<CartItem> cartItems = cartService.listCartItems(customer);
		CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(cartItems, shippingRate);
		
		Order createdOrder = orderService.createOrder(customer, defaultAddress, cartItems, paymentMethod, checkoutInfo);//tạo order
		cartService.deleteByCustomer(customer);//sau khi tạo order thì xóa tất cả các cartItems thuộc về customer hiện tại trong db
		sendOrderConfirmationEmail(request, createdOrder);//gửi mail các thông tin của order 
		
		return "checkout/order_completed";//trả về trang hiển thị đã order thành công
	}

	private void sendOrderConfirmationEmail(HttpServletRequest request, Order order) 
			throws UnsupportedEncodingException, MessagingException {
		EmailSettingBag emailSettings = settingService.getEmailSettings();
		JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
		mailSender.setDefaultEncoding("utf-8");
		
		String toAddress = order.getCustomer().getEmail();
		String subject = emailSettings.getOrderConfirmationSubject();
		String content = emailSettings.getOrderConfirmationContent();

		subject = subject.replace("[[orderId]]", String.valueOf(order.getId()));
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
		helper.setTo(toAddress);
		helper.setSubject(subject);
		
		DateFormat dateFormatter =  new SimpleDateFormat("HH:mm:ss E, dd MMM yyyy");
		String orderTime = dateFormatter.format(order.getOrderTime());
		
		CurrencySettingBag currencySettings = settingService.getCurrencySettings();
		String totalAmount = Utility.formatCurrency(order.getTotal(), currencySettings);
		
		content = content.replace("[[name]]", order.getCustomer().getFullName());
		content = content.replace("[[orderId]]", String.valueOf(order.getId()));
		content = content.replace("[[orderTime]]", orderTime);
		content = content.replace("[[shippingAddress]]", order.getShippingAddress());
		content = content.replace("[[total]]", totalAmount);
		content = content.replace("[[paymentMethod]]", order.getPaymentMethod().toString());
		
		helper.setText(content, true);
		mailSender.send(message);		
	}
	
	@PostMapping("/process_paypal_order")
	public String processPayPalOrder(HttpServletRequest request, Model model) 
			throws UnsupportedEncodingException, MessagingException {
		String orderId = request.getParameter("orderId");
		
		String pageTitle = "Checkout Failure";
		String message = null;
		
		try {
			if (paypalService.validateOrder(orderId)) {
				return placeOrder(request);
			} else {
				pageTitle = "Checkout Failure";
				message = "ERROR: Transaction could not be completed because order information is invalid";
			}
		} catch (PayPalApiException e) {
			message = "ERROR: Transaction failed due to error: " + e.getMessage();
		}
		
		model.addAttribute("pageTitle", pageTitle);
		model.addAttribute("title", pageTitle);
		model.addAttribute("message", message);
		
		return "message";
	}
	
}
