package com.shoppingcart.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.shoppingcart.admin.dashboard.DashboardInfo;
import com.shoppingcart.admin.dashboard.DashboardService;

@Controller
public class MainController {

	@Autowired private DashboardService dashboardService;
	
	//đường dẫn mặc định của app
	@GetMapping("")
	public String viewHomePage(Model model) {
		DashboardInfo summary = dashboardService.loadSummary();
		model.addAttribute("summary", summary);		
		return "index";
	}

	//đường dẫn đến file login
	@GetMapping("/login")
	public String viewLoginPage() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();//trường hợp vừa login xong, đang ở trang index, nhấn back lại thì nó vẫn giữ ở trang index
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {//nếu authentication == null hoặc authentication instanceof AnonymousAuthenticationToken = true có nghĩa là chưa login -->trả về trang login
			return "login";
		}

		return "redirect:/";
	}
}
