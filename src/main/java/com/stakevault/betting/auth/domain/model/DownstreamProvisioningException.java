package com.stakevault.betting.auth.domain.model;

public class DownstreamProvisioningException extends RuntimeException {

	private final String serviceName;

	public DownstreamProvisioningException(String serviceName, Throwable cause) {
		super(serviceName + ": " + cause.getMessage(), cause);
		this.serviceName = serviceName;
	}

	public String serviceName() {
		return serviceName;
	}
}
