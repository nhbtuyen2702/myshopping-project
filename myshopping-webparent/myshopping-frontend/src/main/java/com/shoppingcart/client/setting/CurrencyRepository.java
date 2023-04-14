package com.shoppingcart.client.setting;

import org.springframework.data.repository.CrudRepository;

import com.shoppingcart.common.entity.Currency;

public interface CurrencyRepository extends CrudRepository<Currency, Integer> {

}
