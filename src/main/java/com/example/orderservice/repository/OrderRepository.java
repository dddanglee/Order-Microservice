package com.example.orderservice.repository;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderEntity,Long> {
    Iterable<OrderEntity> findAll();
    Iterable<OrderEntity> findByUserId(String userId);
    OrderEntity findByOrderId(String orderId);

}
