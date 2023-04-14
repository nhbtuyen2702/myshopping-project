package com.shoppingcart.client.brand;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingcart.common.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

}
