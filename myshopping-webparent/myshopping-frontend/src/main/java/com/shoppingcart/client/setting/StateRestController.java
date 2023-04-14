package com.shoppingcart.client.setting;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.shoppingcart.common.entity.Country;
import com.shoppingcart.common.entity.State;
import com.shoppingcart.common.entity.StateDTO;

@RestController
public class StateRestController {

	@Autowired private StateRepository repo;
	
	//lấy tất cả states thuộc về country hiện tại
	@GetMapping("/settings/list_states_by_country/{id}")
	public List<StateDTO> listByCountry(@PathVariable("id") Integer countryId) {//lấy tất cả states theo countryId
		List<State> listStates = repo.findByCountryOrderByNameAsc(new Country(countryId));
		List<StateDTO> result = new ArrayList<>();
		
		for (State state : listStates) {
			result.add(new StateDTO(state.getId(), state.getName()));
		}
		
		return result;
	}

}
