package phenom.stock.signal.fundmental;


public class NetAssetsPerShare extends AbstractFundmentalSignal {
	@Override
	protected Double getData(FundmentalData data){
		return data.getNetAssetsPerShare();
	}
}
