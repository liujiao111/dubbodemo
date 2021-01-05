package com.liu.service.impl;


import com.liu.service.HelloService;

/**
 * @author hgvgh
 * @version 1.0
 * @description
 * @date 2021/1/3
 */
public class HelloServiceImpl implements HelloService {

  @Override
  public String sayHello(String name) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "hello : " + name;
  }
}
