# Coco client

A Java Spring-Boot client to consume eduGAIN Connectivity Check results.

## Usage

Add dependency in your pom.xml:

```xml
	<dependency>
		<groupId>org.edugain.monitor</groupId>
		<artifactId>connectivity-check-client</artifactId>
		<version>1.0.0</version>
	</dependency>
```

Add configuration in `application.yml`:

```yml
application:
  clients:
    connectivityCheck:
      skipCertificate: false
      basePath: https://technical.edugain.org/eccs/services/json_api.php
      connectTimeout: 5000
      readTimeout: 30000
```

In your code:
  * In you App declaration:
  
```java
@SpringBootApplication
@EnableConfigurationProperties({ ConnectivityCheckClient.class })
public class MyApp {
```

  * And "voilà", just inject the client in your code:
  
```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyApp.class)
public class ConnectivityCheckClientTest extends Assertions {

	@Autowired
	private ConnectivityCheckClient connectivityCheckClient;

	@Test
	public void testGetOneIdp() {

		String idp = "https://idp.renater.fr/idp/shibboleth";

		ConnectivityCheckResult result = connectivityCheckClient.getConnectivityCheckApi()
				.getStatus(Constants.ACTION_ENTITIES, null, null, idp, null, null, null, null, null);

		assertThat(result.getResults().size()).isEqualTo(1);
		assertThat(result.getNumRows()).isEqualTo(1);
		assertThat(result.getResults().get(0).getCurrentResult()).isEqualTo("OK");
	}
}
```
