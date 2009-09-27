package phenom.stock;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class NameValuePairTest {
	public NameValuePairTest() {

	}

	@Test
	public void testSearch() {
		NameValuePair nv1 = new NameValuePair("01", 1.0);
		NameValuePair nv2 = new NameValuePair("02", 2.0);
		NameValuePair nv3 = new NameValuePair("03", 3.0);
		
		List<NameValuePair> ls = new ArrayList<NameValuePair>();
		ls.add(nv1);
		ls.add(nv2);
		ls.add(nv3);
		NameValuePair search = new NameValuePair("02", -2.0);
		
		int i = Collections.binarySearch(ls, search);
		System.out.println(i);		
	}
}
