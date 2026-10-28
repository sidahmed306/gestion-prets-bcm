package mr.bcm.gestionprets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Port LDAP aléatoire : ne gêne pas le backend lancé à côté (qui utilise 8389)
@SpringBootTest(properties = "spring.ldap.embedded.port=0")
class GestionPretsBcmApplicationTests {

	@Test
	void contextLoads() {
	}

}
