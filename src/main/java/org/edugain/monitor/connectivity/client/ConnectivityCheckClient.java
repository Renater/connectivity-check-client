package org.edugain.monitor.connectivity.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.edugain.monitor.connectivity.ApiClient;
import org.edugain.monitor.connectivity.api.ConnectivityCheckApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Configuration
@ConfigurationProperties(prefix = "application.clients.connectivity-check")
public class ConnectivityCheckClient {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectivityCheckClient.class);

	private boolean skipCertificate;
	private String basePath;
	private Integer connectTimeout;
	private Integer readTimeout;

	private ApiClient apiClient;

	private ConnectivityCheckApi connectivityCheckApi;

	private RestTemplate restTemplate;

	@PostConstruct
	public void init() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		HttpComponentsClientHttpRequestFactory reqFactory;
		HttpClientBuilder hcb = HttpClientBuilder.create().useSystemProperties();

		boolean skipCertValid = this.isSkipCertificate();
		if (skipCertValid) {
			// skip only
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			hcb.setSSLSocketFactory(csf);
		}
		LOG.info("HttpClientBuilder : {}", hcb);
		reqFactory = new HttpComponentsClientHttpRequestFactory(hcb.build());
		reqFactory.setConnectTimeout(getConnectTimeout());
		reqFactory.setReadTimeout(getReadTimeout());

		RestTemplateBuilder builder = new RestTemplateBuilder();

		restTemplate = builder.errorHandler(new ResponseHandler())
				.interceptors(Collections.singletonList(new RequestLoggingInterceptor()))
				// Buffered Client in order to use interceptor logger
				.requestFactory(() -> new BufferingClientHttpRequestFactory(reqFactory)).build();

		for (HttpMessageConverter converter : restTemplate.getMessageConverters()) {

			if (converter instanceof AbstractJackson2HttpMessageConverter) {
				// Workaround text/html media type
				List<MediaType> supportedMediaTypes = new ArrayList<>();
				supportedMediaTypes.addAll(((AbstractJackson2HttpMessageConverter) converter).getSupportedMediaTypes());
				supportedMediaTypes.add(MediaType.TEXT_HTML);
				((AbstractJackson2HttpMessageConverter) converter).setSupportedMediaTypes(supportedMediaTypes);
			}

//			if (converter instanceof AbstractJackson2HttpMessageConverter) {
//				ObjectMapper mapper = ((AbstractJackson2HttpMessageConverter) converter).getObjectMapper();
//				ThreeTenModule module = new ThreeTenModule();
//				module.addDeserializer(Instant.class, CustomInstantDeserializer.INSTANT);
//				module.addDeserializer(OffsetDateTime.class, CustomInstantDeserializer.OFFSET_DATE_TIME);
//				module.addDeserializer(ZonedDateTime.class, CustomInstantDeserializer.ZONED_DATE_TIME);
//				mapper.registerModule(module);
//			}
		}

		apiClient = new ApiClient(restTemplate);
		apiClient.setBasePath(basePath);

		connectivityCheckApi = new ConnectivityCheckApi(apiClient);

	}

	public boolean isSkipCertificate() {
		return skipCertificate;
	}

	public void setSkipCertificate(boolean skipCertificate) {
		this.skipCertificate = skipCertificate;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public Integer getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Integer getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(Integer readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void setApiClient(ApiClient apiClient) {
		this.apiClient = apiClient;
	}

	/**
	 * Just for UnitTests
	 *
	 * @return
	 */
	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public ConnectivityCheckApi getConnectivityCheckApi() {
		return connectivityCheckApi;
	}

}
