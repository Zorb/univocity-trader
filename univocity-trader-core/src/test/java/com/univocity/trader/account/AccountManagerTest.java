package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;
import org.junit.*;

import java.math.*;
import java.util.*;
import java.util.function.*;

import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Order.Type.*;
import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.indicators.Signal.*;
import static junit.framework.TestCase.*;

public class AccountManagerTest {

	private static final double CLOSE = 0.4379;

	private AccountManager getAccountManager() {
		return getAccountManager(null);
	}

	private AccountManager getAccountManager(OrderManager orderManager) {
		SimulationConfiguration configuration = new SimulationConfiguration();

		SimulationAccount accountCfg = new SimulationConfiguration().account();
		accountCfg
				.referenceCurrency("USDT")
				.tradeWithPair("ADA", "BNB")
				.tradeWith("ADA", "BNB")
				.enableShorting();

		if (orderManager != null) {
			accountCfg.orderManager(orderManager);
		}


		SimulatedClientAccount clientAccount = new SimulatedClientAccount(accountCfg, configuration.simulation());
		AccountManager account = clientAccount.getAccount();

		TradingManager m = new TradingManager(new SimulatedExchange(account), null, account, "ADA", "USDT", Parameters.NULL);
		Trader trader = new Trader(m, null, new HashSet<>());
		trader.trade(new Candle(1, 2, 0.04371, 0.4380, 0.4369, CLOSE, 100.0), Signal.NEUTRAL, null);

		m = new TradingManager(new SimulatedExchange(account), null, account, "BNB", "USDT", Parameters.NULL);
		trader = new Trader(m, null, new HashSet<>());
		trader.trade(new Candle(1, 2, 50, 50, 50, 50, 100.0), Signal.NEUTRAL, null);

		account.setAmount("BNB", 1);

		return account;
	}

	@Test
	public void testFundAllocationBasics() {
		AccountManager account = getAccountManager();
		AccountConfiguration<?> cfg = account.configuration();

		account.setAmount("USDT", 350);
		cfg.maximumInvestmentAmountPerAsset(20.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(funds, 19.98, 0.001);

		cfg.maximumInvestmentPercentagePerAsset(2.0);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(funds, 7.992, 0.001);

		cfg.maximumInvestmentAmountPerTrade(6);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(funds, 5.994, 0.001);

		cfg.maximumInvestmentPercentagePerTrade(1.0);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(funds, 3.996, 0.001);

		cfg.maximumInvestmentAmountPerTrade(3);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(funds, 2.997, 0.001);


		cfg.minimumInvestmentAmountPerTrade(10);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(funds, 0.0, 0.001);

	}

	@Test
	public void testFundAllocationPercentageWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentPercentagePerAsset(90.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(99.9, funds, 0.001);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(49.95, funds, 0.001);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(9.99, funds, 0.001);

		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerAsset(60.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(59.94, funds, 0.001);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(9.99, funds, 0.001);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationPercentagePerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentPercentagePerTrade(40.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(59.94, funds, 0.001); //total funds = 150: 100 USDT + 1 BNB (worth 50 USDT).

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(59.94, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		;
		assertEquals(19.98, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountPerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerTrade(40.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(39.96, funds, 0.001);

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(39.96, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		;
		assertEquals(19.98, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		;
		assertEquals(0.0, funds, 0.001);
	}

	private double getInvestmentAmount(Trader trader, double totalSpent) {
		final TradingFees fees = trader.tradingFees();
		return fees.takeFee(totalSpent, Order.Type.LIMIT, Order.Side.BUY);
	}

	private double checkTradeAfterLongBuy(double usdBalanceBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		return checkTradeAfterLongBuy(usdBalanceBeforeTrade, trade, totalSpent, previousQuantity, unitPrice, maxUnitPrice, minUnitPrice,
				(account) -> account.getAmount("ADA"));
	}

	private double checkTradeAfterBracketOrder(double usdBalanceBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		return checkTradeAfterLongBuy(usdBalanceBeforeTrade, trade, totalSpent, previousQuantity, unitPrice, maxUnitPrice, minUnitPrice,
				//bracket order locks amount bought to sell it back in two opposing orders.
				//locked balance must be relative to amount bought in parent order, and both orders share the same locked balance.
				(account) -> account.getBalance("ADA").getLocked().doubleValue());

	}

	private double checkTradeAfterLongBuy(double usdBalanceBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice, Function<AccountManager, Double> assetBalance) {
		Trader trader = trade.trader();

		double amountAfterFees = getInvestmentAmount(trader, totalSpent);
		double amountToInvest = getInvestmentAmount(trader, amountAfterFees); //take fees again to ensure there are funds for fees when closing
		double quantityAfterFees = (amountToInvest / unitPrice) * 0.9999; //quantity adjustment to ensure exchange doesn't reject order for mismatching decimals

		double totalQuantity = quantityAfterFees + previousQuantity;

		checkLongTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(totalQuantity, trade.quantity(), 0.01);

		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(totalQuantity, assetBalance.apply(account), 0.001);
		assertEquals(usdBalanceBeforeTrade - amountAfterFees, account.getAmount("USDT"), 0.01);

		return quantityAfterFees;
	}

	private void checkTradeAfterLongSell(double usdBalanceBeforeTrade, Trade trade, double quantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();
		final TradingFees fees = trader.tradingFees();

		double totalToReceive = quantity * unitPrice;

		final double receivedAfterFees = fees.takeFee(totalToReceive, Order.Type.LIMIT, Order.Side.SELL);

		checkLongTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(quantity, trade.quantity(), 0.01);
		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), 0.001);
		assertEquals(usdBalanceBeforeTrade + receivedAfterFees, account.getAmount("USDT"), 0.01);
	}


	private void checkLongTradeStats(Trade trade, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		final double change = ((unitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;
		final double minChange = ((minUnitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;
		final double maxChange = ((maxUnitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;

		assertEquals(maxChange, trade.maxChange(), 0.01);
		assertEquals(minChange, trade.minChange(), 0.01);
		assertEquals(change, trade.priceChangePct(), 0.01);
		assertEquals(maxUnitPrice, trade.maxPrice());
		assertEquals(minUnitPrice, trade.minPrice());
		assertEquals(unitPrice, trade.lastClosingPrice());

	}

	@Test
	public void testLongPositionTrading() {
		AccountManager account = getAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration().maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.getTraderOf("ADAUSDT");

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 10, 0.8, BUY);
		double quantity2 = checkTradeAfterLongBuy(usdBalance, trade, MAX, quantity1, 0.8, 1.1, 0.8);

		double averagePrice = ((quantity1 * 1.0) + (quantity2 * 0.8)) / (quantity1 + quantity2);
		assertEquals(averagePrice, trade.averagePrice(), 0.001);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 20, 0.95, SELL);
		checkTradeAfterLongSell(usdBalance, trade, (quantity1 + quantity2), 0.95, 1.1, 0.8);
		assertEquals(averagePrice, trade.averagePrice(), 0.001); //average price is about 0.889

		assertFalse(trade.stopped());
		assertEquals("Sell signal", trade.exitReason());
		assertFalse(trade.tryingToExit());

		checkProfitLoss(trade, initialBalance, (quantity1 * 1.0) + (quantity2 * 0.8));
	}

	private void checkProfitLoss(Trade trade, double initialBalance, double totalInvested) {
		Trader trader = trade.trader();
		AccountManager account = trader.tradingManager.getAccount();

		double finalBalance = account.getAmount("USDT");
		double profitLoss = finalBalance - initialBalance;
		assertEquals(profitLoss, trade.actualProfitLoss(), 0.001);

		double invested = totalInvested + trader.tradingFees().feesOnAmount(totalInvested, Order.Type.LIMIT, Order.Side.SELL);
		double profitLossPercentage = ((profitLoss / invested)) * 100.0;
		assertEquals(profitLossPercentage, trade.actualProfitLossPct(), 0.001);
	}

	private void tradeOnPrice(Trader trader, long time, double price, Signal signal) {
		tradeOnPrice(trader, time, price, signal, false);
	}

	private Candle newTick(long time, double price) {
		return new Candle(time, time, price, price, price, price, 100.0);
	}

	private double checkTradeAfterShortSell(double usdBalanceBeforeTrade, double usdReservedBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();

		double amountToInvest = getInvestmentAmount(trader, totalSpent);
		double feesPaid = totalSpent - amountToInvest;
		double quantityAfterFees = (amountToInvest / unitPrice);

		double totalQuantity = quantityAfterFees + previousQuantity;

		checkShortTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(totalQuantity, trade.quantity(), 0.01);

		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), 0.001);
		assertEquals(totalQuantity, account.getShortedAmount("ADA"), 0.001);

		double inReserve = account.marginReserveFactorPct() * amountToInvest;
		assertEquals(inReserve + usdReservedBeforeTrade, account.getMarginReserve("USDT", "ADA").doubleValue(), 0.001);

		double movedToReserve = inReserve - amountToInvest;
		double freeBalance = usdBalanceBeforeTrade - (movedToReserve + feesPaid);
		assertEquals(freeBalance, account.getAmount("USDT"), 0.01);

		return quantityAfterFees;
	}

	private void checkShortTradeStats(Trade trade, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		final double change = ((trade.averagePrice() - unitPrice) / trade.averagePrice()) * 100.0;
		final double minChange = ((trade.averagePrice() - maxUnitPrice) / trade.averagePrice()) * 100.0;
		final double maxChange = ((trade.averagePrice() - minUnitPrice) / trade.averagePrice()) * 100.0;

		assertEquals(maxChange, trade.maxChange(), 0.001);
		assertEquals(minChange, trade.minChange(), 0.001);
		assertEquals(change, trade.priceChangePct(), 0.001);
		assertEquals(maxUnitPrice, trade.maxPrice());
		assertEquals(minUnitPrice, trade.minPrice());
		assertEquals(unitPrice, trade.lastClosingPrice());
	}

	private void checkTradeAfterShortBuy(double usdBalanceBeforeTrade, double usdReservedBeforeTrade, Trade trade, double quantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();
		final TradingFees fees = trader.tradingFees();

		checkShortTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(quantity, trade.quantity(), 0.01);
		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), 0.001);

		assertEquals(0.0, account.getBalance("ADA").getFreeAmount(), 0.01);
		assertEquals(0.0, account.getBalance("ADA").getLocked().doubleValue(), 0.01);
		assertEquals(0.0, account.getBalance("ADA").getShortedAmount(), 0.01);
		assertEquals(0.0, account.getMarginReserve("USDT", "ADA").doubleValue(), 0.01);

		double pricePaid = quantity * unitPrice;
		double rebuyCostAfterFees = pricePaid + fees.feesOnAmount(pricePaid, Order.Type.LIMIT, Order.Side.BUY);

		double tradeProfit = usdReservedBeforeTrade - rebuyCostAfterFees;
		double netAccountBalance = usdBalanceBeforeTrade + tradeProfit;

		assertEquals(netAccountBalance, account.getAmount("USDT"), 0.01);
	}

	@Test
	public void testShortPositionTrading() {
		AccountManager account = getAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration()
				.maximumInvestmentAmountPerTrade(MAX)
				.minimumInvestmentAmountPerTrade(10.0);

		Trader trader = account.getTraderOf("ADAUSDT");

		double usdBalance = account.getAmount("USDT");
		double reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();
		tradeOnPrice(trader, 1, 0.9, SELL);
		Trade trade = trader.trades().iterator().next();
		double quantity1 = checkTradeAfterShortSell(usdBalance, reservedBalance, trade, MAX, 0.0, 0.9, 0.9, 0.9);

		tradeOnPrice(trader, 5, 1.0, NEUTRAL);
		checkShortTradeStats(trade, 1.0, 1.0, 0.9);

		usdBalance = account.getAmount("USDT");
		reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();
		tradeOnPrice(trader, 10, 1.2, SELL);
		double quantity2 = checkTradeAfterShortSell(usdBalance, reservedBalance, trade, MAX, quantity1, 1.2, 1.2, 0.9);

		//average price calculated to include fees to exit
		double averagePrice = getInvestmentAmount(trader, ((quantity1 * 0.9) + (quantity2 * 1.2))) / (quantity1 + quantity2);
		assertEquals(averagePrice, trade.averagePrice(), 0.001);

		usdBalance = account.getAmount("USDT");
		reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();

		//CANCEL
		tradeOnPrice(trader, 11, 1.1, SELL, true);
		averagePrice = getInvestmentAmount(trader, ((quantity1 * 0.9) + (quantity2 * 1.2))) / (quantity1 + quantity2);
		assertEquals(averagePrice, trade.averagePrice(), 0.001);
		assertEquals(usdBalance, account.getAmount("USDT"), 0.001);
		assertEquals(reservedBalance, account.getMarginReserve("USDT", "ADA").doubleValue(), 0.001);


		tradeOnPrice(trader, 20, 0.1, BUY);

		checkTradeAfterShortBuy(usdBalance, reservedBalance, trade, quantity1 + quantity2, 0.1, 1.2, 0.1);

		assertFalse(trade.stopped());
		assertEquals("Buy signal", trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(72.062, trade.actualProfitLoss(), 0.001);
		assertEquals(90.258, trade.actualProfitLossPct(), 0.001);
	}

	private void tradeOnPrice(Trader trader, long time, double price, Signal signal, boolean cancel) {
		Candle next = newTick(time, price);
		trader.trade(next, signal, null);
		if (signal != Signal.NEUTRAL) {
			if (cancel) {
				trader.trades().iterator().next().position().forEach(Order::cancel);
			}
			trader.tradingManager.updateOpenOrders(trader.symbol(), next = newTick(time + 1, price));
			trader.trade(next, Signal.NEUTRAL, null);
		}
	}

	@Test
	public void testShortPositionTradingNoMax() {
		AccountManager account = getAccountManager();

		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration()
				.minimumInvestmentAmountPerTrade(10.0);

		Trader trader = account.getTraderOf("ADAUSDT");

		assertEquals(150.0, trader.holdings());

		//FIRST SHORT, COMMITS ALL ACCOUNT BALANCE
		double usdBalance = account.getAmount("USDT");
		double reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();
		tradeOnPrice(trader, 1, 0.9, SELL);
		Trade trade = trader.trades().iterator().next();
		double quantity1 = checkTradeAfterShortSell(usdBalance, reservedBalance, trade, initialBalance, 0.0, 0.9, 0.9, 0.9);

		double amountShorted = quantity1 * 0.9;
		double shortFees = trader.tradingFees().feesOnAmount(amountShorted, Order.Type.LIMIT, Order.Side.SELL);
		assertEquals(150.0 - shortFees, trader.holdings(), 0.001);


		tradeOnPrice(trader, 5, 1.0, NEUTRAL);
		checkShortTradeStats(trade, 1.0, 1.0, 0.9);

		//NO BALANCE AVAILABLE TO SHORT
		usdBalance = account.getAmount("USDT");
		reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();
		tradeOnPrice(trader, 10, 1.2, SELL);
		checkShortTradeStats(trade, 1.2, 1.2, 0.9);
		double updatedAmountShorted = quantity1 * 1.2;
		double shortLoss = updatedAmountShorted - amountShorted;
		assertEquals(150.0 - shortLoss - shortFees, trader.holdings(), 0.001);

		double averagePrice = getInvestmentAmount(trader, ((quantity1 * 0.9))) / (quantity1);
		assertEquals(averagePrice, trade.averagePrice(), 0.001);
		assertEquals(usdBalance, account.getAmount("USDT"), 0.001);
		assertEquals(reservedBalance, account.getMarginReserve("USDT", "ADA").doubleValue(), 0.001);

		//COVER
		tradeOnPrice(trader, 20, 1.0, BUY);
		checkTradeAfterShortBuy(usdBalance, reservedBalance, trade, quantity1, 1.0, 1.2, 0.9);

		assertFalse(trade.stopped());
		assertEquals("Buy signal", trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(-11.31, trade.actualProfitLoss(), 0.001);
		assertEquals(-11.333, trade.actualProfitLossPct(), 0.001);

		//profit/loss includes fees.
		assertEquals(150.0 - 11.31, trader.holdings(), 0.001);

	}

	@Test
	public void testTradingWithStopLoss() {
		AccountManager account = getAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration().maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.getTraderOf("ADAUSDT");

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		usdBalance = account.getAmount("USDT");

		OrderRequest or = new OrderRequest("ADA", "USDT", Order.Side.SELL, LONG, 2, null);
		or.setQuantity(BigDecimal.valueOf(quantity1));
		or.setTriggerCondition(Order.TriggerCondition.STOP_LOSS, new BigDecimal("0.9"));
		Order o = account.executeOrder(or);

		trader.tradingManager.updateOpenOrders("ADAUSDT", newTick(3, 1.5));
		assertEquals(Order.Status.NEW, o.getStatus());
		assertFalse(o.isActive());
		assertEquals(usdBalance, account.getAmount("USDT"), 0.001);

		trader.tradingManager.updateOpenOrders("ADAUSDT", newTick(4, 0.8999));
		assertEquals(Order.Status.NEW, o.getStatus());
		assertTrue(o.isActive());
		assertEquals(usdBalance, account.getAmount("USDT"), 0.001);

		trader.tradingManager.updateOpenOrders("ADAUSDT", newTick(4, 0.92));
		assertEquals(FILLED, o.getStatus());
		assertTrue(o.isActive());
		assertEquals(0.0, account.getAmount("ADA"), 0.001);
		assertEquals(usdBalance + ((o.getExecutedQuantity().doubleValue() /*quantity*/) * 0.92 /*price*/) * 0.999 /*fees*/, account.getAmount("USDT"), 0.001);
	}

	@Test
	public void testTradingWithStopGain() {
		AccountManager account = getAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration().maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.getTraderOf("ADAUSDT");

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		usdBalance = account.getAmount("USDT");
		assertEquals(initialBalance - ((MAX * 0.9999 /*quantity offset*/) * 0.999 /*fees*/), usdBalance, 0.001);
		assertEquals(60.044, usdBalance, 0.001);
		assertEquals(MAX * 0.999 * 0.999 * 0.9999, account.getAmount("ADA"), 0.001);

		OrderRequest or = new OrderRequest("ADA", "USDT", Order.Side.BUY, LONG, 2, null);
		or.setQuantity(BigDecimal.valueOf(quantity1));
		or.setTriggerCondition(Order.TriggerCondition.STOP_GAIN, new BigDecimal("1.2"));
		Order o = account.executeOrder(or);

		trader.tradingManager.updateOpenOrders("ADAUSDT", newTick(3, 0.8999));
		assertEquals(Order.Status.NEW, o.getStatus());
		assertFalse(o.isActive());
		assertEquals(usdBalance - o.getTotalOrderAmount().doubleValue(), account.getAmount("USDT"), 0.001);

		trader.tradingManager.updateOpenOrders("ADAUSDT", newTick(4, 1.5));
		assertTrue(o.isActive());
		assertEquals(Order.Status.NEW, o.getStatus()); //can't fill because price is too high and we want to pay 1.2
		assertEquals(usdBalance - o.getTotalOrderAmount().doubleValue(), account.getAmount("USDT"), 0.001);


		double previousUsdBalance = usdBalance;
		trader.tradingManager.updateOpenOrders("ADAUSDT", newTick(5, 0.8));
		assertTrue(o.isActive());
		assertEquals(FILLED, o.getStatus());

		assertEquals(2 * MAX * 0.999 * 0.999 * 0.9999, account.getAmount("ADA"), 0.001);
		assertEquals(previousUsdBalance - (((MAX * 0.9999 /*quantity offset*/) * 0.8 /*price*/) * 0.999 /*fees*/), account.getAmount("USDT"), 0.001);
	}

	@Test
	public void testTradingWithBracketOrder() {
		AccountManager account = getAccountManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
				if (order.isBuy() && order.isLong() || order.isSell() && order.isShort()) {
					OrderRequest marketSellOnLoss = order.attach(MARKET, -1.0);
					OrderRequest takeProfit = order.attach(MARKET, 1.0);
				}
			}
		});


		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration().maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.getTraderOf("ADAUSDT");

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterBracketOrder(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		usdBalance = account.getAmount("USDT");

		assertEquals(MAX * 0.9999 * 0.999 * 0.999, quantity1); //40 minus offset + 2x fees
		assertEquals(initialBalance - (quantity1 + (quantity1 * 0.001)), usdBalance, 0.0001); //attached orders submitted, so 1x fees again

		Order parent = trade.position().iterator().next();
		assertEquals(2, parent.getAttachments().size());

		Order profitOrder = null;
		Order lossOrder = null;

		for (Order o : parent.getAttachments()) {
			assertEquals(NEW, o.getStatus());
			if(o.getTriggerPrice().doubleValue() > 1.0){
				profitOrder = o;
			} else {
				lossOrder = o;
			}
		}

		assertNotNull(profitOrder);
		assertNotNull(lossOrder);

		assertEquals(parent, profitOrder.getParent());
		assertEquals(parent, lossOrder.getParent());

		trader.tradingManager.updateOpenOrders("ADAUSDT", newTick(3, 0.9));

		assertEquals(0.0, account.getBalance("ADA").getLocked().doubleValue(), 0.00001);
		assertEquals(0.0, account.getBalance("ADA").getFree().doubleValue(), 0.00001);

		assertEquals(usdBalance + (quantity1 * 0.9) * 0.999, account.getAmount("USDT"), 0.00001);


		assertEquals(FILLED, lossOrder.getStatus());
		assertEquals(CANCELLED, profitOrder.getStatus());
	}

}