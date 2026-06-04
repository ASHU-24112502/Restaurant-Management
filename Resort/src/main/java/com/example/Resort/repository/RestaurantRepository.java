package com.example.Resort.repository;

import com.example.Resort.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<RestaurantOrder, Long> {

}