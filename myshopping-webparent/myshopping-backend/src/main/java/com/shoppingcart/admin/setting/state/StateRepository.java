package com.shoppingcart.admin.setting.state;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.shoppingcart.common.entity.Country;
import com.shoppingcart.common.entity.State;

public interface StateRepository extends CrudRepository<State, Integer> {
	
	public List<State> findByCountryOrderByNameAsc(Country country);
	
}
