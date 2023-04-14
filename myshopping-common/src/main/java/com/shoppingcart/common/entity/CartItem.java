package com.shoppingcart.common.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.shoppingcart.common.entity.product.Product;

@Entity
@Table(name = "cart_items")
public class CartItem extends IdBasedEntity {
	
	@ManyToOne//nhiều cartItems thuộc về 1 customer
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne//nhiều cartItems thuộc về 1 product
	@JoinColumn(name = "product_id")	
	private Product product;
	
	private int quantity;//số lượng product trong 1 cartItem
	
	@Transient
	private float shippingCost;
	
	public CartItem() {
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "CartItem [id=" + id + ", customer=" + customer.getFullName() + ", product=" + product.getShortName() + ", quantity=" + quantity
				+ "]";
	}

	@Transient
	public float getSubtotal() {
		return product.getDiscountPrice() * quantity;//tính tiền của mỗi cartItem bằng cách lấy discountPrice x quantity
	}

	@Transient
	public float getShippingCost() {
		return shippingCost;
	}

	public void setShippingCost(float shippingCost) {
		this.shippingCost = shippingCost;
	}
	
}
