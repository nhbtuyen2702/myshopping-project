package com.shoppingcart.client.shoppingcart;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shoppingcart.common.entity.CartItem;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.common.entity.product.Product;
import com.shoppingcart.client.product.ProductRepository;

@Service
@Transactional
public class ShoppingCartService {

	@Autowired private CartItemRepository cartRepo;
	@Autowired private ProductRepository productRepo;
	
	public Integer addProduct(Integer productId, Integer quantity, Customer customer) 
			throws ShoppingCartException {
		Integer updatedQuantity = quantity;
		Product product = new Product(productId);
		
		CartItem cartItem = cartRepo.findByCustomerAndProduct(customer, product);//kiểm tra cartItem tương ứng với customer và product đã tồn tại trong db chưa, nếu đã tồn tại thì lấy ra cartItem này 
		
		if (cartItem != null) {//nếu đã tồn tại cartItem trong db thì lấy ra cartItem đó
			updatedQuantity = cartItem.getQuantity() + quantity;//lấy giá trị quantity của cartItem vừa lấy từ db + quantity
			
			if (updatedQuantity > 5) {//nếu quantity > 5 thì báo lỗi maxmimum là 5
				throw new ShoppingCartException("Could not add more " + quantity + " item(s)"
						+ " because there's already " + cartItem.getQuantity() + " item(s) "
						+ "in your shopping cart. Maximum allowed quantity is 5.");
			}
		} else {//nếu chưa có cartItem trong db thì tạo mới
			cartItem = new CartItem();
			cartItem.setCustomer(customer);//gán customer cho cartItem mới tạo
			cartItem.setProduct(product);//gán product cho cartItem mới tạo
		}
		
		cartItem.setQuantity(updatedQuantity);//update lại quantity trong cartItem
		
		cartRepo.save(cartItem);//lưu cartItem xuống db
		
		return updatedQuantity;
	}
	
	public List<CartItem> listCartItems(Customer customer) {
		return cartRepo.findByCustomer(customer);
	}
	
	public float updateQuantity(Integer productId, Integer quantity, Customer customer) {
		cartRepo.updateQuantity(quantity, customer.getId(), productId);//update lại quantity dưới db
		Product product = productRepo.findById(productId).get();
		float subtotal = product.getDiscountPrice() * quantity;//tính giá trị subtotal tương ứng với quantity
		return subtotal;
	}
	
	public void removeProduct(Integer productId, Customer customer) {
		cartRepo.deleteByCustomerAndProduct(customer.getId(), productId);//xóa cartItem trong db
	}
	
	//sau khi create order thì xóa cartItem này trong db
	public void deleteByCustomer(Customer customer) {
		cartRepo.deleteByCustomer(customer.getId());
	}
	
}
