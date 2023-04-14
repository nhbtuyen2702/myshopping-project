package com.shoppingcart.client.shipping;

import org.springframework.data.repository.CrudRepository;

import com.shoppingcart.common.entity.Country;
import com.shoppingcart.common.entity.ShippingRate;

public interface ShippingRateRepository extends CrudRepository<ShippingRate, Integer> {
	
	public ShippingRate findByCountryAndState(Country country, String state);
}
