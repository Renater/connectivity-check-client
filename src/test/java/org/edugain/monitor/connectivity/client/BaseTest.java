package org.edugain.monitor.connectivity.client;

import static org.junit.Assume.assumeFalse;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

public class BaseTest extends Assertions {

	/**
	 * Adds an interceptor for UnitTests to skip tests when API is not available
	 * 
	 * @param restTemplate
	 */
	public void addUniTestInterceptor(RestTemplate restTemplate) {
		List<ClientHttpRequestInterceptor> currentInterceptors = restTemplate.getInterceptors();

		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				ClientHttpResponse response = execution.execute(request, body);
				assumeFalse(response.getStatusCode().is5xxServerError());
				return response;
			}
		};

		currentInterceptors.add(interceptor);
		restTemplate.setInterceptors(currentInterceptors);

	}
}
