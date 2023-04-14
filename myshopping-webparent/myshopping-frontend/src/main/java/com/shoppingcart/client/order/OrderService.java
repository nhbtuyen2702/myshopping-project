package com.shoppingcart.client.order;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shoppingcart.client.checkout.CheckoutInfo;
import com.shoppingcart.common.entity.Address;
import com.shoppingcart.common.entity.CartItem;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.common.entity.order.Order;
import com.shoppingcart.common.entity.order.OrderDetail;
import com.shoppingcart.common.entity.order.OrderStatus;
import com.shoppingcart.common.entity.order.OrderTrack;
import com.shoppingcart.common.entity.order.PaymentMethod;
import com.shoppingcart.common.entity.product.Product;
import com.shoppingcart.common.exception.OrderNotFoundException;

@Service
public class OrderService {
	
	public static final int ORDERS_PER_PAGE = 5;
	
	@Autowired private OrderRepository repo;
	
	public Order createOrder(Customer customer, Address address, List<CartItem> cartItems,
			PaymentMethod paymentMethod, CheckoutInfo checkoutInfo) {
		Order newOrder = new Order();//tạo mới order, lấy các thông tin trong checkoutInfo để gán vào order
		newOrder.setOrderTime(new Date());
		
		if (paymentMethod.equals(PaymentMethod.PAYPAL)) {
			newOrder.setStatus(OrderStatus.PAID);
		} else {
			newOrder.setStatus(OrderStatus.NEW);
		}
		
		newOrder.setCustomer(customer);//gán customer này thuộc về order hiện tại
		newOrder.setProductCost(checkoutInfo.getProductCost());
		newOrder.setSubtotal(checkoutInfo.getProductTotal());
		newOrder.setShippingCost(checkoutInfo.getShippingCostTotal());
		newOrder.setTax(0.0f);
		newOrder.setTotal(checkoutInfo.getPaymentTotal());
		newOrder.setPaymentMethod(paymentMethod);
		newOrder.setDeliverDays(checkoutInfo.getDeliverDays());
		newOrder.setDeliverDate(checkoutInfo.getDeliverDate());
		
		if (address == null) {
			newOrder.copyAddressFromCustomer();//nếu ko có bất kỳ address nào có default = true -->gán address bằng primary address
		} else {
			newOrder.copyShippingAddress(address);//nếu tồn tại address có default = true -->gán address bằng primary address
		}
		
		Set<OrderDetail> orderDetails = newOrder.getOrderDetails();//lấy ra list orderDetails của order hiện tại, sau đó tạo các orderDetail và thêm vào list này
		
		for (CartItem cartItem : cartItems) {//tạo orderDetail để lưu các thông tin của từng cartItem, 1 cartItem tương ứng với 1 orderDetail
			Product product = cartItem.getProduct();
			
			OrderDetail orderDetail = new OrderDetail();
			orderDetail.setOrder(newOrder);//gán orderDetail này thuộc về order hiện tại
			orderDetail.setProduct(product);//gán product này thuộc về order hiện tại
			orderDetail.setQuantity(cartItem.getQuantity());
			orderDetail.setUnitPrice(product.getDiscountPrice());//price sau khi đã giảm
			orderDetail.setProductCost(product.getCost() * cartItem.getQuantity());//cost x số lượng
			orderDetail.setSubtotal(cartItem.getSubtotal());//price sau khi đã giảm x số lượng
			orderDetail.setShippingCost(cartItem.getShippingCost());
			
			orderDetails.add(orderDetail);
		}
		
		OrderTrack track = new OrderTrack();//tạo orderTrack để lưu status của order
		track.setOrder(newOrder);//gán orderTrack này thuộc về order hiện tại
		track.setStatus(OrderStatus.NEW);
		track.setNotes(OrderStatus.NEW.defaultDescription());//gán description của status NEW
		track.setUpdatedTime(new Date());
		
		newOrder.getOrderTracks().add(track);//lấy ra list orderTracks của order hiện tại, sau đó tạo đối tượng orderTrack và thêm vào list này
		
		return repo.save(newOrder);//lưu order xuống db -->orderDetails và orderTracks cũng được lưu xuống db
	}
	
	public Page<Order> listForCustomerByPage(Customer customer, int pageNum, 
			String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable = PageRequest.of(pageNum - 1, ORDERS_PER_PAGE, sort);
		
		if (keyword != null) {
			return repo.findAll(keyword, customer.getId(), pageable);
		}
		
		return repo.findAll(customer.getId(), pageable);
	}
	
	public Order getOrder(Integer id, Customer customer) {
		return repo.findByIdAndCustomer(id, customer);
	}	
	
	public void setOrderReturnRequested(OrderReturnRequest request, Customer customer) 
			throws OrderNotFoundException {
		Order order = repo.findByIdAndCustomer(request.getOrderId(), customer);
		if (order == null) {
			throw new OrderNotFoundException("Order ID " + request.getOrderId() + " not found");
		}
		
		if (order.isReturnRequested()) return;
		
		OrderTrack track = new OrderTrack();
		track.setOrder(order);
		track.setUpdatedTime(new Date());
		track.setStatus(OrderStatus.RETURN_REQUESTED);
		
		String notes = "Reason: " + request.getReason();
		if (!"".equals(request.getNote())) {
			notes += ". " + request.getNote();
		}
		
		track.setNotes(notes);
		
		order.getOrderTracks().add(track);//lấy ra list orderTracks của order hiện tại, sau đó tạo đối tượng orderTrack và thêm vào list này
		order.setStatus(OrderStatus.RETURN_REQUESTED);//thay đổi status của order này thành RETURN_REQUESTED
		
		repo.save(order);
	}
	
}
