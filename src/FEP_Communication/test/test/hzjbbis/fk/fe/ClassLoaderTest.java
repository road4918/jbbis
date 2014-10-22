package test.hzjbbis.fk.fe;

import java.util.Calendar;

public class ClassLoaderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ClassLoaderUtil.initializeClassPath();
		System.out.println(System.currentTimeMillis());
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(0);
		System.out.println("year="+cl.get(Calendar.YEAR));
		cl = Calendar.getInstance();
		cl.add(Calendar.YEAR, 2000000000);
		System.out.println(cl.getTimeInMillis());
	}

}
