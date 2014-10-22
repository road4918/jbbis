package com.hzjbbis.fas.protocol.handler;

import com.hzjbbis.fas.startup.ClassLoaderUtil;
import com.hzjbbis.fas.protocol.zj.viewer.FrameInformationFactory;

import junit.framework.TestCase;


/**
 *@filename	ProtocolViewerTest.java
 *@auther	netice
 *@date		2007-7-18
 *@version	1.0
 *TODO
 */
public class ProtocolViewerTest extends TestCase {

	public void setUp(){
		ClassLoaderUtil.initializeClassPath();
	}
	
	public void tearDown(){
		System.out.println("good bye");
	}
	
	public void testFrameInfo(){
		String data="FEFEFEFE6894308204811668815E0001000000000000009980FFFFFFFFFFFFFFFFFF8580FFFFFFFFFFFFFF9680FFFFFFFFFFFFFF8780FFFFFFFFFFFFFF9880FFFFFFFFFFFFFF5580325634020105127680FFFFFFFFFFFFFF7080FF9380FFFFFFFFFFFFFF7180FFFFFFFFFFFFFF3E16";
		String info=FrameInformationFactory.getFrameInformation(data);
		System.out.println(info);
		TestCase.assertNotNull(info);
	}
	
	public void testRtu(){
		/*Rtu rtu=new Rtu();
		rtu.setSmsGateNum("01,95598340100");
		rtu.setSmsGateNum("95598340100");
		rtu.setMcommAddress("02,192.124.3.3:7788");
		rtu.setMcommAddress("192.124.3.3:7788");
		TestCase.assertNotNull(rtu);*/
	}
}
