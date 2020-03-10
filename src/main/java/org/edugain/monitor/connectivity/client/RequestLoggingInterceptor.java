package org.edugain.monitor.connectivity.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		ClientHttpResponse response = execution.execute(request, body);
		if (LOG.isDebugEnabled()) {
			String bodyString = new String(body, StandardCharsets.UTF_8);
			byte[] targetArray = new byte[response.getBody().available()];
			response.getBody().read(targetArray);
			String bodyResponse = new String(targetArray);

			LOG.debug("Request submitted with verb: {}, to URI: {} \n with headers: {} \n Request body: {}",
					request.getMethod(), request.getURI(), request.getHeaders(), bodyString);
			LOG.debug("Response with status code: {} \n Response headers: {} \n Response body: {}",
					response.getStatusCode(), response.getHeaders(), bodyResponse);
		}
		return response;
	}
}
