package phenom.stock;

import java.util.Map;
import java.util.List;

import junit.framework.Assert;
import org.junit.Test;

public class IndexTest {
	Map<String, List<String>> mapping = null;

	public IndexTest() {
		mapping = Index.getIndexStockMapping();
	}

	@Test
	public void testGetIndexStockMapping() {
		Assert.assertEquals(14, mapping.size());
	}

	@Test
	public void testStocks() {
		for (String index : mapping.keySet()) {
			List<String> ls = mapping.get(index);
			//System.out.println(ls.size());
			if (ls == null || ls.size() == 0) {
				Assert.fail("Error --- Index has no stocks " + index);
			}
		}
	}
}
