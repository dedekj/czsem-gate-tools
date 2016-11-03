/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import czsem.fs.query.FSQuery.QueryData;

public class ReferencingRestriction extends AttrRestriction {
	
	public static final Pattern pattern = Pattern.compile("\\{([^.]+)\\.(.+)\\}");	

	protected final String origString;
	protected final String refName;
	protected final String refAttr;
	protected final BasicRestriction r;
	
	
	public static String[] tryParseRefString(String refString) {
		if (refString == null) return null;
		
		Matcher m = pattern.matcher(refString.trim());
		if (! m.matches()) return null;
		String name = m.group(1);
		String attr = m.group(2);
		
		return new String [] {name, attr}; 
	}

	public ReferencingRestriction(BasicRestriction r, String attr, String refString) {
		super(attr);
		origString = refString.trim();
		this.r = r;
		
		String[] split = tryParseRefString(origString);
		
		if (split == null) throw new InvalidParameterException("Not a valid ref string: origString");
		
		refName = split[0];
		refAttr = split[1];
	}

	public boolean evaluate(QueryData data, int nodeId, Map<String, Integer> dataBindings) {
		Integer refNodeId = dataBindings.get(refName);
		if (refNodeId == null)
			throw new IllegalStateException("Unknow binding for node name: "+refName);
		
		Object rightValue = data.getNodeAttributes().getValue(refNodeId, refAttr);
		Object leftValue =  data.getNodeAttributes().getValue(nodeId, getAttrName());
		
		boolean ret = r.evaluate(leftValue, rightValue);
		return ret;
	}

	@Override
	public String getComparator() {
		return r.getComparator();
	}

	@Override
	public String getRightArg() {
		return origString;
	}

}
