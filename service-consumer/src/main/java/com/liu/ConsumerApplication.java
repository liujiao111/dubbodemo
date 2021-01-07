package com.liu;

import com.liu.service.HelloService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author hgvgh
 * @version 1.0
 * @description
 * @date 2021/1/3
 */
public class ConsumerApplication {

  public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:dubbo-consumer.xml");
    context.start();
    HelloService bean = context.getBean(HelloService.class);
    while (true) {
      for (int i = 0; i < 1000; i++) {
        Thread.sleep(5);
        new Thread(new Runnable() {
          @Override
          public void run() {
            String liujiao = bean.sayHello("liujiao");
            System.out.println(liujiao);
          }
        }).start();
      }
    }
  }

}
