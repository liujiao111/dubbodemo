package com.liu;

import com.liu.service.HelloService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author hgvgh
 * @version 1.0
 * @description
 * @date 2021/1/3
 */
public class ConsumerApplication {

  public static void main(String[] args) throws IOException {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:dubbo-consumer.xml");
    context.start();
    HelloService bean = context.getBean(HelloService.class);
    String liujiao = bean.sayHello("liujiao");
    System.out.println(liujiao);
    System.in.read();
  }

}
