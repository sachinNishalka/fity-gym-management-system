package pro.sachin.fity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FityApplication {
	public static void main(String[] args) {
		SpringApplication.run(FityApplication.class, args);
	}
}
