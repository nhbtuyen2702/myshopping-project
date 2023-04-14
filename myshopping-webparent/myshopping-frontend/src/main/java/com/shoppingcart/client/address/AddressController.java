package com.shoppingcart.client.address;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shoppingcart.client.ControllerHelper;
import com.shoppingcart.common.entity.Address;
import com.shoppingcart.common.entity.Country;
import com.shoppingcart.common.entity.Customer;
import com.shoppingcart.client.customer.CustomerService;

@Controller
public class AddressController {

	@Autowired private AddressService addressService;
	@Autowired private CustomerService customerService;
	@Autowired private ControllerHelper controllerHelper;
	
	//khi nhấn vào menu Address Book
	@GetMapping("/address_book")
	public String showAddressBook(Model model, HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		List<Address> listAddresses = addressService.listAddressBook(customer);//lấy ra tất cả address thuộc về customer hiện tại
		
		/*
		usePrimaryAddressAsDefault là biến để kiểm tra customer có đang gán primary address là address để ship hay ko
		nếu customer tạo thêm address và set address mới này là default thì gán usePrimaryAddressAsDefault = false
		*/
		boolean usePrimaryAddressAsDefault = true;
		for (Address address : listAddresses) {//duyệt các address customer tạo thêm
			if (address.isDefaultForShipping()) {//nếu 1 trong những address này được gán default thì gán usePrimaryAddressAsDefault = false
				usePrimaryAddressAsDefault = false;
				break;
			}
		}
		
		model.addAttribute("listAddresses", listAddresses);
		model.addAttribute("customer", customer);
		model.addAttribute("usePrimaryAddressAsDefault", usePrimaryAddressAsDefault);
		
		return "address_book/addresses";
	}
	
	//nếu nhấn vào Add New Address trong trang addresses
	@GetMapping("/address_book/new")
	public String newAddress(Model model) {
		List<Country> listCountries = customerService.listAllCountries();
		
		model.addAttribute("listCountries", listCountries);
		model.addAttribute("address", new Address());
		model.addAttribute("pageTitle", "Add New Address");
		
		return "address_book/address_form";
	}
	
	//khi nhấn save address ở trang address_form
	@PostMapping("/address_book/save")
	public String saveAddress(Address address, HttpServletRequest request, RedirectAttributes ra) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		
		address.setCustomer(customer);//gán customer hiện tại cho address mới tạo
		addressService.save(address);//save address mới tạo xuống db
		
		String redirectOption = request.getParameter("redirect");//lấy ra param redirect trong queryString
		String redirectURL = "redirect:/address_book";//nếu customer ko từ trang checkout qua thì trả về trang addresses 
		
		if ("checkout".equals(redirectOption)) {//dựa vào param redirect để biết được có đang từ trang checkout qua ko
			redirectURL += "?redirect=checkout";//nếu customer đang ở trang checkout và nhấn qua trang address thì sau khi update hoặc tạo mới address thì sẽ trả về trang checkout
		}
		
		ra.addFlashAttribute("message", "The address has been saved successfully.");
		
		return redirectURL;
	}
	
	//khi nhấn vào edit address
	@GetMapping("/address_book/edit/{id}")
	public String editAddress(@PathVariable("id") Integer addressId, Model model,
			HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		List<Country> listCountries = customerService.listAllCountries();
		
		Address address = addressService.get(addressId, customer.getId());

		model.addAttribute("address", address);
		model.addAttribute("listCountries", listCountries);
		model.addAttribute("pageTitle", "Edit Address (ID: " + addressId + ")");
		
		return "address_book/address_form";
	}
	
	//khi nhấn delete address
	@GetMapping("/address_book/delete/{id}")
	public String deleteAddress(@PathVariable("id") Integer addressId, RedirectAttributes ra,
			HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		addressService.delete(addressId, customer.getId());
		
		ra.addFlashAttribute("message", "The address ID " + addressId + " has been deleted.");
		
		return "redirect:/address_book";
	}
	
	//khi customer tạo address mới và nhấn default cho address mới này 
	@GetMapping("/address_book/default/{id}")
	public String setDefaultAddress(@PathVariable("id") Integer addressId,
			HttpServletRequest request) {
		Customer customer = controllerHelper.getAuthenticatedCustomer(request);
		addressService.setDefaultAddress(addressId, customer.getId());//gán default = true cho address mới và gán default = false cho tất cả address còn lại
		
		String redirectOption = request.getParameter("redirect");//lấy ra param redirect trong queryString
		String redirectURL = "redirect:/address_book";//nếu customer ko từ trang checkout hoặc trang cart qua thì trả về trang addresses 
		
		if ("cart".equals(redirectOption)) {
			redirectURL = "redirect:/cart";//nếu customer đang ở trang cart và nhấn qua trang address thì sau khi update hoặc tạo mới address thì sẽ trả về trang cart
		} else if ("checkout".equals(redirectOption)) {
			redirectURL = "redirect:/checkout";//nếu customer đang ở trang checkout và nhấn qua trang address thì sau khi update hoặc tạo mới address thì sẽ trả về trang checkout
		}
		
		return redirectURL; 
	}
}
