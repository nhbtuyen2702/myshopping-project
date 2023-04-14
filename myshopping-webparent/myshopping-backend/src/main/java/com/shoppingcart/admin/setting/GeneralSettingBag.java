package com.shoppingcart.admin.setting;

import java.util.List;

import com.shoppingcart.common.entity.setting.Setting;
import com.shoppingcart.common.entity.setting.SettingBag;

public class GeneralSettingBag extends SettingBag {

	public GeneralSettingBag(List<Setting> listSettings) {
		super(listSettings);
	}

	//CURRENCY_SYMBOL	$	CURRENCY
	public void updateCurrencySymbol(String value) {
		super.update("CURRENCY_SYMBOL", value);
	}
	
	//SITE_LOGO	shopping-logo.png	GENERAL
	public void updateSiteLogo(String value) {
		super.update("SITE_LOGO", value);
	}
	
}
