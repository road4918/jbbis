package com.hzjbbis.junit;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.hzjbbis.fk.message.zj.MessageZjHead;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fas.model.*;
import com.hzjbbis.fas.protocol.codec.MessageDecoder;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.data.DataItem;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;
import com.hzjbbis.fas.protocol.meter.MeterProtocolFactory;
import com.hzjbbis.fas.protocol.meter.conf.MeterProtocolDataSet;
import com.hzjbbis.fas.protocol.zj.parse.*;
import com.hzjbbis.fas.startup.ClassLoaderUtil;

public class ProtocolTest extends TestCase {
	public void setUp(){
		ClassLoaderUtil.initializeClassPath();
	}
	
	public void tearDown(){
		System.out.println("good bye");
	}
	
	public void testProtocolHandlerFactory(){
		MessageZj msgzj=new MessageZj();
				
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{
				0x01
				,0x68,0x18,0x18,0x68
				,(byte)0xFF,0x10,0x01,0x56,0x01,0x49,0x00
				,0x62,0x32,0x16,0x00,0x00,0x00,0x00,0x00,0x54
				,0x13,0x30,0x00,0x40,0x55,0x02,0x00,(byte)0xED,0x75,0x0D
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=0x1F;
		hzj.c_func=0;
		hzj.c_dir=(byte)0x1;
		hzj.rtua=0x92070179;
		
		msgzj.head=hzj;
		msgzj.data=repdata;
		FaalReadForwardDataRequest para=new FaalReadForwardDataRequest();
		List calldks=new ArrayList();
		FaalRequestParam param01=new FaalRequestParam();
		param01.setName("9010");
		calldks.add(param01);
		FaalRequestParam param02=new FaalRequestParam();
		param02.setName("9011");
		calldks.add(param02);
		FaalRequestParam param03=new FaalRequestParam();
		param03.setName("9012");
		calldks.add(param03);
		FaalRequestParam param04=new FaalRequestParam();
		param04.setName("9013");
		calldks.add(param04);
		FaalRequestParam param05=new FaalRequestParam();
		param05.setName("9014");
		calldks.add(param05);
		para.setParams(calldks);
		para.setTn("1");
		HostCommand hc=new HostCommand();
		hc.setMessageCount(1);
		hc.setId(new Long(9210012831123L));
		List results=new ArrayList();
		results.add(para);
		hc.setResults(results);
		//msgzj.setAttachment(hc);
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testProcess(){
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{				
				0x0E,0x06,0x08,0x28,0x03,0x00,0x01,0x02,0x0F,0x40,0x36,0x00
				,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
				,0x00,0x00,0x00,0x00,0x64,0x14,0x00,0x00,0x00,0x00,0x00,0x00,0x00
				,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
				,0x00,0x00,0x00,0x00,0x00,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
				,(byte)0xFF,(byte)0xFF,(byte)0xFF
				,(byte)0xFF,0x07,0x01,0x07,0x01,(byte)0xFF,(byte)0xFF,(byte)0xF0,(byte)0xFF
				,(byte)0xF0,(byte)0xFF,0x00,0x00
				,0x69,0x01,0x00,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
				,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,0x01,0x00
				,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
				,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
				,0x00,0x00,0x00,0x06,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
				,0x00,0x00,0x00,0x00,0x00,0x00,0x00
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=0x7D;
		hzj.c_func=2;
		hzj.c_dir=(byte)0x1;
		hzj.rtua=0x92040135;
		
		MessageZj msgzj=new MessageZj();
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		
		HostCommand hc=new HostCommand();
		HostCommandResult rt=new HostCommandResult();		
		rt.setCommandId(new Long(0));		
		//msgzj.setAttachment(hc);
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		//Object result=ph.process(msgzj);
		MessageDecoder decoder=ph.getCodecFactory().getDecoder(2);
		Object result=decoder.decode(msgzj);
		List datas=(List)result;
		for(Iterator iter=datas.iterator();iter.hasNext();){
			RtuData bean=(RtuData)iter.next();
		}
		TestCase.assertNotNull(result);
	}
	
	public void testProcess01(){
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x01,0x07,0x09,0x13,0x09,0x45,0x01,0x02,0x0F
				,0x23,0x02,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
				,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,0x66,(byte)0x80,0x65,0x00
				,(byte)0x88,0x22,0x46,0x00,0x27,(byte)0x89,0x12,0x00
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=0x2E;
		hzj.c_func=2;
		hzj.c_dir=(byte)0x1;
		hzj.rtua=0x94000013;
		
		MessageZj msgzj=new MessageZj();
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		
//		HostCommand hc=new HostCommand();
//		HostCommandResult rt=new HostCommandResult();		
//		rt.setCommandId(new Long(0));		
//		msgzj.setAttachment(hc);
//		FaalReadCurrentDataRequest para=new FaalReadCurrentDataRequest();
//		List calldks=new ArrayList();
//		FaalRequestParam param01=new FaalRequestParam();
//		param01.setName("8015");
//		calldks.add(param01);
//		para.setParams(calldks);
//		hc.setMessageCount(1);
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	
	
	public void testParser0x9130(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x30,(byte)0x91,0x01,0x66,0x21,0x34
				,0x31,(byte)0x91,0x01,0x66,0x21,0x34
				,0x32,(byte)0x91,0x01,0x66,0x21,0x34
				,0x33,(byte)0x91,0x01,0x66,0x21,0x34
				,0x34,(byte)0x91,0x01,0x66,0x21,0x34
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x9240(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x40,(byte)0x91,0x01,0x66,0x21,0x34
				,0x41,(byte)0x91,0x01,0x66,0x21,0x34
				,0x42,(byte)0x91,0x01,0x66,0x21,0x34
				,0x43,(byte)0x91,0x01,0x66,0x21,0x34
				,0x44,(byte)0x91,0x01,0x66,0x21,0x34
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x9250(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x50,(byte)0x91,0x01,0x66,0x21,0x34
				,0x51,(byte)0x91,0x01,0x66,0x21,0x34
				,0x52,(byte)0x91,0x01,0x66,0x21,0x34
				,0x53,(byte)0x91,0x01,0x66,0x21,0x34
				,0x54,(byte)0x91,0x01,0x66,0x21,0x34
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x9260(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x60,(byte)0x91,0x01,0x66,0x21,0x34
				,0x61,(byte)0x91,0x01,0x66,0x21,0x34
				,0x62,(byte)0x91,0x01,0x66,0x21,0x34
				,0x63,(byte)0x91,0x01,0x66,0x21,0x34
				,0x64,(byte)0x91,0x01,0x66,0x21,0x34
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xA010(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x10,(byte)0xA0,0x01,0x66,0x21
				,0x11,(byte)0xA0,0x01,0x66,0x21
				,0x12,(byte)0xA0,0x01,0x66,0x21
				,0x13,(byte)0xA0,0x01,0x66,0x21
				,0x14,(byte)0xA0,0x01,0x66,0x21
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xA020(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x20,(byte)0xA0,0x01,0x66,0x21
				,0x21,(byte)0xA0,0x01,0x66,0x21
				,0x22,(byte)0xA0,0x01,0x66,0x21
				,0x23,(byte)0xA0,0x01,0x66,0x21
				,0x24,(byte)0xA0,0x01,0x66,0x21
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xA040(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x10,(byte)0xA4,0x01,0x66,0x21
				,0x11,(byte)0xA4,0x01,0x66,0x21
				,0x12,(byte)0xA4,0x01,0x66,0x21
				,0x13,(byte)0xA4,0x01,0x66,0x21
				,0x14,(byte)0xA4,0x01,0x66,0x21
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xA420(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x20,(byte)0xA4,0x01,0x66,0x21
				,0x21,(byte)0xA4,0x01,0x66,0x21
				,0x22,(byte)0xA4,0x01,0x66,0x21
				,0x23,(byte)0xA4,0x01,0x66,0x21
				,0x24,(byte)0xA4,0x01,0x66,0x21
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xB010(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x10,(byte)0xB0,0x01,0x23,0x21,0x12
				,0x11,(byte)0xB0,0x01,0x23,0x21,0x02
				,0x12,(byte)0xB0,0x01,0x23,0x21,0x11
				,0x13,(byte)0xB0,0x01,0x23,0x21,0x10
				,0x14,(byte)0xB0,0x01,0x23,0x19,0x12
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xB020(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x20,(byte)0xB0,0x01,0x23,0x21,0x12
				,0x21,(byte)0xB0,0x01,0x23,0x21,0x02
				,0x22,(byte)0xB0,0x01,0x23,0x21,0x11
				,0x23,(byte)0xB0,0x01,0x23,0x21,0x10
				,0x24,(byte)0xB0,0x01,0x23,0x19,0x12
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xB410(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x10,(byte)0xB4,0x01,0x23,0x21,0x12
				,0x11,(byte)0xB4,0x01,0x23,0x21,0x02
				,0x12,(byte)0xB4,0x01,0x23,0x21,0x11
				,0x13,(byte)0xB4,0x01,0x23,0x21,0x10
				,0x14,(byte)0xB4,0x01,0x23,0x19,0x12
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xB420(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x20,(byte)0xB4,0x01,0x23,0x21,0x12
				,0x21,(byte)0xB4,0x01,0x23,0x21,0x02
				,0x22,(byte)0xB4,0x01,0x23,0x21,0x11
				,0x23,(byte)0xB4,0x01,0x23,0x21,0x10
				,0x24,(byte)0xB4,0x01,0x23,0x19,0x12
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xB210(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x10,(byte)0xB2,0x01,0x23,0x21,0x12
				,0x11,(byte)0xB2,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff
				,0x12,(byte)0xB2,0x01,0x23
				,0x13,(byte)0xB2,(byte)0x99,0x34		
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xB300(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x10,(byte)0xB3,0x01,0x23
				,0x11,(byte)0xB3,0x01,0x23
				,0x12,(byte)0xB3,0x01,0x23
				,0x13,(byte)0xB3,(byte)0x99,0x34
				,0x20,(byte)0xB3,0x01,0x23,0x21
				,0x21,(byte)0xB3,0x01,0x23,0x21
				,0x22,(byte)0xB3,0x01,0x23,0x65
				,0x23,(byte)0xB3,(byte)0x99,0x34,0x65
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xB600(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x11,(byte)0xB6,(byte)0x81,0x03
				,0x12,(byte)0xB6,0x71,0x03
				,0x13,(byte)0xB6,(byte)0x99,0x02				
				,0x21,(byte)0xB6,0x01,0x23
				,0x22,(byte)0xB6,0x01,0x23
				,0x23,(byte)0xB6,(byte)0x99,0x34
				,0x30,(byte)0xB6,0x01,0x13,0x40
				,0x31,(byte)0xB6,0x01,0x23,0x40
				,0x32,(byte)0xB6,0x01,0x23,0x03
				,0x33,(byte)0xB6,(byte)0x99,0x34,0x56
				,0x40,(byte)0xB6,0x01,0x13
				,0x41,(byte)0xB6,0x01,0x23
				,0x42,(byte)0xB6,0x01,0x23
				,0x43,(byte)0xB6,(byte)0x99,0x34
				,0x50,(byte)0xB6,0x01,0x13
				,0x51,(byte)0xB6,0x01,0x23
				,0x52,(byte)0xB6,0x01,0x23
				,0x53,(byte)0xB6,(byte)0x99,0x34
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0xC000(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x10,(byte)0xC0,(byte)0x02,0x03,0x10,0x06
				,0x11,(byte)0xC0,0x41,0x03,0x23
				,0x20,(byte)0xC0,(byte)0x01				
				,0x30,(byte)0xC0,0x01,0x23,0x76
				,0x31,(byte)0xC0,0x01,0x23,0x56
				,0x19,(byte)0xC1,(byte)0x99,0x34,0x01,0x04
				,0x1A,(byte)0xC1,0x01,0x13,0x40,0x01
				,0x31,(byte)0xC3,0x01,0x23,0x10
				,0x32,(byte)0xC3,0x02,0x23,0x12
				,0x33,(byte)0xC3,(byte)0x03,0x34,0x14
				,0x34,(byte)0xC3,0x03,0x04,0x15
				,0x35,(byte)0xC3,0x01,0x23,0x16
				,0x36,(byte)0xC3,0x02,0x23,0x17
				,0x37,(byte)0xC3,(byte)0x01,0x34,0x18
				,0x38,(byte)0xC3,0x01,0x13,0x19				
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x8E00(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x11,(byte)0x8E,(byte)0x02,0x03,0x10,0x06
				,0x12,(byte)0x8E,0x41,0x03,0x23,0x78
				,0x13,(byte)0x8E,(byte)0x01,0x03,0x23,0x78
				,0x21,(byte)0x8E,0x01,0x23,0x76,0x34
				,0x22,(byte)0x8E,0x01,0x23,0x56,(byte)0x98
				,0x23,(byte)0x8E,(byte)0x99,0x34,0x01,0x04				
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x8E30(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x31,(byte)0x8E,(byte)0x02,0x03,0x10,0x06
				,0x32,(byte)0x8E,0x41,0x03,0x23,0x78
				,0x33,(byte)0x8E,(byte)0x01,0x03,0x23,0x78
				,0x30,(byte)0x8E,(byte)0x01,0x03,0x23,0x78
				,0x41,(byte)0x8E,0x01,0x23,0x76,0x34
				,0x42,(byte)0x8E,0x01,0x23,0x56,(byte)0x98
				,0x43,(byte)0x8E,(byte)0x99,0x34,0x01,0x04
				,0x40,(byte)0x8E,(byte)0x01,0x03,0x23,0x78
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x8E60(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x60,(byte)0x8E,(byte)0x02,0x03,0x10,0x16
				,0x61,(byte)0x8E,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff
				,0x62,(byte)0x8E,0x41,0x03,0x18
				,0x63,(byte)0x8E,0x03,0x23,0x18				
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x8E70(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,0x70,(byte)0x8E,(byte)0x02,0x03
				,(byte)0x80,(byte)0x8E,(byte)0x02
				,(byte)0x81,(byte)0x8E,(byte)0xff
				,(byte)0x82,(byte)0x8E,0x03
				,(byte)0x83,(byte)0x8E,(byte)0x02
				,(byte)0x84,(byte)0x8E,0x41
				,(byte)0x85,(byte)0x8E,0x03				
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x8E86(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,(byte)0x86,(byte)0x8E,(byte)0x02,0x05,0x45,0x12,0x23,0x03,0x06
				,(byte)0x87,(byte)0x8E,(byte)0x02,0x05,0x15,0x12,0x23,0x03,0x06
				,(byte)0x88,(byte)0x8E,(byte)0x02,0x05,0x25,0x12,0x23,0x03,0x06
				,(byte)0x89,(byte)0x8E,(byte)0x02,0x05,0x35,0x12,0x23,0x03,0x06
				,(byte)0x8A,(byte)0x8E,(byte)0x02,0x05,0x30,0x12,0x23,0x03,0x06
				,(byte)0x8B,(byte)0x8E,(byte)0x12,0x05,0x45,0x12,0x23,0x03,0x06
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testParser0x8E90(){
		MessageZj msgzj=new MessageZj();
		ByteBuffer repdata=ByteBuffer.wrap(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00				
				,(byte)0x90,(byte)0x8E,(byte)0x02,0x05
				,(byte)0x91,(byte)0x8E,(byte)0x02,0x17,0x05,0x15,0x12,0x23,0x03,0x06
				,(byte)0x92,(byte)0x8E,(byte)0x02,0x05,0x16,0x25,0x12,0x23,0x03,0x06
				,(byte)0x93,(byte)0x8E,(byte)0x02,0x05,0x35,0x12,0x23,0x03,0x06
				,(byte)0x94,(byte)0x8E,(byte)0x02,0x05,0x30,0x12,0x23,0x03,0x06
				,(byte)0x95,(byte)0x8E,(byte)0x12,0x05,0x45,0x12,0x23,0x03,0x06
				,(byte)0x96,(byte)0x8E,(byte)0x02,0x05,0x35,0x12,0x23,0x03,0x06
				,(byte)0x97,(byte)0x8E,(byte)0x02,0x05,0x30,0x12,0x23,0x03,0x06
				,(byte)0x98,(byte)0x8E,(byte)0x12,0x05,0x45,0x12,0x23,0x03,0x06
				,(byte)0xA0,(byte)0x8E,(byte)0x12,0x05
				});
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=16;
		hzj.c_func=1;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testProtocolParser(){
		MessageZj msgzj=new MessageZj();		
		byte[] data=new byte[26];
		ParseTool.HexsToBytesCB(data,0,"0A05022001000103010501000004012700000029006004000000");
		ByteBuffer repdata=ByteBuffer.wrap(data);
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=26;
		hzj.c_func=2;
		hzj.rtua=0x99013110;
		hzj.c_dir=(byte)0x80;
		msgzj.head=hzj;
		msgzj.data=repdata;
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	
	
	
	
	public void testParseTool(){
		//byte[] rt=ParseTool.HexsToBytes("F0FF3");
		//String rt=ParseTool.BytesToHexC(new byte[]{0xf,0x3,(byte)0xff},0,3);
		//String rt=ParseTool.BytesBit(new byte[]{(byte)0x74,0x12});
		//String rt=ParseTool.BytesToHexC(new byte[]{(byte)0x74,0x12},0,2);
		//String rt=ParseTool.BytesToHex(new byte[]{(byte)0xfa,0x12},0,2);
		//String rt=ParseTool.BytesToHexL(new byte[]{(byte)0xfa,0x12},0,2);
		//int rt=ParseTool.CharToDecimalB("F");		
		String hex="22:44";
		//byte[] rt=ParseTool.StringToBytesC(hex,3);
		//byte[] rt=ParseTool.StringToBCDsC(hex,3);
		//byte[] rt=ParseTool.TimeToBytes(hex,5,1);
		//byte[] rt=ParseTool.TimeToBytes(hex,3,1);
		//byte[] rt=ParseTool.DecimalToBCDsS(new Double(-2333.03),3,2);
		//byte[] rt=ParseTool.DecimalToBytesS(new Double(-2333.03),3,2);
		byte[] ip=new byte[6];
		//ParseTool.HexsToBytes(ip,0,"f09001");
		ParseTool.HexsToBytesCB(ip,0,"f09001");
		TestCase.assertNotNull(ip);
	}
	
	public void testParserVC8020(){
		byte[] data=new byte[]{0x01,0x34,0x12};
		String rt=(String)DataItemParser.parseVC8020(data,0,3);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8031(){
		byte[] data=new byte[]{0x01,0x23,0x34,0x10};
		String rt=(String)DataItemParser.parseVC8031(data,0,3);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8045(){
		byte[] data=new byte[]{0x15,0x23,0x22};
		String rt=(String)DataItemParser.parseVC804X(data,0,3,0x8045);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8048(){
		byte[] data=new byte[]{0x15,0x12,0x14,0x01,0x05,0x06,0x10,0x11,0x7};
		String rt=(String)DataItemParser.parseVC804X(data,0,9,0x8048);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC804C(){
		byte[] data=new byte[]{0x15,0x01,0x14,0x01};
		String rt=(String)DataItemParser.parseVC804X(data,0,4,0x804C);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC804E(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11};
		Double rt=(Double)DataItemParser.parseVC804X(data,0,4,0x804E);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8051(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,0x01,0x25,0x23};
		String rt=(String)DataItemParser.parseVC805X(data,0,7,0x8051);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8059(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,0x04,0x25,0x3,0x12,0x1};
		String rt=(String)DataItemParser.parseVC805X(data,0,9,0x8059);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8062(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,(byte)0x84};
		String rt=(String)DataItemParser.parseVC806X(data,0,5,0x8062);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8101(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,(byte)0x84,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x12,(byte)0x80,0x13,(byte)0x80};
		String rt=(String)DataItemParser.parseVC8101(data,0,5);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8102(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,(byte)0x84,0x02,0x02,0x02,0x02,0x02
				,0x02,0x02,0x02,0x02,0x02,0x02,0x12,(byte)0x80,0x13,(byte)0x80
				,0x11,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x10,0x11};
		String rt=(String)DataItemParser.parseVC8102(data,0,5);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8103(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,(byte)0x84,0x02,0x02,0x12,(byte)0x80,0x02
				,0x13,(byte)0x80,0x01,0x13};
		String rt=(String)DataItemParser.parseVC8104(data,0,5);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8403(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,(byte)0x84};
		String rt=(String)DataItemParser.parseVC84XX(data,0,5,0x8401);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8501(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,(byte)0x84,0x01,0x02,0x14,0x11,(byte)0x84,0x01,0x02,0x14,0x11,(byte)0x84,0x01,0x12,(byte)0x80,0x11};
		String rt=(String)DataItemParser.parseVC8501(data,0,19);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8601(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,(byte)0x84,0x01,0x02,0x14,0x11,0x12,(byte)0x80};
		String rt=(String)DataItemParser.parseVC8601(data,0,11);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8EXX(){
		byte[] data=new byte[]{0x01,0x02,0x14,0x11,0x23,0x01,0x02};
		String rt=(String)DataItemParser.parseVC8EXX(data,0,7,0x8e87);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVC8E9X(){
		byte[] data=new byte[]{0x01,0x02,(byte)0x83,0x14,0x11,0x23,0x01,0x02};
		String rt=(String)DataItemParser.parseVC8EXX(data,0,7,0x8e91);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVCCXXX(){
		byte[] data=new byte[]{0x01,0x02,0x11,0x05};
		String rt=(String)DataItemParser.parseVCCXXX(data,0,4,0xc010);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVCC119(){
		byte[] data=new byte[]{0x01,(byte)0x87,0x11,(byte)0x85};
		Double rt=(Double)DataItemParser.parseVCCXXX(data,0,4,0xc119);
		TestCase.assertNotNull(rt);
	}
	
	public void testParserVCC331(){
		byte[] data=new byte[]{0x01,(byte)0x34,0x11};
		String rt=(String)DataItemParser.parseVCCXXX(data,0,4,0xc331);
		TestCase.assertNotNull(rt);
	}
	
	public void testParser01(){
		byte[] data=new byte[5];
		Parser01.constructor(data,"120005.01",0,4,2);
		String val=(String)Parser01.parsevalue(data,0,4,2);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser02(){
		byte[] data=new byte[5];
		Parser02.constructor(data,"-10005.01",0,4,2);
		String val=(String)Parser02.parsevalue(data,0,4,2);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser03(){
		byte[] data=new byte[4];
		Parser03.constructor(data,"655.35",0,4,2);
		String val=(String)Parser03.parsevalue(data,0,4,2);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser04(){
		byte[] data=new byte[4];
		Parser04.constructor(data,"-32",0,4,0);
		Number val=(Number)Parser04.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser05(){
		byte[] data=new byte[4];
		Parser05.constructor(data,"FFAA6644",0,4,0);
		String val=(String)Parser05.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser06(){
		byte[] data=new byte[4];
		Parser06.constructor(data,"FFAA6644",0,4,0);
		String val=(String)Parser06.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser07(){
		byte[] data=new byte[4];
		Parser07.constructor(data,"12-04 2:35",0,4,0);
		String val=(String)Parser07.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser08(){
		byte[] data=new byte[4];
		Parser08.constructor(data,"2004-12-04,3",0,4,0);
		String val=(String)Parser08.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser09(){
		byte[] data=new byte[3];
		Parser09.constructor(data,"23:36:10",0,4,0);
		String val=(String)Parser09.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser10(){
		byte[] data=new byte[7];
		Parser10.constructor(data,"2005-11-30 23:36,225",0,7,0);
		String val=(String)Parser10.parsevalue(data,0,7,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser11(){
		byte[] data=new byte[8];
		Parser11.constructor(data,"2005-11-30 23:36,-225.02",0,7,0);
		String val=(String)Parser11.parsevalue(data,0,8,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser12(){
		byte[] data=new byte[3];
		Parser12.constructor(data,"23:36,12",0,3,0);
		String val=(String)Parser12.parsevalue(data,0,3,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser13(){
		byte[] data=new byte[2];
		Parser13.constructor(data,"23:36",0,2,0);
		String val=(String)Parser13.parsevalue(data,0,2,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser14(){
		byte[] data=new byte[9];
		Parser14.constructor(data,"01,955983406",0,9,0);
		String val=(String)Parser14.parsevalue(data,0,9,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser15(){
		byte[] data=new byte[8];
		Parser15.constructor(data,"0138187018",0,8,0);
		String val=(String)Parser15.parsevalue(data,0,8,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser16(){
		byte[] data=new byte[8];
		Parser16.constructor(data,"127.0.1.123:9001",0,8,0);
		String val=(String)Parser16.parsevalue(data,0,8,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser17(){
		byte[] data=new byte[3];
		Parser17.constructor(data,"1234,01",0,3,0);
		String val=(String)Parser17.parsevalue(data,0,3,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser18(){
		byte[] data=new byte[2];
		Parser18.constructor(data,"1234",0,2,0);
		String val=(String)Parser18.parsevalue(data,0,2,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser19(){
		byte[] data=new byte[6];
		Parser19.constructor(data,"2006-02-26 23:45:12",0,6,0);
		String val=(String)Parser19.parsevalue(data,0,6,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser20(){
		byte[] data=new byte[4];
		Parser20.constructor(data,"11,24,12:03",0,4,0);
		String val=(String)Parser20.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser21(){
		byte[] data=new byte[9];
		Parser21.constructor(data,"AF,01,02,03,04,05,06,07,08",0,9,0);
		String val=(String)Parser21.parsevalue(data,0,9,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser22(){
		byte[] data=new byte[4];
		Parser22.constructor(data,"01,25,12:05",0,4,0);
		String val=(String)Parser22.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser23(){
		byte[] data=new byte[7];
		Parser23.constructor(data,"12:05,01,23456.32",0,7,0);
		String val=(String)Parser23.parsevalue(data,0,7,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser24(){
		byte[] data=new byte[9];
		Parser24.constructor(data,"02-14,03-23,06,8",0,9,0);
		String val=(String)Parser24.parsevalue(data,0,9,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser25(){
		byte[] data=new byte[5];
		Parser25.constructor(data,"-3568,23",0,5,0);
		String val=(String)Parser25.parsevalue(data,0,5,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser26(){
		byte[] data=new byte[18];
		Parser26.constructor(data,"01,02,02,04,03,04,03,04,03,01,01,960,0,1,9001",0,5,0);
		String val=(String)Parser26.parsevalue(data,0,18,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser27(){
		byte[] data=new byte[25];
		Parser27.constructor(data,"02,02,02,04,03,04,03,04,03,01,01,1,960,10,AA,0,10,4,9001FFAA",0,25,0);
		String val=(String)Parser27.parsevalue(data,0,25,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser28(){
		byte[] data=new byte[17];
		Parser28.constructor(data,"04,010A,15,03,0,3,0,9010,0,9020,0,9130,2",0,14,0);
		String val=(String)Parser28.parsevalue(data,0,14,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser29(){
		byte[] data=new byte[3];
		Parser29.constructor(data,"01,1,1",0,2,0);
		String val=(String)Parser29.parsevalue(data,0,2,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser30(){
		byte[] data=new byte[5];
		Parser30.constructor(data,"01,02,453289",0,5,0);
		String val=(String)Parser30.parsevalue(data,0,5,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser31(){
		byte[] data=new byte[19];
		Parser31.constructor(data,"01,A202,01,02,01,02,01,02,01,02,01,02,01,02,01,02,01,02",0,19,0);
		String val=(String)Parser31.parsevalue(data,0,19,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser32(){
		byte[] data=new byte[11];
		Parser32.constructor(data,"A202,01,02,1234.56,33.88,99.11,",0,11,0);
		String val=(String)Parser32.parsevalue(data,0,11,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser33(){
		byte[] data=new byte[6];
		Parser33.constructor(data,"121115",0,6,0);
		String val=(String)Parser33.parsevalue(data,0,6,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser34(){
		byte[] data=new byte[14];
		Parser34.constructor(data,"04,1033,03,04,0,2,01,9001,02,A002,1",0,14,0);
		String val=(String)Parser34.parsevalue(data,0,14,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser35(){
		byte[] data=new byte[7];
		Parser35.constructor(data,"2005-11-30 23:36,2.25",0,14,0);
		String val=(String)Parser35.parsevalue(data,0,7,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser36(){
		byte[] data=new byte[4];
		Parser36.constructor(data,"91010389",0,4,0);
		String val=(String)Parser36.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser37(){
		byte[] data=new byte[8];
		Parser37.constructor(data,"127.0.1.123:9001",0,8,2);
		String val=(String)Parser37.parsevalue(data,0,8,2);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser39(){
		byte[] data=new byte[73];
		Parser39.constructor(data,"02,127.0.1.123:9001,5,H,3456as,fd345,wer,34ts",0,71,0);
		String val=(String)Parser39.parsevalue(data,0,71,2);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser43(){
		byte[] data=new byte[16];
		Parser43.constructor(data,"cmnet",0,16,0);
		String val=(String)Parser43.parsevalue(data,0,16,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser44(){
		byte[] data=new byte[1];
		Parser44.constructor(data,"00111100",0,1,0);
		String val=(String)Parser44.parsevalue(data,0,1,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	
	public void testParser42(){
		byte[] data=new byte[3];
		Parser42.constructor(data,"01,05:32",0,3,0);
		String val=(String)Parser42.parsevalue(data,0,3,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser41(){
		byte[] data=new byte[2];
		Parser41.constructor(data,"01-23",0,2,0);
		String val=(String)Parser41.parsevalue(data,0,2,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser40(){
		byte[] data=new byte[7];
		Parser40.constructor(data,"01-23 03:45,34.006",0,7,0);
		String val=(String)Parser40.parsevalue(data,0,7,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser46(){
		byte[] data=new byte[6];
		Parser46.constructor(data,"FFFFFFFFFFFF",0,6,0);
		String val=(String)Parser46.parsevalue(data,0,6,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}	
	
	public void testParser50(){
		byte[] data=new byte[64];
		Parser50.constructor(data,"11111111,22222222,33333333,44444444,11111111,22222222,33333333,44444444,11111111,22222222,33333333,44444444,11111111,22222222,33333333,44444444",0,64,0);
		String val=(String)Parser50.parsevalue(data,0,64,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	public void testParser51(){
		byte[] data=new byte[7];
		Parser51.constructor(data,"2008-04-09 17:36,12.25",0,7,2);
		String val=(String)Parser51.parsevalue(data,0,7,2);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	public void testParser52(){
		byte[] data=new byte[8];
		Parser52.constructor(data,"2008-04-09 17:36,-112.25",0,8,2);
		String val=(String)Parser52.parsevalue(data,0,8,2);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	public void testParser53(){
		byte[] data=new byte[4];
		Parser53.constructor(data,"17:36,01,2",0,4,0);
		String val=(String)Parser53.parsevalue(data,0,4,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	public void testParser54(){
		byte[] data=new byte[49];
		Parser54.constructor(data,"16,01,11.11,01,22.22,01,33.33,01,44.44,01,11.11,01,22.22,01,33.33,01,44.44,01,11.11,01,22.22,01,33.33,01,44.44,01,11.11,01,22.22,01,33.33,01,44.44",0,49,0);
		String val=(String)Parser54.parsevalue(data,0,49,0);
		//TestCase.assertTrue(data[3]==(byte)0x12);
		TestCase.assertNotNull(val);
	}
	public void testConstruct00(){
		FaalReadForwardDataRequest request=new FaalReadForwardDataRequest();
		request.setTn("1");
		request.setRtuIds(new String[]{"2717866"});
		List paras=new ArrayList();
		FaalRequestParam item=new FaalRequestParam();
		item.setName("B611");
		paras.add(item);
		FaalRequestParam item1=new FaalRequestParam();
		item1.setName("B612");
		paras.add(item1);
		FaalRequestParam item2=new FaalRequestParam();
		item2.setName("B613");
		paras.add(item2);
		FaalRequestParam item3=new FaalRequestParam();
		item3.setName("B621");
		paras.add(item3);
		FaalRequestParam item4=new FaalRequestParam();
		item4.setName("B622");
		paras.add(item4);
		FaalRequestParam item5=new FaalRequestParam();
		item5.setName("B623");
		paras.add(item5);
		
		request.setParams(paras);
		List cmdids=new ArrayList();
		cmdids.add(new Long(0));
		request.setCmdIds(cmdids);
		request.setFixAddre("FF");
		request.setFixPort("01");
		request.setFixProto("20");
		
		RtuManage.getInstance();
		BizRtu rtu=new BizRtu();
		rtu.setRtuId("2");
		rtu.setRtua(0x91000100);
		rtu.setLogicAddress("91000100");
		rtu.setManufacturer("10");
		MeasuredPoint mp=new MeasuredPoint();
		mp.setAtrAddress("01");
		mp.setAtrPort("01");
		mp.setAtrProtocol("20");
		mp.setTn("1");
		mp.setRtuId("2");
		rtu.addMeasuredPoint(mp);

		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		IMessage[] result=ph.createMessage(request);
		TestCase.assertNotNull(result);
	}
	
	public void testConstruct01(){
		FaalReadCurrentDataRequest request=new FaalReadCurrentDataRequest();
		request.setTn(new String[]{"0","04"});
		request.setRtuIds(new String[]{"2"});
		List paras=new ArrayList();
		FaalRequestParam item=new FaalRequestParam();
		item.setName("9010");
		paras.add(item);
		request.setParams(paras);
		List cmdids=new ArrayList();
		cmdids.add(new Long(0));
		request.setCmdIds(cmdids);
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		IMessage[] result=ph.createMessage(request);
		TestCase.assertNotNull(result);
	}
	
	public void testConstruct02(){
		FaalReadTaskDataRequest request=new FaalReadTaskDataRequest();
		request.setCount(10);
		request.setFrequence(1);
		request.setStartTime(new Date());
		request.setTaskNum("01");
		request.setRtuIds(new String[]{"2"});
		List paras=new ArrayList();
		FaalRequestParam item=new FaalRequestParam();
		item.setName("9010");
		paras.add(item);
		request.setParams(paras);
		List cmdids=new ArrayList();
		cmdids.add(new Long(0));
		request.setCmdIds(cmdids);
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		IMessage[] result=ph.createMessage(request);
		TestCase.assertNotNull(result);
	}
	
	public static void testConstruct08(){
		FaalWriteParamsRequest request=new FaalWriteParamsRequest();
		
		request.setRtuIds(new String[]{"9200000000631"});
		request.setTn("0");
		List paras=new ArrayList();
		FaalRequestParam item=new FaalRequestParam();
		item.setName("8015");
		item.setValue("NBDL.ZJ");
		paras.add(item);
		request.setParams(paras);
		List cmdids=new ArrayList();
		cmdids.add(new Long(0));
		request.setCmdIds(cmdids);
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);				
		IMessage[] result=ph.createMessage(request);
		TestCase.assertNotNull(result);
	}
	
	public void testConstruct07(){
		FaalRealTimeWriteParamsRequest request=new FaalRealTimeWriteParamsRequest();
		request.setCmdTime(Calendar.getInstance());
		request.setTimeout(10);
		request.setRtuIds(new String[]{"2"});
		request.setTn("0");
		List paras=new ArrayList();
		FaalRequestParam item=new FaalRequestParam();
		item.setName("8010");
		item.setValue("04,129.123.7.23:9001");
		paras.add(item);
		request.setParams(paras);
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		IMessage[] result=ph.createMessage(request);
		TestCase.assertNotNull(result);
	}
	
	public void testDecoder00(){
		FaalReadForwardDataRequest request=new FaalReadForwardDataRequest();
		request.setTn("1");
		request.setRtuIds(new String[]{"2717866"});
		List paras=new ArrayList();
		FaalRequestParam item=new FaalRequestParam();
		item.setName("B611");
		paras.add(item);
		FaalRequestParam item1=new FaalRequestParam();
		item1.setName("B612");
		paras.add(item1);
		FaalRequestParam item2=new FaalRequestParam();
		item2.setName("B613");
		paras.add(item2);
		FaalRequestParam item3=new FaalRequestParam();
		item3.setName("B621");
		paras.add(item3);
		FaalRequestParam item4=new FaalRequestParam();
		item4.setName("B622");
		paras.add(item4);
		FaalRequestParam item5=new FaalRequestParam();
		item5.setName("B623");
		paras.add(item5);
		
		request.setParams(paras);
		List cmdids=new ArrayList();
		cmdids.add(new Long(0));
		request.setCmdIds(cmdids);
		request.setFixAddre("FF");
		request.setFixPort("01");
		request.setFixProto("20");
		
		byte[] data=new byte[]{0x68,0x1C,0x1C,0x68 
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
		,(byte)0xE8,0x0D};
		
		MessageZjHead hzj=new MessageZjHead();
		hzj.dlen=0x22;
		hzj.c_func=0;
		hzj.c_dir=(byte)0x1;
		hzj.rtua=0x94000013;
		
		MessageZj msgzj=new MessageZj();
		msgzj.head=hzj;
		msgzj.data=ByteBuffer.wrap(data);
		
		ProtocolHandler ph=ProtocolHandlerFactory.getInstance().getProtocolHandler(MessageZj.class);
		Object result=ph.process(msgzj);
		TestCase.assertNotNull(result);
	}
	
	public void testMeterProtocolConfig(){
		MeterProtocolDataSet dataset=MeterProtocolFactory.createMeterProtocolDataSet("ZJMeter");
		TestCase.assertNotNull(dataset);
	}
	
	public void testTimeReform(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date=null;
		try {
			date=sdf.parse("06-4-2 21:4:2");
		} catch (ParseException e) {			
			e.printStackTrace();
		}
		TestCase.assertNotNull(date);
	}
	
}
