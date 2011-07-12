package org.ebayopensource.turmeric.runtime.sif.impl.internal.service.config;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceLocationFactory {

	private static ServiceLocationFactory INSTANCE = new ServiceLocationFactory();
	private static Map<String, ServiceLocationHolder> holderMap = new HashMap<String, ServiceLocationHolder>();
	
	public static ServiceLocationFactory getInstance(){
		return INSTANCE;
	}
	
	public synchronized ServiceLocationHolder getServiceLocationHolder(List<URL> serviceLocations, String consumerId, String adminName) {
		String key = consumerId+":"+adminName;
		ServiceLocationHolder svcHolder = holderMap.get(key);
		if(svcHolder == null){
			svcHolder = new CustomServiceLocationHolder(serviceLocations);
			holderMap.put(key, svcHolder);
		}
		return svcHolder;
	}

	public ServiceLocationHolder setServiceLocations(List<URL> serviceLocations, String consumerId, String adminName) {
		ServiceLocationHolder svcHolder = getServiceLocationHolder(null, consumerId, adminName);
		svcHolder.setLocations(serviceLocations);
		return svcHolder;
	}

}
