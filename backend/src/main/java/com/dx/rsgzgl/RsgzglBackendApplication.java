package com.dx.rsgzgl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({
        "com.dx.rsgzgl.system.mapper",
        "com.dx.rsgzgl.person.mapper",
        "com.dx.rsgzgl.org.mapper",
        "com.dx.rsgzgl.salary.mapper"
})
@SpringBootApplication
public class RsgzglBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RsgzglBackendApplication.class, args);
    }
}
