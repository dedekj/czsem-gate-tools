package czsem.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RunClass {

	public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (args.length < 1)
		{
			System.err.println("No class name was provided in the first argument.");
			return;
		}
		
		Class<?> cl = Class.forName(args[0]);
		
		Method m = cl.getMethod("main", String[].class);
		String[] args2 = Arrays.copyOfRange(args, 1, args.length);
		m.invoke(null, (Object) args2);
	}
}
