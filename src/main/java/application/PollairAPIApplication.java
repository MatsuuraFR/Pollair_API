package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import service.FilesStorageService;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.CommandLineRunner;
import javax.annotation.Resource;

//@SpringBootApplication(scanBasePackages="controller")
@SpringBootApplication(scanBasePackages={"controller","service"})
//@ComponentScan("service")
public class PollairAPIApplication implements CommandLineRunner {
	
	@Resource
	FilesStorageService storageService;
	
	public static void main(String[] args) {
		SpringApplication.run(PollairAPIApplication.class, args);
	}
	
	
	@Override
	public void run(String... arg) throws Exception {
	  //storageService.deleteAll();
	  //storageService.init();
		try {
			storageService.init();
		}catch(Exception e) {
			System.err.println(e);
		}
	}
}