package yuxuan.travelassisant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("yuxuan.travelassisant.mapper")
@SpringBootApplication
public class TravelAssisantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelAssisantApplication.class, args);
    }

}
