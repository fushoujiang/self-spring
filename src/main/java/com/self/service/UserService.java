package com.self.service;


import com.spring.Autowired;
import com.spring.Component;
import com.spring.Scope;

@Component
@Scope("singleton")
public class UserService {

    @Autowired
     OrderService orderService;


}
