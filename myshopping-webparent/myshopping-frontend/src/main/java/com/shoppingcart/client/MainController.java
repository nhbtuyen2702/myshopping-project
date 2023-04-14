package com.shoppingcart.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.shoppingcart.client.category.CategoryService;
import com.shoppingcart.client.section.SectionService;
import com.shoppingcart.common.entity.Category;
import com.shoppingcart.common.entity.section.Section;
import com.shoppingcart.common.entity.section.SectionType;

@Controller
public class MainController {

	@Autowired private CategoryService categoryService;
	@Autowired private SectionService sectionService;
	
	//khi truy cập đường dẫn mặc định
	@GetMapping("")
	public String viewHomePage(Model model) {
		List<Section> listSections = sectionService.listEnabledSections();
		model.addAttribute("listSections", listSections);
		
		if (hasAllCategoriesSection(listSections)) {
			List<Category> listCategories = categoryService.listNoChildrenCategories();//chỉ lấy ra các categories ko có con, cháu
			model.addAttribute("listCategories", listCategories);
		}
		
		return "index";
	}
	
	private boolean hasAllCategoriesSection(List<Section> listSections) {
		for (Section section : listSections) {
			if (section.getType().equals(SectionType.ALL_CATEGORIES)) {
				return true;
			}
		}
		
		return false;
	}
	
	//khi nhấn login
	@GetMapping("/login")
	public String viewLoginPage() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {//nếu chưa login thì trả về trang login
			return "login";
		}
		
		return "redirect:/";//nếu đã login thì trả về trang index
	}	
}
