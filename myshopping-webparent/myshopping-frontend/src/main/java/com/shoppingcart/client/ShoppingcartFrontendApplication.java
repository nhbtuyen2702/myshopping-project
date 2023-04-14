package com.shoppingcart.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({"com.shoppingcart.client.*", "com.shoppingcart.client" })
@EnableJpaRepositories(basePackages = { "com.shoppingcart.client.*" })
@EntityScan({ "com.shoppingcart.common.*" })
public class ShoppingcartFrontendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppingcartFrontendApplication.class, args);
	}

}
