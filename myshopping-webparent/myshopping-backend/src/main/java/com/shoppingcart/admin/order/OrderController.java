package com.shoppingcart.admin.order;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shoppingcart.admin.security.ShoppingUserDetails;
import com.shoppingcart.admin.setting.SettingService;
import com.shoppingcart.common.entity.Country;
import com.shoppingcart.common.entity.order.Order;
import com.shoppingcart.common.entity.order.OrderDetail;
import com.shoppingcart.common.entity.order.OrderStatus;
import com.shoppingcart.common.entity.order.OrderTrack;
import com.shoppingcart.common.entity.product.Product;
import com.shoppingcart.common.entity.setting.Setting;
import com.shoppingcart.common.exception.OrderNotFoundException;

@Controller
public class OrderController {
	
	//đường dẫn mặc định của module orders
	private String defaultRedirectURL = "redirect:/orders/page/1?sortField=orderTime&sortDir=desc";
	
	@Autowired private OrderService orderService;
	@Autowired private SettingService settingService;

	//khi nhấn vào View All
	@GetMapping("/orders")
	public String listFirstPage() {
		return defaultRedirectURL;
	}
	
	@GetMapping("/orders/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum, Model model,
			@Param("sortField") String sortField, @Param("sortDir") String sortDir, @Param("keyword") String keyword,
			HttpServletRequest request, @AuthenticationPrincipal ShoppingUserDetails loggedUser) {//@AuthenticationPrincipal dùng để lấy ra đối tượng shoppingUserDetails -->chính là đối tượng đã đăng nhập 
		Page<Order> page = orderService.listByPage(pageNum, sortField, sortDir, keyword);
		List<Order> listOrders = page.getContent();

		long startCount = (pageNum - 1) * OrderService.ORDERS_PER_PAGE + 1;
		long endCount = startCount + OrderService.ORDERS_PER_PAGE - 1;

		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}

		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";

		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("listOrders", listOrders);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", reverseSortDir);
		model.addAttribute("keyword", keyword);
		
		loadCurrencySetting(request);//sử dụng trong fragment format_currency(amount)
		
		//khi role là shipper thì trả về trang orders_shipper
		if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Salesperson") && loggedUser.hasRole("Shipper")) {
			return "orders/orders_shipper";
		}

		return "orders/orders";
	}
	
	/*key						value			category
	CURRENCY_ID					1				CURRENCY
	CURRENCY_SYMBOL				$				CURRENCY -->dùng ký tự $ để hiển thị với số tiền
	CURRENCY_SYMBOL_POSITION	Before price	CURRENCY -->dấu $ sẽ hiện trước số tiền
	DECIMAL_DIGITS				2				CURRENCY -->có 2 ký tự trong phần thập phân
	DECIMAL_POINT_TYPE			POINT			CURRENCY -->ngăn cách giữa phần nguyên và phần thập phân là dấu .
	THOUSANDS_POINT_TYPE		COMMA			CURRENCY -->ngăn cách giữa phần hàng nghìn của phần nguyên là dấu ,
	*/
	private void loadCurrencySetting(HttpServletRequest request) {
		List<Setting> currencySettings = settingService.getCurrencySettings();
		
		for (Setting setting : currencySettings) {
			request.setAttribute(setting.getKey(), setting.getValue());//request.setAttribute = model.addAttribute -->html có thể truy cập thông qua ${}
		}	
	}	
	
	//khi nhấn vào view order detail
	@GetMapping("/orders/detail/{id}")
	public String viewOrderDetails(@PathVariable("id") Integer id, Model model, RedirectAttributes ra,
			HttpServletRequest request,	@AuthenticationPrincipal ShoppingUserDetails loggedUser) {
		try {
			Order order = orderService.get(id);
			
			loadCurrencySetting(request);//sử dụng trong fragment format_currency(amount)	
			boolean isVisibleForAdminOrSalesperson = false;
			
			//Admin và Salesperson sẽ có thể xem/chỉnh sửa nhiều thông tin hơn các roles khác
			if (loggedUser.hasRole("Admin") || loggedUser.hasRole("Salesperson")) {
				isVisibleForAdminOrSalesperson = true;
			}
			
			model.addAttribute("isVisibleForAdminOrSalesperson", isVisibleForAdminOrSalesperson);
			
			model.addAttribute("order", order);
			return "orders/order_details_modal";
		} catch (OrderNotFoundException ex) {
			ra.addFlashAttribute("message", ex.getMessage());
			return defaultRedirectURL;
		}
	}
	
	//khi nhấn vào delete order
	@GetMapping("/orders/delete/{id}")
	public String deleteOrder(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		try {
			orderService.delete(id);
			ra.addFlashAttribute("message", "The order ID " + id + " has been deleted.");
		} catch (OrderNotFoundException ex) {
			ra.addFlashAttribute("message", ex.getMessage());
		}
		
		return defaultRedirectURL;
	}
	
	//khi nhấn vào edit order
	@GetMapping("/orders/edit/{id}")
	public String editOrder(@PathVariable("id") Integer id, Model model, RedirectAttributes ra, HttpServletRequest request) {
		try {
			Order order = orderService.get(id);;
			
			List<Country> listCountries = orderService.listAllCountries();
			
			model.addAttribute("pageTitle", "Edit Order (ID: " + id + ")");
			model.addAttribute("order", order);
			model.addAttribute("listCountries", listCountries);
			
			return "orders/order_form";
			
		} catch (OrderNotFoundException ex) {
			ra.addFlashAttribute("message", ex.getMessage());
			return defaultRedirectURL;
		}
	}	
	
	//khi nhấn vào save form
	@PostMapping("/order/save")
	public String saveOrder(Order order, HttpServletRequest request, RedirectAttributes ra) {
		String countryName = request.getParameter("countryName");
		order.setCountry(countryName);
		
		updateProductDetails(order, request);//lấy tất cả orderDetails trong order_form_products để update lại
		updateOrderTracks(order, request);//lấy tất cả orderTracks trong order_form_tracks để update lại
		
		orderService.save(order);//lưu order xuống db	
		
		ra.addFlashAttribute("message", "The order ID " + order.getId() + " has been updated successfully");
		
		return defaultRedirectURL;
	}
	
	private void updateOrderTracks(Order order, HttpServletRequest request) {
		String[] trackIds = request.getParameterValues("trackId");//lấy ra tất cả các thẻ có name = trackId trong order_form_tracks
		String[] trackStatuses = request.getParameterValues("trackStatus");//lấy ra tất cả các thẻ có name = trackStatus trong order_form_tracks
		String[] trackDates = request.getParameterValues("trackDate");
		String[] trackNotes = request.getParameterValues("trackNotes");
		
		List<OrderTrack> orderTracks = order.getOrderTracks();
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		
		for (int i = 0; i < trackIds.length; i++) {
			OrderTrack trackRecord = new OrderTrack();
			
			Integer trackId = Integer.parseInt(trackIds[i]);
			if (trackId > 0) {
				trackRecord.setId(trackId);//nếu tồn tại orderTrack có id = trackId -->update orderTrack này, nếu ko tồn tại -->tạo mới orderTrack
			}
			
			trackRecord.setOrder(order);//gán orderTrack thuộc về order hiện tại
			trackRecord.setStatus(OrderStatus.valueOf(trackStatuses[i]));
			trackRecord.setNotes(trackNotes[i]);
			
			try {
				trackRecord.setUpdatedTime(dateFormatter.parse(trackDates[i]));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			orderTracks.add(trackRecord);//thêm tất cả orderTrack vào list orderTracks của order
		}
	}

	private void updateProductDetails(Order order, HttpServletRequest request) {
		String[] detailIds = request.getParameterValues("detailId");//lấy ra tất cả các thẻ có name = detailId trong order_form_products
		String[] productIds = request.getParameterValues("productId");//lấy ra tất cả các thẻ có name = productId trong order_form_products
		String[] productPrices = request.getParameterValues("productPrice");
		String[] productDetailCosts = request.getParameterValues("productDetailCost");
		String[] quantities = request.getParameterValues("quantity");
		String[] productSubtotals = request.getParameterValues("productSubtotal");
		String[] productShipCosts = request.getParameterValues("productShipCost");
		
		Set<OrderDetail> orderDetails = order.getOrderDetails();
		
		for (int i = 0; i < detailIds.length; i++) {
			System.out.println("Detail ID: " + detailIds[i]);
			System.out.println("\t Prodouct ID: " + productIds[i]);
			System.out.println("\t Cost: " + productDetailCosts[i]);
			System.out.println("\t Quantity: " + quantities[i]);
			System.out.println("\t Subtotal: " + productSubtotals[i]);
			System.out.println("\t Ship cost: " + productShipCosts[i]);
			
			OrderDetail orderDetail = new OrderDetail();
			Integer detailId = Integer.parseInt(detailIds[i]);
			if (detailId > 0) {
				orderDetail.setId(detailId);//nếu tồn tại orderDetail có id = detailId -->update orderDetail này, nếu ko tồn tại -->tạo mới orderDetail
			}
			
			orderDetail.setOrder(order);//gán orderDetail thuộc về order hiện tại
			orderDetail.setProduct(new Product(Integer.parseInt(productIds[i])));//gán product thuộc về order hiện tại
			orderDetail.setProductCost(Float.parseFloat(productDetailCosts[i]));
			orderDetail.setSubtotal(Float.parseFloat(productSubtotals[i]));
			orderDetail.setShippingCost(Float.parseFloat(productShipCosts[i]));
			orderDetail.setQuantity(Integer.parseInt(quantities[i]));
			orderDetail.setUnitPrice(Float.parseFloat(productPrices[i]));
			
			orderDetails.add(orderDetail);//thêm tất cả orderDetail vào list orderDetails của order
			
		}
		
	}
	
}
