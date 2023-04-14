package com.shoppingcart.admin.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shoppingcart.common.entity.product.Product;
import com.shoppingcart.common.exception.ProductNotFoundException;

@RestController
public class ProductRestController {

	@Autowired private ProductService service;
	
	//kiểm tra name trùng
	@PostMapping("/products/check_unique")
	public String checkUnique(Integer id, String name) {
		return service.checkUnique(id, name);
	}	
	
	//lấy thông tin của product thông qua id trong order_form_add_product 
	@GetMapping("/products/get/{id}")
	public ProductDTO getProductInfo(@PathVariable("id") Integer id) throws ProductNotFoundException {
		Product product = service.get(id);
		return new ProductDTO(product.getName(), product.getMainImagePath(), 
				product.getDiscountPrice(), product.getCost());
	}
}
