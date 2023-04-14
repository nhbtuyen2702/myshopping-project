package com.shoppingcart.client.setting;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.shoppingcart.common.entity.Country;
import com.shoppingcart.common.entity.State;

public interface StateRepository extends CrudRepository<State, Integer> {
	
	public List<State> findByCountryOrderByNameAsc(Country country);
	
}
