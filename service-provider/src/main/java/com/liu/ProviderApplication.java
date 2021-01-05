package com.liu;

import java.io.IOException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author hgvgh
 * @version 1.0
 * @description
 * @date 2021/1/3
 */
public class ProviderApplication {


  public static void main(String[] args) throws IOException {

    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:dubbo-provider.xml");
    context.start();
    System.in.read();
  }
}
