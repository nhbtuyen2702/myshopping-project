package com.shoppingcart.client.checkout;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shoppingcart.common.entity.CartItem;
import com.shoppingcart.common.entity.ShippingRate;
import com.shoppingcart.common.entity.product.Product;

@Service
public class CheckoutService {
	
	private static final int DIM_DIVISOR = 139;

	public CheckoutInfo prepareCheckout(List<CartItem> cartItems, ShippingRate shippingRate) {//tính toán tất cả chi phí
		CheckoutInfo checkoutInfo = new CheckoutInfo();//checkoutInfo là đối tượng dùng để lưu thông tin của order
		
		float productCost = calculateProductCost(cartItems);
		float productTotal = calculateProductTotal(cartItems);
		float shippingCostTotal = calculateShippingCost(cartItems, shippingRate);
		float paymentTotal = productTotal + shippingCostTotal;
		
		checkoutInfo.setProductCost(productCost);
		checkoutInfo.setProductTotal(productTotal);
		checkoutInfo.setShippingCostTotal(shippingCostTotal);
		checkoutInfo.setPaymentTotal(paymentTotal);
		
		checkoutInfo.setDeliverDays(shippingRate.getDays());
		checkoutInfo.setCodSupported(shippingRate.isCodSupported());
		
		return checkoutInfo;
	}

	private float calculateShippingCost(List<CartItem> cartItems, ShippingRate shippingRate) {
		float shippingCostTotal = 0.0f;
		
		for (CartItem item : cartItems) {
			Product product = item.getProduct();
			float dimWeight = (product.getLength() * product.getWidth() * product.getHeight()) / DIM_DIVISOR;
			float finalWeight = product.getWeight() > dimWeight ? product.getWeight() : dimWeight;
			float shippingCost = finalWeight * item.getQuantity() * shippingRate.getRate();
			
			item.setShippingCost(shippingCost);
			
			shippingCostTotal += shippingCost;//tổng tiền ship dựa trên chiều dài, chiều rộng, chiều cao
		}
		
		return shippingCostTotal;
	}

	private float calculateProductTotal(List<CartItem> cartItems) {
		float total = 0.0f;//total là giá khi bán product
		
		for (CartItem item : cartItems) {
			total += item.getSubtotal();//tổng = (price của mỗi product sau khi đã giảm giá) x số lượng mỗi product
		}
		
		return total;
	}

	private float calculateProductCost(List<CartItem> cartItems) {
		float cost = 0.0f;//cost là giá khi nhập product
		
		for (CartItem item : cartItems) {
			cost += item.getQuantity() * item.getProduct().getCost();//tổng = (cost của mỗi product) x số lượng mỗi product
		}
		
		return cost;
	}
}
