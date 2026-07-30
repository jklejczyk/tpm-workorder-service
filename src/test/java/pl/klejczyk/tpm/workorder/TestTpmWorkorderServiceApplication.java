package pl.klejczyk.tpm.workorder;

import org.springframework.boot.SpringApplication;

public class TestTpmWorkorderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(TpmWorkorderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
