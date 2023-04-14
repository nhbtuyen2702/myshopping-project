package com.shoppingcart.client.setting;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shoppingcart.common.entity.Currency;
import com.shoppingcart.common.entity.setting.Setting;
import com.shoppingcart.common.entity.setting.SettingCategory;

@Service
public class SettingService {//class này chứa tất cả các settings
	
	@Autowired private SettingRepository settingRepo;
	@Autowired private CurrencyRepository currencyRepo;
	
	/*
	INSERT INTO `settings` VALUES 
	('MAIL_FROM','nhbtuyen2702@gmail.com','MAIL_SERVER'),
	('MAIL_HOST','smtp.gmail.com','MAIL_SERVER'),
	('MAIL_PASSWORD','gpctiolgpwrabzxn','MAIL_SERVER'),
	('MAIL_PORT','587','MAIL_SERVER'),
	('MAIL_SENDER_NAME','Shopping Team','MAIL_SERVER'),
	('MAIL_USERNAME','nhbtuyen2702@gmail.com','MAIL_SERVER'),
	('SMTP_AUTH','true','MAIL_SERVER'),
	('SMTP_SECURED','true','MAIL_SERVER')
	*/

	/* Insert data sau vào table settings
	key					value					category
	MAIL_HOST			smtp.gmail.com			MAIL_SERVER
	MAIL_PORT			587						MAIL_SERVER
	MAIL_USERNAME		nhbtuyen2702@gmail.com	MAIL_SERVER				
	MAIL_PASSWORD		gpctiolgpwrabzxn		MAIL_SERVER
	MAIL_FROM			nhbtuyen2702@gmail.com	MAIL_SERVER
	SMTP_AUTH			true					MAIL_SERVER
	SMTP_SECURED		true					MAIL_SERVER
	MAIL_SENDER_NAME	Shopping Team			MAIL_SERVER	
	
	Cách lấy các giá trị ở trên
	1/ Đăng nhập vào gmail > chọn setting(cài đặt) > See all settings(xem tất cả chế độ cài đặt) > Forwarding and POP/MAP(Chuyển tiếp và POP/IMAP) > chọn Configuration Instructions(Hướng dẫn định cấu hình) > chọn step 2(bước 2)
	-->Copy giá trị tương ứng trong mục Outgoing Mail(SMTP) Server(Máy chủ thư đi (SMTP)
	MAIL_HOST = smtp.gmail.com
	MAIL_PORT = Port for TLS/STARTTLS(Cổng cho TLS/STARTTLS) = 587
	
	2/ Đăng nhập đường dẫn: https://myaccount.google.com/apppasswords > nhập email và password > Select app(chọn ứng dụng), chọn Other(khác) > nhập My Shop > Tạo > copy mã(gpctiolgpwrabzxn)
	Lưu ý: nếu như hệ thống báo cài đặt ko khả dụng khi truy cập đường dẫn này thì phải bật tính năng xác minh 2 bước trên tài khoản này.
	Nhấn vào avatar > Quản lý tài khoản google của bạn > bảo mật > xác minh 2 bước > ...
	*/
	public EmailSettingBag getEmailSettings() {//các settings liên quan đến email
		List<Setting> settings = settingRepo.findByCategory(SettingCategory.MAIL_SERVER);//lấy tất cả các records có SettingCategory là MAIL_SERVER
		settings.addAll(settingRepo.findByCategory(SettingCategory.MAIL_TEMPLATES));
		
		return new EmailSettingBag(settings);//tạo đối tượng EmailSettingBag bằng Constructor có 1 tham số là settings, settings sẽ được gán vào thuộc tính listSettings của SettingBag
	}
	
	public List<Setting> getGeneralSettings() {
		return settingRepo.findByTwoCategories(SettingCategory.GENERAL, SettingCategory.CURRENCY);
	}
	
	public CurrencySettingBag getCurrencySettings() {
		List<Setting> settings = settingRepo.findByCategory(SettingCategory.CURRENCY);
		return new CurrencySettingBag(settings);
	}
	
	public PaymentSettingBag getPaymentSettings() {
		List<Setting> settings = settingRepo.findByCategory(SettingCategory.PAYMENT);
		return new PaymentSettingBag(settings);
	}
	
	public String getCurrencyCode() {
		Setting setting = settingRepo.findByKey("CURRENCY_ID");
		Integer currencyId = Integer.parseInt(setting.getValue());
		Currency currency = currencyRepo.findById(currencyId).get();
		
		return currency.getCode();
	}
	
}
