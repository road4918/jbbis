package com.hzjbbis.junit;


import junit.framework.TestCase;


/**
 *@filename	MessageTest.java
 *@auther	yangdh
 *@date		2006-12-15
 *@version	1.0
 *TODO
 */
public class MessageTest extends TestCase {
	public void setUp(){
		//ClassLoaderUtil.initializeClassPath();
	}
	
	public void tearDown(){
		System.out.println("good bye");
	}
	public void testSplit(){
		String s="SB_DLSJ;WGZXX11;01";
		String[] ss=s.split("/");
		for(int i=0;i<ss.length;i++)
		System.out.println(ss[i]);
		TestCase.assertNotNull(ss);
	}
	/*
	public void createmsgtest(){
		String rawmsg="689A150581C0026882240001061214214501020F43022100530044023000802100FFFFFFFFFFFF8208100045020100BE16";
		MessageZj rt=MessageZj.loadRepMessage(rawmsg);
		TestCase.assertNotNull(rt);
	}
	
	public void testDeserial(){
		String buf=null;
		buf="com.hzjbbis.fas.framework.message.MessageZj|uprawstring=6891109105801668820E0002070517183001020F000300EE009D16|msg.rtu.ip=";
		MessageZj zj=new MessageZj();
		try {
			zj.deserializeFromString(buf);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TestCase.assertNotNull(zj);
	}*/
}
