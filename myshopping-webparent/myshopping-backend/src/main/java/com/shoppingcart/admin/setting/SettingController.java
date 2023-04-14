package com.shoppingcart.admin.setting;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shoppingcart.admin.FileUploadUtil;
import com.shoppingcart.common.entity.Currency;
import com.shoppingcart.common.entity.setting.Setting;

@Controller
public class SettingController {

	@Autowired private SettingService service;
	
	@Autowired private CurrencyRepository currencyRepo;
	
	/*
	INSERT INTO `currencies` VALUES 
	(1,'United States Dollar','$','USD'),
	(2,'British Pound','£','GPB'),
	(3,'Japanese Yen','¥','JPY'),
	(4,'Euro','€','EUR'),
	(5,'Russian Ruble','₽','RUB'),
	(6,'South Korean Won','₩','KRW'),
	(7,'Chinese Yuan','¥','CNY'),
	(8,'Brazilian Real','R$','BRL'),
	(9,'Australian Dollar','$','AUD'),
	(10,'Canadian Dollar','$','CAD'),
	(11,'Vietnamese đồng','₫','VND'),
	(12,'Indian Rupee','₹','INR')
	*/
	
	/*
	INSERT INTO `settings` VALUES 
	('SITE_NAME','Shopping','GENERAL'),
	('SITE_LOGO','shopping-logo.png','GENERAL'),
	('COPYRIGHT','Copyright (C) 2021 My Shop Ltd.','GENERAL'),
	('CURRENCY_ID','1','CURRENCY'),
	('CURRENCY_SYMBOL','$','CURRENCY'),
	('CURRENCY_SYMBOL_POSITION','before','CURRENCY'),
	('DECIMAL_POINT_TYPE','POINT','CURRENCY'),
	('DECIMAL_DIGITS','2','CURRENCY'),
	('THOUSANDS_POINT_TYPE','COMMA','CURRENCY')
	*/
	
	/* Customer Verification
	subject = "Please verify your registration to continue shopping";
		
	content = "<p>Dear [[name]],</p>"
			+ "<br>"
			+ "<p>Click the link below to verify your account:</p>"
			+ "<br>"
			+ "<p><a href=\"" + "[[URL]]" + "\">VERIFY</a></p>"
			+ "<br>"
			+ "<p>Thanks,</p>"
			+ "<p>The Shopping Team.</p>";
	*/
	
	/* Order Confirmation
	subject = "Confirm of your order ID #[[orderId]]";
		
	content = "<p>Dear [[name]],</p>"
			+ "<br>"
			+ "<p>This email is to confirm that you have successfully placed an order through our website. Please review the following order summary:</p>"
			+ "<br>"
			+ "<p>- Order ID: [[orderId]]</p>"
			+ "<p>- Order time: [[orderTime]]</p>"
			+ "<p>- Ship to: [[shippingAddress]]</p>"
			+ "<p>- Total: $ [[total]]</p>"
			+ "<p>- Payment method: [[paymentMethod]]</p>"
			+ "<br>"
			+ "<p>We're currently processing your order. Click here to view full details of your order on our website(login required).</p>"
			+ "<br>"
			+ "<p>Thanks for purchasing products at Shopping</p>"
			+ "<p>The Shopping Team.</p>";
	*/
	
	/*
	INSERT INTO `settings` VALUES 
	('CUSTOMER_VERIFY_SUBJECT','Please verify your registration to continue shopping','MAIL_SERVER'),
	('CUSTOMER_VERIFY_CONTENT','<p>Dear [[name]],</p><br><p>Click the link below to verify your account:</p><br><p><a href="[[URL]]">VERIFY</a></p><br><p>Thanks,</p><p>The Shopping Team.</p>','MAIL_SERVER'),
	('ORDER_CONFIRMATION_SUBJECT','Confirm of your order ID #[[orderId]]','MAIL_SERVER'),
	('ORDER_CONFIRMATION_CONTENT','<p>Dear [[name]],</p><br><p>This email is to confirm that you have successfully placed an order through our website. Please review the following order summary:</p><br><p>- Order ID: [[orderId]]</p><p>- Order time: [[orderTime]]</p><p>- Ship to: [[shippingAddress]]</p><p>- Total: $ [[total]]</p><p>- Payment method: [[paymentMethod]]</p><br><p>We"re currently processing your order. Click here to view full details of your order on our website(login required).</p><br><p>Thanks for purchasing products at Shopping</p><p>The Shopping Team.</p>','MAIL_SERVER')
	*/

	@GetMapping("/settings")
	public String listAll(Model model) {
		List<Setting> listSettings = service.listAllSettings();
		List<Currency> listCurrencies = currencyRepo.findAllByOrderByNameAsc();
		
		model.addAttribute("listCurrencies", listCurrencies);
		
		for (Setting setting : listSettings) {
			model.addAttribute(setting.getKey(), setting.getValue());
		}
		
		return "settings/settings";
	}
	
	/*
	SITE_LOGO					shopping-logo.png						GENERAL
	SITE_NAME					Shopping								GENERAL
	COPYRIGHT					Copyright (C) 2021 My Shop Ltd.	GENERAL
	CURRENCY_ID					1										CURRENCY
	CURRENCY_SYMBOL				$										CURRENCY
	CURRENCY_SYMBOL_POSITION	Before price							CURRENCY
	*/
	@PostMapping("/settings/save_general")
	public String saveGeneralSettings(@RequestParam("fileImage") MultipartFile multipartFile,
			HttpServletRequest request, RedirectAttributes ra) throws IOException {
		GeneralSettingBag settingBag = service.getGeneralSettings();
		
		saveSiteLogo(multipartFile, settingBag);
		saveCurrencySymbol(request, settingBag);
		
		updateSettingValuesFromForm(request, settingBag.list());
		
		ra.addFlashAttribute("message", "General settings have been saved.");
		
		return "redirect:/settings";
	}

	private void saveSiteLogo(MultipartFile multipartFile, GeneralSettingBag settingBag) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			String value = "/site-logo/" + fileName;
			settingBag.updateSiteLogo(value);
			
			String uploadDir = "site-logo";
			FileUploadUtil.removeDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}
	}
	
	private void saveCurrencySymbol(HttpServletRequest request, GeneralSettingBag settingBag) {
		Integer currencyId = Integer.parseInt(request.getParameter("CURRENCY_ID"));
		Optional<Currency> findByIdResult = currencyRepo.findById(currencyId);
		
		if (findByIdResult.isPresent()) {//chỉ được chọn currency tồn tại trong table currencies
			Currency currency = findByIdResult.get();
			settingBag.updateCurrencySymbol(currency.getSymbol());
		}
	}
	
	private void updateSettingValuesFromForm(HttpServletRequest request, List<Setting> listSettings) {
		for (Setting setting : listSettings) {
			String value = request.getParameter(setting.getKey());
			if (value != null) {
				setting.setValue(value);
			}
		}
		
		service.saveAll(listSettings);
	}
	
	/*
	MAIL_FROM					nhbtuyen2702@gmail.com	MAIL_SERVER
	MAIL_HOST					smtp.gmail.com			MAIL_SERVER
	MAIL_PASSWORD				gpctiolgpwrabzxn		MAIL_SERVER
	MAIL_PORT					587						MAIL_SERVER
	MAIL_SENDER_NAME			Shopping Team			MAIL_SERVER
	MAIL_USERNAME				nhbtuyen2702@gmail.com	MAIL_SERVER
	SMTP_AUTH					true					MAIL_SERVER
	SMTP_SECURED				true					MAIL_SERVER
	*/
	@PostMapping("/settings/save_mail_server")
	public String saveMailServerSetttings(HttpServletRequest request, RedirectAttributes ra) {
		List<Setting> mailServerSettings = service.getMailServerSettings();
		updateSettingValuesFromForm(request, mailServerSettings);
		
		ra.addFlashAttribute("message", "Mail server settings have been saved");
		
		return "redirect:/settings#mailServer";
	}
	
	/*
	CUSTOMER_VERIFY_CONTENT		<p>Dear [[name]],</p><br><p>Click the link below to verify your account:</p><br><p><a href="[[URL]]">VERIFY</a></p><br><p>Thanks,</p><p>The Shopping Team.</p>	MAIL_TEMPLATES
	CUSTOMER_VERIFY_SUBJECT		Please verify your registration to continue shopping	MAIL_TEMPLATES
	ORDER_CONFIRMATION_CONTENT	<p>Dear [[name]],</p><br><p>This email is to confirm that you have successfully placed an order through our website. Please review the following order summary:</p><br><p>- Order ID: [[orderId]]</p><p>- Order time: [[orderTime]]</p><p>- Ship to: [[shippin...	MAIL_TEMPLATES
	ORDER_CONFIRMATION_SUBJECT	Confirm of your order ID #[[orderId]]	MAIL_TEMPLATES
	*/
	@PostMapping("/settings/save_mail_templates")
	public String saveMailTemplateSetttings(HttpServletRequest request, RedirectAttributes ra) {
		List<Setting> mailTemplateSettings = service.getMailTemplateSettings();
		updateSettingValuesFromForm(request, mailTemplateSettings);
		
		ra.addFlashAttribute("message", "Mail template settings have been saved");
		
		return "redirect:/settings#mailTemplates";
	}
	
	@PostMapping("/settings/save_payment")
	public String savePaymentSetttings(HttpServletRequest request, RedirectAttributes ra) {
		List<Setting> paymentSettings = service.getPaymentSettings();
		updateSettingValuesFromForm(request, paymentSettings);
		
		ra.addFlashAttribute("message", "Payment settings have been saved");
		
		return "redirect:/settings#payment";
	}		
}
