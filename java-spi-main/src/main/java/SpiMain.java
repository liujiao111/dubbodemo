import com.liu.DemoService;
import com.liu.EmailService;

import java.util.ServiceLoader;

public class SpiMain {

    public static void main(String[] args) {
        ServiceLoader<DemoService> demoServices = ServiceLoader.load(DemoService.class);
        for (DemoService demoService : demoServices) {
            System.out.println(demoService.sayHello());
        }
    }
}
