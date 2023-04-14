package com.shoppingcart.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import com.shoppingcart.client.security.oauth.CustomerOAuth2User;
import com.shoppingcart.client.setting.CurrencySettingBag;
import com.shoppingcart.client.setting.EmailSettingBag;

public class Utility {
	
	//HttpServletRequest có thể lấy ra tất cả thông tin của request gửi đến
	public static String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();//http://localhost:8083/ShoppingCartClient/create_customer	http://localhost:8083/ShoppingCartClient/forgot_password
		
		return siteURL.replace(request.getServletPath(), "");//thay /create_customer bằng ""	thay /forgot_password bằng "" 
	}
	
	//lấy các thông tin cấu hình gmail để gán vào đối tượng javaMailSenderImpl
	public static JavaMailSenderImpl prepareMailSender(EmailSettingBag settings) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();//tạo đối tượng javaMailSenderImpl và gán các thông tin để gửi mail
		
		mailSender.setHost(settings.getHost());//smtp.gmail.com
		mailSender.setPort(settings.getPort());//587
		mailSender.setUsername(settings.getUsername());//nhbtuyen2702@gmail.com
		mailSender.setPassword(settings.getPassword());//gpctiolgpwrabzxn
		
		Properties mailProperties = new Properties();
		mailProperties.setProperty("mail.smtp.auth", settings.getSmtpAuth());//true
		mailProperties.setProperty("mail.smtp.starttls.enable", settings.getSmtpSecured());//true
		
		mailSender.setJavaMailProperties(mailProperties);
		
		return mailSender;
	}
	
	//phương thức này sẽ lấy ra customer đang đăng nhập, sau đó trả về email của customer đó
	public static String getEmailOfAuthenticatedCustomer(HttpServletRequest request) {
		Object principal = request.getUserPrincipal();
		if (principal == null) return null;
		
		String customerEmail = null;
		
		if (principal instanceof UsernamePasswordAuthenticationToken //customer này được tạo bằng username và password
				|| principal instanceof RememberMeAuthenticationToken) {
			customerEmail = request.getUserPrincipal().getName();
		} else if (principal instanceof OAuth2AuthenticationToken) {//customer này được tạo thông qua google hoặc facebook
			OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
			CustomerOAuth2User oauth2User = (CustomerOAuth2User) oauth2Token.getPrincipal();
			customerEmail = oauth2User.getEmail();
		}
		
		return customerEmail;
	}	
	
	public static String formatCurrency(float amount, CurrencySettingBag settings) {
		String symbol = settings.getSymbol();
		String symbolPosition = settings.getSymbolPosition();
		String decimalPointType = settings.getDecimalPointType();
		String thousandPointType = settings.getThousandPointType();
		int decimalDigits = settings.getDecimalDigits();
		
		String pattern = symbolPosition.equals("Before price") ? symbol : "";
		pattern += "###,###";
		
		if (decimalDigits > 0) {
			pattern += ".";
			for (int count = 1; count <= decimalDigits; count++) pattern += "#";
		}
		
		pattern += symbolPosition.equals("After price") ? symbol : "";
		
		char thousandSeparator = thousandPointType.equals("POINT") ? '.' : ',';
		char decimalSeparator = decimalPointType.equals("POINT") ? '.' : ',';
		
		DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
		decimalFormatSymbols.setDecimalSeparator(decimalSeparator);
		decimalFormatSymbols.setGroupingSeparator(thousandSeparator);
		
		DecimalFormat formatter = new DecimalFormat(pattern, decimalFormatSymbols);
		
		return formatter.format(amount);
	}
	
}
