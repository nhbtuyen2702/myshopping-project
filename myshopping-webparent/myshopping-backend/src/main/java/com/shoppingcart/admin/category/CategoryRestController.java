package com.shoppingcart.admin.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryRestController {

	@Autowired
	private CategoryService service;

	//kiểm tra email trùng
	@PostMapping("/categories/check_unique")
	public String checkUnique(@Param("id") Integer id, @Param("name") String name, @Param("alias") String alias) {//params = {id: catId, name: catName, alias: catAlias} -->khi dùng @Param thì các tham số ko cần đúng thứ tự
		return service.checkUnique(id, name, alias);
	}
}