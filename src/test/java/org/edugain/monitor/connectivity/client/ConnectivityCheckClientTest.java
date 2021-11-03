package org.edugain.monitor.connectivity.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.edugain.monitor.connectivity.model.ConnectivityCheckResult;
import org.edugain.monitor.connectivity.model.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfig.class)
@Disabled
public class ConnectivityCheckClientTest extends BaseTest {

	@Autowired
	private ConnectivityCheckClient connectivityCheckClient;

	@BeforeEach
	public void init() {
		this.addUniTestInterceptor(connectivityCheckClient.getRestTemplate());
	}

	@Test
	public void testGetOneIdp() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		String idp = "https://idp.renater.fr/idp/shibboleth";

		ConnectivityCheckResult result = connectivityCheckClient.getConnectivityCheckApi()
				.getStatus(Constants.ACTION_ENTITIES, null, null, idp, null, null, null, null, null);

		assertThat(result.getResults().size()).isEqualTo(1);
		assertThat(result.getNumRows()).isEqualTo(1);
		assertThat(result.getResults().get(0).getCurrentResult()).isEqualTo("OK");
	}

}
