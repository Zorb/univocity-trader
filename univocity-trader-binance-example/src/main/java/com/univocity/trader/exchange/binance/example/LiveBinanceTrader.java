package com.univocity.trader.exchange.binance.example;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.binance.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;

import java.math.*;
import java.time.*;

public class LiveBinanceTrader {

	private static final EmailConfiguration getEmailConfig() {
		return Configuration.configure().email()
				.replyToAddress("dev@univocity.com")
				.smtpHost("smtp.gmail.com")
				.smtpSSL(true)
				.smtpPort(587)
				.smtpUsername("<YOU>@gmail.com")
				.smtpPassword("<YOUR SMTP PASSWORD>")
				.smtpSender("<YOU>>@gmail.com");
	}

	public static void main(String... args) {

//		TODO: configure your database connection as needed. The following options are available:

//		(a) Load configuration file
//		Configuration.load();                                //tries to open a univocity-trader.properties file
//		Configuration.loadFromCommandLine(args);		      //opens a file provided via the command line
//		Configuration.load("/path/to/config", "other.file"); //tries to find specific configuration files

//		(b) Configuration code
//		Configuration.configure().database()
//				.jdbcDriver("my.database.DriverClass")
//				.jdbcUrl("jdbc:mydb://localhost:5555/database")
//				.user("admin")
//				.password("qwerty");

//		(c) Use your own DataSource implementation:
//		DataSource ds = ?
//		CandleRepository.setDataSource(ds);

		BinanceTrader binance = new BinanceTrader(TimeInterval.minutes(1));

		BinanceClientConfiguration clientConfig = Binance.configure().client()
				.email("<YOUR E-MAIL")
				.timeZone("system")
				.referenceCurrency("USDT")
				.apiKey("<YOUR BINANCE API KEY>")
				.secret("<YOUR BINANCE API SECRET>");

		Client client = binance.addClient(clientConfig);
		client.tradeWith("BTC", "ETH", "XRP", "ADA");

		client.strategies().add(ExampleStrategy::new);
		client.monitors().add(ExampleStrategyMonitor::new);

		client.account().maximumInvestmentAmountPerAsset(20);
		client.account().setOrderManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
				switch (order.getSide()) {
					case BUY:
						order.setPrice(order.getPrice().multiply(new BigDecimal("0.9"))); //10% less
						break;
					case SELL:
						order.setPrice(order.getPrice().multiply(new BigDecimal("1.1"))); //10% more
				}
			}
		});

		client.listeners().add(new OrderExecutionToLog());
		binance.run();

	}

}
