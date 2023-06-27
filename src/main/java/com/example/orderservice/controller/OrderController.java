package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.repository.OrderEntity;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/order-service")
public class OrderController {

    Environment env;
    OrderService orderService;
    KafkaProducer kafkaProducer;
    OrderProducer orderProducer;
    @Autowired
    public OrderController(Environment env, OrderService orderService, KafkaProducer kafkaProducer
            ,OrderProducer orderProducer) {
        this.env = env;
        this.orderService = orderService;
        this.kafkaProducer = kafkaProducer;
        this.orderProducer = orderProducer;
    }

    @GetMapping("/health_check")
    public String Status(){
        return "it's working. on Port "+env.getProperty("local.server.port");
    }


    @PostMapping("{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrders(@RequestBody RequestOrder orderDetails
                                                    , @PathVariable("userId") String userId){

        log.info("Before add orders data");
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        /*jpa-->kafka*/


/*
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());

        ResponseOrder rtnValue = mapper.map(orderDto,ResponseOrder.class);

        */
/* send this order to the kafka*//*

        kafkaProducer.send("example-catalog-topic",orderDto);
        orderProducer.send("orders",orderDto);
*/

        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder rtnValue = mapper.map(createdOrder,ResponseOrder.class);

        kafkaProducer.send("example-catalog-topic",orderDto);
        log.info("After added orders data");



        return ResponseEntity.status(HttpStatus.CREATED).body(rtnValue);
    }


    @GetMapping("{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) throws Exception{
        log.info("Before retrieve orders data");
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> rtnvalue = new ArrayList<>();
        orderList.forEach(v->{
            ResponseOrder tmp = mapper.map(v,ResponseOrder.class);
            rtnvalue.add(tmp);
        });

        log.info("Add retrieve orders data");
        return ResponseEntity.status(HttpStatus.OK).body(rtnvalue);
    }


}
