package com.univocity.trader.config;

import java.util.*;
import java.util.function.*;

public abstract class Configuration<T extends AccountConfiguration<T>> extends ConfigurationRoot {

	private static final Configuration instance = new Configuration() {
		@Override
		protected ConfigurationGroup[] getAdditionalConfigurationGroups() {
			return null;
		}

		@Override
		protected AccountConfiguration newAccountConfiguration() {
			return new AccountConfiguration();
		}
	};

	protected static final ConfigurationManager manager = new ConfigurationManager();

	private final DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(this);
	private final EmailConfiguration emailConfiguration = new EmailConfiguration(this);
	private final AccountList<T> accountList = new AccountList<T>(this, () -> newAccountConfiguration());

	private static Supplier<ConfigurationRoot> staticInstanceSupplier = () -> instance;
	private static String defaultConfigurationFile = "univocity-trader.properties";

	protected Configuration() {
		initialize();
	}

	protected void initialize() {
		manager.initialize(staticInstanceSupplier, defaultConfigurationFile);
	}

	protected static void initialize(Supplier<ConfigurationRoot> staticInstanceSupplier, String defaultConfigurationFile) {
		Configuration.staticInstanceSupplier = staticInstanceSupplier;
		Configuration.defaultConfigurationFile = defaultConfigurationFile;
	}

	public static Configuration getInstance() {
		return (Configuration) manager.getInstance();
	}

	public static Configuration configure() {
		return (Configuration) manager.configure();
	}

	public static Configuration load() {
		return (Configuration) manager.load();
	}

	public static Configuration load(String filePath, String... alternativeFilePaths) {
		return (Configuration) manager.load(filePath, alternativeFilePaths);
	}

	public static Configuration loadFromCommandLine(String... args) {
		return (Configuration) manager.loadFromCommandLine(args);
	}

	@Override
	protected void addConfigurationGroup(List<ConfigurationGroup> groups) {
		groups.add(databaseConfiguration);
		groups.add(emailConfiguration);
		groups.add(accountList);

		ConfigurationGroup[] additionalGroups = getAdditionalConfigurationGroups();
		if (additionalGroups != null) {
			Collections.addAll(groups, additionalGroups);
		}
	}

	protected abstract ConfigurationGroup[] getAdditionalConfigurationGroups();

	public DatabaseConfiguration database() {
		return databaseConfiguration;
	}

	public EmailConfiguration email() {
		return emailConfiguration;
	}

	public T account() {
		return accountList.account();
	}

	public T account(String accountId) {
		return accountList.account(accountId);
	}

	protected abstract T newAccountConfiguration();
}