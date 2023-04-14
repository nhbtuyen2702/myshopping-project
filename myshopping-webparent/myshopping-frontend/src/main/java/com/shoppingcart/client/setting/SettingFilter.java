package com.shoppingcart.client.setting;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.shoppingcart.client.menu.MenuService;
import com.shoppingcart.common.entity.Menu;
import com.shoppingcart.common.entity.setting.Setting;

@Component
@Order(-123)
public class SettingFilter implements Filter {
	
	@Autowired private SettingService articleService;
	
	@Autowired private MenuService menuService;
	

	//tất cả các request trước khi gửi đến controller đều phải chạy qua phương thức doFilter
	//có thể sử dụng phương thức doFilter để làm 1 số tác vụ nào đó trước khi request được gửi đến controller 
	
	/*Ví dụ: 
	kiểm tra thông tin đăng nhập, nếu chưa đăng nhập thì chuyển hướng qua trang đăng nhập hoặc chặn nếu user ko được phép truy cập
	lấy dữ liệu trong db và gán vào ServletRequest -->nhờ cách này mà sau khi request gửi đến controller, controller có thể lấy ra các data nằm trong ServletRequest 
	ghi logs,...
	*/
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		String url = servletRequest.getRequestURL().toString();
		
		//tất cả request đến file css,js,images đều cho phép
		if (url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png") ||	url.endsWith(".jpg")) {
			chain.doFilter(request, response);//cho phép request gửi đến controller
			return;
		}
		
		loadGeneralSettings(request);
		loadMenuSettings(request);
		
		chain.doFilter(request, response);//cho phép request gửi đến controller

	}
	
	private void loadMenuSettings(ServletRequest request) {
		List<Menu> headerMenuItems = menuService.getHeaderMenuItems();
		request.setAttribute("headerMenuItems", headerMenuItems);

		List<Menu> footerMenuItems = menuService.getFooterMenuItems();
		request.setAttribute("footerMenuItems", footerMenuItems);		
	}

	private void loadGeneralSettings(ServletRequest request) {
		List<Setting> generalSettings = articleService.getGeneralSettings();
		
		generalSettings.forEach(setting -> {
			request.setAttribute(setting.getKey(), setting.getValue());
			System.out.println(setting.getKey() + " == > " + setting.getValue());
		});
		
	}

}
