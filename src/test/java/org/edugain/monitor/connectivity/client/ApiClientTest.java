package org.edugain.monitor.connectivity.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.edugain.monitor.connectivity.ApiClient;
import org.edugain.monitor.connectivity.api.ConnectivityCheckApi;
import org.edugain.monitor.connectivity.model.ConnectivityCheckResult;
import org.edugain.monitor.connectivity.model.Constants;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZonedDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.threetenbp.ThreeTenModule;

/**
 * Simple tests
 *
 * @author arnoud
 *
 */
public class ApiClientTest extends BaseTest {

	@Test
	public void testGetOneIdp() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		ApiClient apiClient = new ApiClient(getRestTemplate());
		ConnectivityCheckApi api = new ConnectivityCheckApi(
				apiClient.setBasePath("https://technical.edugain.org/eccs/services/json_api.php"));

		String idp = "https://idp.renater.fr/idp/shibboleth";

		ConnectivityCheckResult result = api.getStatus(Constants.ACTION_ENTITIES, null, null, idp, null, null, null,
				null, null);

		assertThat(result.getResults().size()).isEqualTo(1);
		assertThat(result.getNumRows()).isEqualTo(1);
		assertThat(result.getResults().get(0).getCurrentResult()).isEqualTo("OK");
	}

	private RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		RestTemplateBuilder builder = new RestTemplateBuilder();
		HttpComponentsClientHttpRequestFactory reqFactory;
		HttpClientBuilder hcb = HttpClientBuilder.create().useSystemProperties();

		SSLContext sslContext;

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		hcb.setSSLSocketFactory(csf);

		reqFactory = new HttpComponentsClientHttpRequestFactory(hcb.build());
		reqFactory.setConnectTimeout(5000);
		reqFactory.setReadTimeout(30000);

		// Buffered Client in order to use interceptor logger

		RestTemplate rt = builder.requestFactory(() -> new BufferingClientHttpRequestFactory(reqFactory)).build();

		for (HttpMessageConverter<?> converter : rt.getMessageConverters()) {
			// If converters need some tweaks...
			if (converter instanceof AbstractJackson2HttpMessageConverter) {
				// Workaround text/html media type
				List<MediaType> supportedMediaTypes = new ArrayList<>();
				supportedMediaTypes.addAll(((AbstractJackson2HttpMessageConverter) converter).getSupportedMediaTypes());
				supportedMediaTypes.add(MediaType.TEXT_HTML);
				((AbstractJackson2HttpMessageConverter) converter).setSupportedMediaTypes(supportedMediaTypes);
			}

			if (converter instanceof AbstractJackson2HttpMessageConverter) {
				ObjectMapper mapper = ((AbstractJackson2HttpMessageConverter) converter).getObjectMapper();
				ThreeTenModule module = new ThreeTenModule();
				module.addDeserializer(Instant.class, ConnectivityCheckCustomInstantDeserializer.INSTANT);
				module.addDeserializer(OffsetDateTime.class,
						ConnectivityCheckCustomInstantDeserializer.OFFSET_DATE_TIME);
				module.addDeserializer(ZonedDateTime.class, ConnectivityCheckCustomInstantDeserializer.ZONED_DATE_TIME);
				mapper.registerModule(module);
			}
		}

		this.addUniTestInterceptor(rt);

		return rt;

	}
}
