package org.ebayopensource.turmeric.runtime.sif.impl.internal.service.config;

import java.net.URL;
import java.util.List;

public interface ServiceLocationHolder {
	public URL getCurrentAddress();
	public void setInvokeSuccess(boolean success);
	public boolean cycledThrough();
	public int getNumOfAddresses();
	public void resetLocationCycling();
	public void setLocations(List<URL> locations);
}