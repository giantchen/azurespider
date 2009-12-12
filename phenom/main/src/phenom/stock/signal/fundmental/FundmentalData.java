package phenom.stock.signal.fundmental;

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
	
}
