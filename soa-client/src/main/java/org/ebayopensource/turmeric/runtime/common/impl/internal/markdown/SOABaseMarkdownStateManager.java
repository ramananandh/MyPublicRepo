/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.runtime.common.impl.internal.markdown;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.ebayopensource.turmeric.runtime.common.impl.internal.service.ServiceDesc;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.runtime.common.service.ServiceOperationDesc;
import org.ebayopensource.turmeric.runtime.sif.impl.internal.service.ClientServiceDesc;

import com.ebay.kernel.markdown.simple.SimpleMarkdownStateManager;

/**
 * @author ichernyshev
 */
public abstract class SOABaseMarkdownStateManager<I extends SOABaseMarkdownStateId, S extends SOABaseMarkdownState<I>>
	extends SimpleMarkdownStateManager<I,S>
{
	@Override
	protected boolean doesStateIdInclude(I outer, I inner) {
		if (!outer.getAdminName().equals(inner.getAdminName())) {
			return false;
		}

		String outerOpName = outer.getOperationName();
		if (outerOpName != null) {
			String innerOpName = inner.getOperationName();
			if (innerOpName == null || !innerOpName.equals(outerOpName)) {
				return false;
			}
		}

		return true;
	}

	private void createServiceStateIds(ServiceDesc svcDesc, Collection<I> ids) {
		List<URL> locations = null;
		if(svcDesc instanceof ClientServiceDesc){
			locations = ((ClientServiceDesc)svcDesc).getServiceLocations();
		}
		
		createServiceStateIds(svcDesc, null, ids, locations);

		Collection<ServiceOperationDesc> ops = svcDesc.getAllOperations();
		for (ServiceOperationDesc op: ops) {
			String opName = op.getName();
			createServiceStateIds(svcDesc, opName, ids, locations);
		}
	}

	private void createServiceStateIds(ServiceDesc svcDesc, String opName, Collection<I> ids, List<URL> locations)
	{
		String adminName = svcDesc.getAdminName();
		String subname = svcDesc.getServiceId().getServiceSubname();
		if(locations!=null &&!locations.isEmpty())
		{
			for(URL location:locations){
				ids.add(createSoaStateId(adminName, opName, null, location.toString()));
				if (subname != null) {
					ids.add(createSoaStateId(adminName, opName, subname, location.toString()));
				}	
			}
		}else{
			ids.add(createSoaStateId(adminName, opName, null, null));
			if (subname != null) {
				ids.add(createSoaStateId(adminName, opName, subname, null));
			}
		}
	}

	public final void addServiceStates(ServiceDesc svcDesc) {
		Collection<I> ids = new ArrayList<I>();
		createServiceStateIds(svcDesc, ids);

		Map<I,Throwable> errors = new HashMap<I,Throwable>();
		createStates(ids, errors);

		// log any errors after loading
		for (Map.Entry<I,Throwable> entry: errors.entrySet()) {
			I id = entry.getKey();
			Throwable e = entry.getValue();

			LogManager.getInstance(SOABaseMarkdownStateManager.class).log(Level.SEVERE,
				"Unable to pre-create markdown state " + id.getStringId() +
				" in '" + getName() + "'. " + e.toString(), e);
		}

		postCreateStates(svcDesc, ids);
	}

	protected void postCreateStates(ServiceDesc svcDesc, Collection<I> ids) {
		// no work here, allow overrides
	}

	@Override
	protected Collection<I> getParentIds(S primaryState) {
		I primaryId = primaryState.getId();

		String opName = primaryId.getOperationName();
		String subname = primaryId.getSubname();
		String location = primaryId.getLocation();
		if (opName == null && subname == null) {
			return null;
		}

		Collection<I> result = new ArrayList<I>();

		// add top level
		result.add(createSoaStateId(primaryId.getAdminName(), null, null, location));

		if (opName != null && subname != null) {
			result.add(createSoaStateId(primaryId.getAdminName(), opName, null, location));
			result.add(createSoaStateId(primaryId.getAdminName(), null, subname, location));
		}

		return result;
	}

	protected abstract I createSoaStateId(String adminName, String opName, String subname, String location);
}

