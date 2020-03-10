package org.edugain.monitor.connectivity.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class ResponseHandler implements ResponseErrorHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ResponseHandler.class);

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return (response.getRawStatusCode() >= 400);

	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		LOG.error("Response error: {} {}", response.getStatusCode(), response.getStatusText());
	}

}
