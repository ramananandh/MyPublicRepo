package org.ebayopensource.turmeric.runtime.sif.impl.internal.service.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class CustomServiceLocationHolder implements ServiceLocationHolder{
		private class ServiceAddressHolder{
			private URL serviceAddress;
			boolean used = false;
			
			public ServiceAddressHolder(URL serviceAddress){
				this.serviceAddress = serviceAddress;
			}
			public URL getServiceAddress(){
				used = true;
				return serviceAddress;
			}
		}
		//private List<URL> serviceAddresses;
		private boolean lastSuccess=true;
		private int currentPointer=0;
		private ArrayList<ServiceAddressHolder> holders;
		private Object lock = new Object();

		public CustomServiceLocationHolder(List<URL> serviceAddresses) {
			holders = new ArrayList<ServiceAddressHolder>();
			if(serviceAddresses!=null)
				for(URL add:serviceAddresses){
					ServiceAddressHolder holder = new ServiceAddressHolder(add);
					holders.add(holder);
				}
		}

		public URL getCurrentAddress() {
			synchronized(lock){
				if(holders.isEmpty())
					return null;
				if(!lastSuccess){
					// get the next one
					currentPointer++;
					if(currentPointer >= holders.size()){
						// go over
						currentPointer=0;
					}
					lastSuccess = true;
				}
			}
			ServiceAddressHolder h =holders.get(currentPointer);
			h.used = true;
			return h.getServiceAddress();
		}

		public void setInvokeSuccess(boolean success) {
			lastSuccess = success;
		}

		public boolean cycledThrough() {
			// each service address needs to be checked if they have been cycled through
			synchronized (holders) {
				for(ServiceAddressHolder h:holders){
					if(!h.used)
						return false;
				}
			}
			return true;
		}

		public int getNumOfAddresses() {
			return holders.size();
		}

		public void resetLocationCycling() {
			synchronized (holders) {
				for(ServiceAddressHolder h:holders){
					h.used = false;
				}				
			}
		}

		@Override
		public void setLocations(List<URL> locations) {
			synchronized(lock){
				holders = new ArrayList<ServiceAddressHolder>();
				for(URL add:locations){
					ServiceAddressHolder holder = new ServiceAddressHolder(add);
					holders.add(holder);
				}
				currentPointer = 0;
				lastSuccess = true;
			}
		}
	}