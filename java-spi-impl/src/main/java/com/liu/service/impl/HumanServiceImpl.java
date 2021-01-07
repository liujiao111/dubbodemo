package com.liu.service.impl;

import com.liu.DemoService;

public class HumanServiceImpl implements DemoService {
    @Override
    public String sayHello() {
        return "Hello Human Spi!!";
    }
}
