package com.ysq.theTourGuide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableSwagger2
@MapperScan("com.ysq.theTourGuide.mapper")
public class TheTourGuideApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheTourGuideApplication.class, args);
    }

}
