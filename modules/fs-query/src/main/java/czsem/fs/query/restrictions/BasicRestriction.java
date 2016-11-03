/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.restrictions;

import czsem.fs.query.restrictions.Restrictions.CMP;


public interface BasicRestriction extends HasComparator {
	
	public abstract boolean evaluate(Object leftValue, Object rightValue);

	public static final BasicRestriction BR_NEQ = new BasicRestriction() {
		@Override
		public String getComparator() { return CMP.NEQ; }

		@Override
		public boolean evaluate(Object leftValue, Object rightValue) {
			return ! BR_EQ.evaluate(leftValue, rightValue);
		}

	};
	
	public static abstract class ComparativeRestriction implements BasicRestriction {
		protected abstract boolean checkCompare(int compare);

		@Override
		public boolean evaluate(Object leftValue, Object rightValue) {
			if (rightValue == leftValue) return checkCompare(0);
			if (leftValue == null) return false;
			if (rightValue == null) return false;
			
			Object [] typed = convertToSameTypes(leftValue, rightValue);
			
			@SuppressWarnings("unchecked")
			int compare = ((Comparable<Object>) typed[0]).compareTo(typed[1]);
			
			return checkCompare(compare);
		}
	}

	public static final BasicRestriction BR_EQ = new ComparativeRestriction() {
		@Override
		public String getComparator() { return CMP.EQ; }

		@Override
		protected boolean checkCompare(int compare) {
			return compare == 0;
		}
	};

	
	
	public static final BasicRestriction BR_GT = new ComparativeRestriction() {
		@Override
		public String getComparator() { return CMP.GT; }

		@Override
		protected boolean checkCompare(int compare) {
			return compare > 0;
		}
	};
	
	public static final BasicRestriction BR_GTEQ = new ComparativeRestriction() {
		@Override
		public String getComparator() { return CMP.GTEQ; }

		@Override
		protected boolean checkCompare(int compare) {
			return compare >= 0;
		}
	};

	public static final BasicRestriction BR_LT = new ComparativeRestriction() {
		@Override
		public String getComparator() { return CMP.LT; }

		@Override
		protected boolean checkCompare(int compare) {
			return compare < 0;
		}
	};
	
	public static final BasicRestriction BR_LTEQ = new ComparativeRestriction() {
		@Override
		public String getComparator() { return CMP.LTEQ; }

		@Override
		protected boolean checkCompare(int compare) {
			return compare <= 0;
		}
	};
	
	public static Object [] convertToSameTypes(Object leftValue, Object rightValue) {
		if (leftValue == null || rightValue == null) 
			return new Object[] {leftValue, rightValue};

		if (leftValue.getClass().equals(rightValue.getClass()))
			return new Object[] {leftValue, rightValue};
		
		if (leftValue instanceof Number) {
			Double rightDouble = toDouble(rightValue);
			if (rightDouble != null) {
				return new Object[] {toDouble(leftValue), rightDouble};
			}
		} else if (rightValue instanceof Number) {
			Double leftDouble = toDouble(leftValue);
			if (leftDouble != null) {
				return new Object[] {leftDouble, toDouble(rightValue)};
			}
			
		}
		
		return new Object[] {leftValue.toString(), rightValue.toString()}; 
	}
	
	public static Double toDouble(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		
		try {
			return new Double(value.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
