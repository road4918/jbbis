package com.hzjbbis.junit;

import com.hzjbbis.fas.protocol.meter.BbMeterFrame;
import com.hzjbbis.fas.protocol.meter.BbMeterParser;
import com.hzjbbis.fas.protocol.meter.SmMeterFrame;
import com.hzjbbis.fas.protocol.meter.SmMeterParser;
import com.hzjbbis.fas.protocol.meter.ZjMeterFrame;
import com.hzjbbis.fas.protocol.meter.ZjMeterParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;
import com.hzjbbis.fas.startup.ClassLoaderUtil;

import junit.framework.TestCase;

public class MeterProtocolTest extends TestCase {
	public void setUp(){
		ClassLoaderUtil.initializeClassPath();
	}
	
	public void tearDown(){
		System.out.println("good bye");
	}
	public void testZjMeterFrame(){
		/*byte[] data=new byte[]{0x68,0x1C,0x1C,0x68 
				,0x25 
				,0x10,0x04 
				,0x00,0x00,0x00 
				,0x00,0x00,0x00 
				,0x00,0x00,0x00 
				,0x00,0x00,0x00 
				,0x05,0x00
				,0x13,0x00 
				,(byte)0x88,0x22 
				,0x00,0x00 
				,0x00,0x00 
				,0x00,0x00 
				,(byte)0xED 
				,(byte)0xE8,0x0D};*/
		ZjMeterParser parser=new ZjMeterParser();
		String[] datakeys=new String[]{"B611","B612","B613","B621","B622","B623"};
		String[] zjkeys=parser.convertDataKey(datakeys);
		//ZjMeterFrame frame=new ZjMeterFrame();
		//frame.parse(data,0,data.length);
		//byte[] result=frame.getData();
		//Object[] datas=parser.parser(data,0,data.length );
		TestCase.assertNotNull(zjkeys);
	}
	
	public void testBbMeterFrame(){
		/*byte[] data=new byte[]{0x68,0x01,00,0x00,0x00,0x00,0x00,0x68
				,(byte)0x81,0x06,0x43,(byte)0xC3,(byte)0x8A,(byte)0xB7
				,0x4B,0x33,0x1D,0x16};*/
		byte[] data=new byte[]{0x68,0x07,0x28,0x04,0x00,0x00,0x00,0x68
				,(byte)0x81,0x17,0x52,(byte)0xC3,0x3A,0x55,0x7C,0x33,(byte)0x9A
				,0x64,0x34,0x33,0x69,(byte)0x99,0x65,0x33,0x33,0x33,0x33,0x33
				,0x37,0x57,0x48,0x33,(byte)0xDD,(byte)0x9F,0x16};
		BbMeterParser parser=new BbMeterParser();
		BbMeterFrame frame=new BbMeterFrame();
		parser.convertDataKey(new String[]{"B611","B612","B613","B621","B622","B623"});
		frame.parse(data,0,data.length);
		//byte[] result=frame.getData();
		Object[] datas=parser.parser(data,0,data.length );
		TestCase.assertNotNull(datas);
	}
	
	public void testSMMeterFrame(){
		/*byte[] data=new byte[]{0x68,0x01,00,0x00,0x00,0x00,0x00,0x68
				,(byte)0x81,0x06,0x43,(byte)0xC3,(byte)0x8A,(byte)0xB7
				,0x4B,0x33,0x1D,0x16};*/
		String hex="000246283030290D0A30283733323333333737290D0A312838383838383838290D0A322831353A32393A3432290D0A332830372D30332D3037290D0A342E312830333135332E33322A6B5768290D0A342E312A30322830333133352E3139290D0A342E312A30312830333131322E3933290D0A342E322830303738392E37372A6B5768290D0A342E322A30322830303738352E3230290D0A342E322A30312830303737392E3435290D0A342E332830323432382E31342A6B5768290D0A342E332A30322830323431352E3534290D0A342E332A30312830323339352E3639290D0A352E312830303030302E30302A6B5768290D0A352E312A30322830303030302E3030290D0A352E312A30312830303030302E3030290D0A352E322830303030302E30302A6B5768290D0A352E322A30322830303030302E3030290D0A352E322A30312830303030302E3030290D0A352E332830303030302E30302A6B5768290D0A352E332A30322830303030302E3030290D0A352E332A30312830303030302E3030290D0A362830363337312E32342A6B5768290D0A362A30322830363333352E3934290D0A372830303030302E30302A6B5768290D0A372A30322830303030302E3030290D0A382830303837392E32382A6B76617268290D0A382A30322830303837352E3632290D0A392830303030302E30302A6B76617268290D0A392A30322830303030302E3030290D0A31302E3128302E3532362A6B57292830372D30332D30362032313A3434290D0A31302E312A303228302E333737292830372D30312D32352032313A3539290D0A31302E312A303128302E353937292830372D30312D30332032313A3434290D0A31302E3228302E3436382A6B57292830372D30332D30362032303A3539290D0A31302E322A303228302E333339292830372D30312D32352032303A3539290D0A31302E322A303128302E353139292830372D30312D30392032303A3539290D0A31302E3328302E3530382A6B57292830372D30332D30362032323A3134290D0A31302E332A303228302E333734292830372D30312D32352032323A3134290D0A31302E332A303128302E353835292830372D30312D30342032323A3239290D0A31312E3128302E3030302A6B57292830302D30302D30302030303A3030290D0A31312E312A303228302E303030292830302D30302D30302030303A3030290D0A31312E312A303128302E303030292830302D30302D30302030303A3030290D0A31312E3228302E3030302A6B57292830302D30302D30302030303A3030290D0A31312E322A303228302E303030292830302D30302D30302030303A3030290D0A31312E322A303128302E303030292830302D30302D30302030303A3030290D0A31312E3328302E3030302A6B57292830302D30302D30302030303A3030290D0A31312E332A303228302E303030292830302D30302D30302030303A3030290D0A31312E332A303128302E303030292830302D30302D30302030303A3030290D0A313228302E3532362A6B57292830372D30332D30362032313A3434290D0A31322A303228302E333737292830372D30312D32352032313A3539290D0A31322A303128302E353937292830372D30312D30332032313A3434290D0A31332E3128302E3030302A6B57292830302D30302D30302030303A3030290D0A31332E312A303228302E303030292830302D30302D30302030303A3030290D0A31332E312A303128302E303030292830302D30302D30302030303A3030290D0A31342831393439332A68290D0A4C2E31283130342A56290D0A4C2E32283030302A56290D0A4C2E33283130322A56290D0A31352830303033290D0A31362E302830303030290D0A31362E312830303030290D0A31362E322830303030290D0A31362E332830303030290D0A31372E302830303030290D0A31372E312830303030290D0A31372E322830303030290D0A31372E332830303030290D0A31382830332D30372D3235290D0A3139";
		byte[] data=new byte[hex.length()/2];
		ParseTool.HexsToBytesCB(data, 0, hex);
		SmMeterParser parser=new SmMeterParser();
		SmMeterFrame frame=new SmMeterFrame();
		frame.parse(data,0,data.length);
		//byte[] result=frame.getData();
		Object[] datas=parser.parser(data,0,data.length );
		TestCase.assertNotNull(datas);
	}
}