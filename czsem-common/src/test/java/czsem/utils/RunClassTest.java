package czsem.utils;

import static org.testng.AssertJUnit.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.testng.annotations.Test;

public class RunClassTest {
	@Test
	public void main() throws IllegalArgumentException, SecurityException,
			ClassNotFoundException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		String[] args1 = { PrintArgs.class.getCanonicalName() };
		String[] args2 = { PrintArgs.class.getCanonicalName(), "ahoj1", "ahoj2" };

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setErr(new PrintStream(out));

		RunClass.main(args1);
		assertEquals(Arrays.toString(new String[0]), out.toString());
		out.reset();

		RunClass.main(args2);
		assertEquals(
				Arrays.toString(Arrays.copyOfRange(args2, 1, args2.length)),
				out.toString());
	}
}
