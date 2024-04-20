package io.github.zhangwei1989.zwregistry;

import io.github.zhangwei1989.zwregistry.config.ZwregistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ZwregistryConfigProperties.class)
public class ZwregistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZwregistryApplication.class, args);
    }

}
