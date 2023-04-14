package com.shoppingcart.admin.order;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.shoppingcart.common.entity.order.OrderDetail;

public interface OrderDetailRepository extends CrudRepository<OrderDetail, Integer> {
	
	/*
	 SELECT NEW com.shoppingcart.common.entity.order.OrderDetail(d.product.category.name, d.quantity,
	 d.productCost, d.shippingCost, d.subtotal) -->chạy vào constructor có 5 tham số của OrderDetail, mỗi tham số trong Constructor phải có kiểu dữ kiệu giống với kiểu dữ liệu của các giá trị trong câu query 
	 -->nếu ko có Constructor có 5 tham số, cùng kiểu dữ liệu -->báo lỗi 
	 -->dùng để tạo đối tượng orderDetail
	 */
	@Query("SELECT NEW com.shoppingcart.common.entity.order.OrderDetail(d.product.category.name, d.quantity,"
			+ " d.productCost, d.shippingCost, d.subtotal)"
			+ " FROM OrderDetail d WHERE d.order.orderTime BETWEEN ?1 AND ?2")
	public List<OrderDetail> findWithCategoryAndTimeBetween(Date startTime, Date endTime);
	
	@Query("SELECT NEW com.shoppingcart.common.entity.order.OrderDetail(d.quantity, d.product.name,"
			+ " d.productCost, d.shippingCost, d.subtotal)"
			+ " FROM OrderDetail d WHERE d.order.orderTime BETWEEN ?1 AND ?2")
	public List<OrderDetail> findWithProductAndTimeBetween(Date startTime, Date endTime);	
	
}
