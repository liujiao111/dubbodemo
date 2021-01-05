package com.liu.service;

public class HelloServiceMock implements HelloService{

    @Override
    public String sayHello(String name) {
        return "不好意思，请求超时啦";
    }
}
