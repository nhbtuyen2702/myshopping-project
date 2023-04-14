package com.shoppingcart.client.menu;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shoppingcart.common.entity.Article;
import com.shoppingcart.common.entity.Menu;
import com.shoppingcart.common.entity.MenuType;
import com.shoppingcart.common.exception.MenuItemNotFoundException;

@Service
public class MenuService {
	
	@Autowired private MenuRepository repo;
	
	public List<Menu> getHeaderMenuItems() {
		return repo.findByTypeAndEnabledOrderByPositionAsc(MenuType.HEADER, true);
	}
	
	public List<Menu> getFooterMenuItems() {
		return repo.findByTypeAndEnabledOrderByPositionAsc(MenuType.FOOTER, true);
	}
	
	public Article getArticleBoundToMenu(String menuAlias) throws MenuItemNotFoundException {
		Menu menu = repo.findByAlias(menuAlias);
		if (menu == null) {
			throw new MenuItemNotFoundException("No menu found with alias " + menuAlias);
		}
		
		return menu.getArticle();
	}	
}
