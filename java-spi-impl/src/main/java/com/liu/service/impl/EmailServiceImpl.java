package com.liu.service.impl;

import com.liu.EmailService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.SPI;

@SPI
public class EmailServiceImpl implements EmailService {
    @Override
    public String sayHello(URL url) {
        return "hello dubbo spi!!";
    }
}
