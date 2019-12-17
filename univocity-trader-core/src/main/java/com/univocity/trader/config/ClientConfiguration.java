package com.univocity.trader.config;

import org.apache.commons.lang3.*;

import java.util.*;

public class ClientConfiguration<T extends ClientConfiguration<T>> {

	private static final Set<String> supportedTimeZones = new TreeSet<>(List.of(TimeZone.getAvailableIDs()));
	private static final String supportedTimezoneDescription;

	static {
		StringBuilder tmp = new StringBuilder(3000);
		tmp.append(" Supported timezones:");
		for (String tz : supportedTimeZones) {
			tmp.append('\n').append(tz);
		}
		supportedTimezoneDescription = tmp.toString();
	}

	private String email;
	private String referenceCurrency;
	private TimeZone timeZone;

	protected ClientConfiguration() {
	}

	protected final void readProperties(PropertyBasedConfiguration properties) {

	}

	protected final void readProperties(String clientId, PropertyBasedConfiguration properties) {
		if (!clientId.isBlank()) {
			clientId = clientId + ".";
		}
		email = properties.getProperty(clientId + "email");
		referenceCurrency = properties.getProperty(clientId + "reference.currency");

		String tz = properties.getProperty(clientId + "timezone");
		timeZone = getTimeZone(tz);
		if (timeZone == null) {
			String msg = "Unsupported timezone '" + tz + "' set ";
			if (clientId.isBlank()) {
				msg += "in 'timezone' property.";
			} else {
				msg += "for '" + clientId + "timezone' property.";
			}
			throw new IllegalConfigurationException(msg + supportedTimezoneDescription);
		}
		readClientProperties(clientId, properties);
	}

	protected void readClientProperties(String clientId, PropertyBasedConfiguration properties){

	}

	public boolean isConfigured() {
		return StringUtils.isNoneBlank(referenceCurrency);
	}

	public String email() {
		return email;
	}

	public T email(String email) {
		this.email = email;
		return (T)this;
	}

	public String referenceCurrency() {
		return referenceCurrency;
	}

	public T referenceCurrency(String referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
		return (T)this;
	}

	public TimeZone timeZone() {
		return timeZone;
	}

	public T timeZone(String timeZone) {
		this.timeZone = getTimeZone(timeZone);
		if (this.timeZone == null) {
			throw new IllegalArgumentException("Unsupported time zone: '" + timeZone + "." + supportedTimezoneDescription);
		}
		return (T)this;
	}

	private TimeZone getTimeZone(String tz) {
		if (tz == null || tz.equals("system")) {
			timeZone = TimeZone.getDefault();
		} else if (supportedTimeZones.contains(tz)) {
			timeZone = TimeZone.getTimeZone(tz);
		}
		return null;
	}

	public T timeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		return (T)this;
	}
}