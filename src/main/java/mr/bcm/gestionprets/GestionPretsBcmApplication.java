package mr.bcm.gestionprets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan // trouve les classes @ConfigurationProperties (ex : AnnuaireProperties)
@EnableAsync      // active @Async : les emails partent en arrière-plan sans ralentir l'application
@EnableScheduling // active @Scheduled : tâches automatiques (rappel quotidien des retards)
public class GestionPretsBcmApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionPretsBcmApplication.class, args);
	}

}
