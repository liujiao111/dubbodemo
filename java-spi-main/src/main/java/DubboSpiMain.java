import com.liu.EmailService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;

import java.util.List;

public class DubboSpiMain {

    public static void main(String[] args) {
        URL url = URL.valueOf("test://localhost/email?email.service=email2");
        EmailService activateExtension = ExtensionLoader.getExtensionLoader(EmailService.class).getAdaptiveExtension();
        System.out.println(activateExtension.sayHello(url));
    }
}
