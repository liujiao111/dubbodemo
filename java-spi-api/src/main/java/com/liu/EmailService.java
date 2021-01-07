package com.liu;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;

@SPI
public interface EmailService {

    @Adaptive
    String sayHello(URL url);
}
