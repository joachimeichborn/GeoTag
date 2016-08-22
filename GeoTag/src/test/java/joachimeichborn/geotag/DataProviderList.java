package joachimeichborn.geotag;

import java.util.ArrayList;
import java.util.List;

public class DataProviderList {
	private final List<Object[]> data;

	public DataProviderList() {
		data = new ArrayList<>();
	}
	
	public void add(final Object... aItems) {
		data.add(aItems);
	}
	
	public Object[][] toArray(){
		return data.toArray(new Object[0][0]);
	}
}
