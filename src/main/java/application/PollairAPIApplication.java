package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages="controller")
public class PollairAPIApplication {

	public static void main(String[] args) {
		SpringApplication.run(PollairAPIApplication.class, args);
	}
	
}