package phenom.stock.signal.fundmental;

public class EarningPerShare extends AbstractFundmentalSignal {
	@Override
	protected Double getData(FundmentalData data){
		return data.getEarningPerShare();
	}
}
