package com.shoppingcart.common.entity;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
//@MappedSuperclass dùng để tách những cột dùng chung cho nhiều entity thành một class riêng biệt -->refactor code 
@MappedSuperclass//nhờ có annotation này mà tất cả các column khai báo trong class này sẽ được ánh xạ vào trong table customers
public class AbstractAddressWithCountry extends AbstractAddress {
	
	@ManyToOne
	@JoinColumn(name = "country_id")
	protected Country country;
	
	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	
	@Override
	public String toString() {
		String address = firstName;
		
		if (lastName != null && !lastName.isEmpty()) address += " " + lastName;
		
		if (!addressLine1.isEmpty()) address += ", " + addressLine1;
		
		if (addressLine2 != null && !addressLine2.isEmpty()) address += ", " + addressLine2;
		
		if (!city.isEmpty()) address += ", " + city;
		
		if (state != null && !state.isEmpty()) address += ", " + state;
		
		address += ", " + country.getName();
		
		if (!postalCode.isEmpty()) address += ". Postal Code: " + postalCode;
		if (!phoneNumber.isEmpty()) address += ". Phone Number: " + phoneNumber;
		
		return address;
	}	
	
}
