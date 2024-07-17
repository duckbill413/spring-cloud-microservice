package wh.duckbill.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.core.env.Environment;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

//    public ConfigServerApplication(Environment environment) {
//        System.out.println(environment.getProperty("======================="));
//        System.out.println(environment.getProperty("======================="));
//        System.out.println(environment.getProperty("user.home"));
//        System.out.println(environment.getProperty("======================="));
//        System.out.println(environment.getProperty("======================="));
//    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
