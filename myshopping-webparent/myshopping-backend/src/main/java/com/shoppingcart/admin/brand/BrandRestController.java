package com.shoppingcart.admin.brand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoppingcart.common.entity.Brand;
import com.shoppingcart.common.entity.Category;

@RestController
public class BrandRestController {

	@Autowired
	private BrandService service;

	//kiểm tra email trùng
	@PostMapping("/brands/check_unique")
	public String checkUnique(Integer id, String name) {
		return service.checkUnique(id, name);
	}

	//dùng để lấy tất cả categories thuộc về brand hiện tại trong product_form_overview.js
	@GetMapping("/brands/{id}/categories") 
	public List<CategoryDTO> listCategoriesByBrand(@PathVariable(name = "id") Integer brandId)
			throws BrandNotFoundRestException {
		List<CategoryDTO> listCategories = new ArrayList<>();

		try {
			Brand brand = service.get(brandId);
			Set<Category> categories = brand.getCategories();//lấy ra tất cả categories thuộc về brand hiện tại

			//DTO: Data Transfer Object, đối tượng DTO dùng để lưu các thông tin giống đối tượng gốc nhưng có ít thông tin hơn đối tượng gốc 
			//DTO chỉ là model, ko phải là entity -->nó ko có table tương ứng trong db
			//khi dùng DTO sẽ đỡ tốn chi phí so với entity, vì nó chỉ có 1 số thông tin cần thiết
			for (Category category : categories) {//vì Brand và Category có mối quan hệ @ManyToMany, khi trả về list Categories thì nó sẽ ko hiểu mối quan hệ này -->gây ra lỗi -->phải tạo 1 đối tượng CategoryDTO để lưu trữ id và name của category và sẽ trả về list CategoriesDTO
				CategoryDTO dto = new CategoryDTO(category.getId(), category.getName());
				listCategories.add(dto);
			}

			return listCategories;
		} catch (BrandNotFoundException e) {
			throw new BrandNotFoundRestException();
		}
	}
}
