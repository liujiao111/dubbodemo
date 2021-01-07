package com.liu.service.impl;

import com.liu.DemoService;

public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello() {
        return "Hello Java Spi!!";
    }
}
