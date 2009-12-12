package phenom.stock.signal.fundmental;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import phenom.database.ConnectionManager;

public class FundmentalData {
	private String symbol;
	private String dueDate;
	private String announceDate;
	private double netAssetsPerShare;
	private double earningPerShare;
	private double cashPerShare;
	private double capitalReservPerShare;
	private double fixedAssets;
	private double currentAssets;
	private double totalAssets;
	private double longTermDebt;
	private double primeRevenue;
	private double financingExpense;
	private double netProfit;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public String getAnnounceDate() {
		return announceDate;
	}

	public void setAnnounceDate(String announceDate) {
		this.announceDate = announceDate;
	}

	public double getNetAssetsPerShare() {
		return netAssetsPerShare;
	}

	public void setNetAssetsPerShare(double netAssetsPerShare) {
		this.netAssetsPerShare = netAssetsPerShare;
	}

	public double getEarningPerShare() {
		return earningPerShare;
	}

	public void setEarningPerShare(double earningPerShare) {
		this.earningPerShare = earningPerShare;
	}

	public double getCashPerShare() {
		return cashPerShare;
	}

	public void setCashPerShare(double cashPerShare) {
		this.cashPerShare = cashPerShare;
	}

	public double getCapitalReservPerShare() {
		return capitalReservPerShare;
	}

	public void setCapitalReservPerShare(double capitalReservPerShare) {
		this.capitalReservPerShare = capitalReservPerShare;
	}

	public double getFixedAssets() {
		return fixedAssets;
	}

	public void setFixedAssets(double fixedAssets) {
		this.fixedAssets = fixedAssets;
	}

	public double getCurrentAssets() {
		return currentAssets;
	}

	public void setCurrentAssets(double currentAssets) {
		this.currentAssets = currentAssets;
	}

	public double getTotalAssets() {
		return totalAssets;
	}

	public void setTotalAssets(double totalAssets) {
		this.totalAssets = totalAssets;
	}

	public double getLongTermDebt() {
		return longTermDebt;
	}

	public void setLongTermDebt(double longTermDebt) {
		this.longTermDebt = longTermDebt;
	}

	public double getPrimeRevenue() {
		return primeRevenue;
	}

	public void setPrimeRevenue(double primeRevenue) {
		this.primeRevenue = primeRevenue;
	}

	public double getFinancingExpense() {
		return financingExpense;
	}

	public void setFinancingExpense(double financingExpense) {
		this.financingExpense = financingExpense;
	}

	public double getNetProfit() {
		return netProfit;
	}

	public void setNetProfit(double netProfit) {
		this.netProfit = netProfit;
	}

	static public List<FundmentalData> loadFundmentalData(
			final List<String> symbols, final String startDate,
			final String endDate) {
		List<FundmentalData> ret = new ArrayList<FundmentalData>();
		Connection conn = null;
		try {
			conn = ConnectionManager.getConnection();

			for (String symbol : symbols) {
				try {
					String SQL_GET_FINANCE_INDICATOR_BY_SYMBOL = "SELECT * FROM STOCK_FINANCE WHERE symbol = ?";
					PreparedStatement statement = conn
							.prepareStatement(SQL_GET_FINANCE_INDICATOR_BY_SYMBOL);
					statement.setString(1, symbol);
					ResultSet result = statement.executeQuery();
					while (result.next()) {
						FundmentalData data = new FundmentalData();
						data.setAnnounceDate(result.getString("AnnounceDate"));
						data.setCapitalReservPerShare(result
								.getDouble("CapitalReservePerShare"));
						data.setCashPerShare(result.getDouble("CashPerShare"));
						data
								.setCurrentAssets(result
										.getDouble("CurrentAssets"));
						data.setDueDate(result.getString("DueDate"));
						data.setEarningPerShare(result
								.getDouble("EarningPerShare"));
						data.setFinancingExpense(result
								.getDouble("EarningPerShare"));
						data.setFixedAssets(result.getDouble("FixedAssets"));
						data.setLongTermDebt(result.getDouble("FixedAssets"));
						data.setNetAssetsPerShare(result
								.getDouble("NetAssetsPerShare"));
						data.setNetProfit(result.getDouble("NetProfit"));
						data.setPrimeRevenue(result.getDouble("PrimeRevenue"));
						data.setSymbol(result.getString("Symbol"));
						data.setTotalAssets(result.getDouble("TotalAssets"));
						ret.add(data);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return ret;
	}
}
