package phenom.stock.signal.fundmental;


public class ReturnOnEquity extends AbstractFundmentalSignal {
	@Override
	protected Double getData(FundmentalData data){
		return data.getEarningPerShare() / data.getNetAssetsPerShare();
	}
}
