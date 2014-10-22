package test.hzjbbis.monitor;

import junit.framework.TestCase;

import com.hzjbbis.fk.monitor.biz.HandleListFile;

public class ListFileTest extends TestCase {

	protected void tearDown() throws Exception {
	}

	public void testListString() {
		System.out.println(System.getProperty("user.dir"));
		
		System.out.println(HandleListFile.getListFile().list("."));
	}

	public void testListStringString() {
		System.out.println(HandleListFile.getListFile().list(".","*.project"));
	}

}
