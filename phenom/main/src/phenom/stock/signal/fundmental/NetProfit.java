package phenom.stock.signal.fundmental;


public class NetProfit extends AbstractFundmentalSignal {
	@Override
	protected Double getData(FundmentalData data){
		return data.getNetProfit();
	}
}
