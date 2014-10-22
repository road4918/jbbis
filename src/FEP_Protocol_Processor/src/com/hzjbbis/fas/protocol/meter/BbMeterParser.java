package com.hzjbbis.fas.protocol.meter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.fas.protocol.data.DataItem;
import com.hzjbbis.fas.protocol.meter.conf.MeterProtocolDataItem;
import com.hzjbbis.fas.protocol.meter.conf.MeterProtocolDataSet;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	BbMeterParser.java
 * @auther 		netice
 * @date		2006-6-21 15:40:55
 * @version		1.0
 * TODO			部颁表规约
 * 				0x68---------------------帧头标识1
 * 				A0-----------------------地址（低位）
 * 				A1-----------------------地址
 * 				A2-----------------------地址
 * 				A3-----------------------地址
 * 				A4-----------------------地址
 * 				A5-----------------------地址（高位）
 * 				0x68---------------------帧头标识2
 * 				c------------------------控制码 bit7：传输方向，0-命令 1-应答 bit6：异常标识 0-正常 1-异常 
 * 											   bit5：后续帧标识 0-单帧 1-有后续帧
 *                                             bit0-bit4 功能码
 *                                             00000 ：  保留
 *				                               00001 ：  读数据
 *				                               00010 ：  读后续数据
 *				                               00011 ：  重读数据
 *				                               00100 ：  写数据
 *				                               01000 ：  广播校时
 *				                               01010 ：  写设备地址
 *				                               01100 ：  更改通讯速率
 *				                               01111 :   修改密码
 *				                               10000 :   最大需量清零
 *				L-------------------------数据域长度
 *				DATA----------------------数据域 读数据不超过200，写数据不超过50
 *				CS------------------------校验 从帧头标识1到CS前所有字节的和模256
 *				0x16----------------------帧尾
 */
public class BbMeterParser implements IMeterParser{
	private final Log log=LogFactory.getLog(BbMeterParser.class);
	private MeterProtocolDataSet dataset;
	
	public BbMeterParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("BBMeter");
		}catch(Exception e){
			log.error("部颁表规约初始化失败");
		}
	}
	
	/**
	 * 说明：部颁表规约中数据标识和浙江表规约中数据标识一致，浙江表规约中的数据集是
	 *      部颁表规约数据集的一个子集
	 *      所以本函数的转换功能减弱，主要是减少数据标识重复
	 */
	public String[] convertDataKey(String[] datakey) {
		String[] rt=null;
		try{
			if(datakey!=null && datakey.length>0){//有数据标识要转换
				rt=new String[datakey.length];
				for(int i=0;i<datakey.length;i++){
					if((datakey[i]!=null) && datakey[i].equalsIgnoreCase("8902")){
						addDataKey(rt,"C034");
					}else{
						addDataKey(rt,datakey[i]);
					}
				}
			}
		}catch(Exception e){
			log.error("部颁表数据标识转换",e);
		}
		return rt;
	}
	
	/**
	 * 加入未包含数据点key到队列
	 * @param datakeys
	 * @param dkey
	 * 说明：数据标识为XXXX，最多支持召测XXFF数据块，不支持FFFF、XFFF，因为数据太多
	 */
	private void addDataKey(String[] datakeys,String dkey){
		for(int i=0;i<datakeys.length;i++){
			if(datakeys[i]==null || datakeys[i].equals("")){//队列中未包含
				if(dkey.substring(0,1).equalsIgnoreCase("F") || dkey.substring(1,2).equalsIgnoreCase("F")){
					//丢弃，不召测FFFF、XFFF
					break;
				}
				datakeys[i]=dkey;
				break;
			}
			String char1=datakeys[i].substring(0,1);
			String char2=datakeys[i].substring(1,2);
			String char3=datakeys[i].substring(2,3);
			if(char1.equalsIgnoreCase(dkey.substring(0,1))
				&& char2.equalsIgnoreCase(dkey.substring(1,2))
				&& char3.equalsIgnoreCase(dkey.substring(2,3))){//是一个数据块内数据
				
				StringBuffer sb=new StringBuffer();
				sb.append(char1);
				sb.append(char2);
				//小数据块
				sb.append(char3);
				sb.append("F");
				
				datakeys[i]=sb.toString();
				sb=null;
				break;
			}
		}
	}
	
	/**
	 * datakey只接受第一个数据标识，以支持外层组多帧
	 */
	public byte[] constructor(String[] datakey, DataItem para) {
		byte[] frame=null;
		try{
			if((datakey!=null)&&(datakey.length>0)
					&&(para!=null)&&(para.getProperty("point")!=null)){	//check para
				frame=new byte[14];
				String maddr=(String)para.getProperty("point");//表地址
				String dkey=datakey[0];
				frame[0]=0x68;
				ParseTool.HexsToBytesAA(frame,1,maddr,6,(byte)0xAA);				
				frame[7]=0x68;
				frame[8]=0x01;
				frame[9]=0x02;
				ParseTool.HexsToBytes(frame,10,dkey);
				frame[10]=(byte)(frame[10]+0x33);
				frame[11]=(byte)(frame[11]+0x33);
				frame[12]=ParseTool.calculateCS(frame,0,12);	//cs
				frame[13]=0x16;	//cs
			}
		}catch(Exception e){
			
		}
		return frame;
	}

	public Object[] parser(byte[] data, int loc, int len) {
		List result=null;
		try{
			BbMeterFrame frame=new BbMeterFrame();
			frame.parse(data,loc,len);
			if(frame.getDatalen()>0){	//数据中包含表帧且帧中包含数据
				result=new ArrayList();
				//抽取表帧数据
				int datalen=frame.getDatalen();
				String meteraddr=frame.getMeteraddr();	//表地址
				DataItem ma=new DataItem();
				ma.addProperty("value",meteraddr);
				ma.addProperty("datakey","8902");
				result.add(ma);
				
				int ctrl=frame.getCtrl();	/*控制码*/
				if((ctrl & 0x40)<=0){//正常应答
					byte[] framedata=frame.getData();
					
					int pos=frame.getPos();
					switch(ctrl & 0x1F){
						case 1:	//读数据
							String datakey=ParseTool.BytesToHexC(framedata,pos,2);
							MeterProtocolDataItem item=dataset.getDataItem(datakey);
							pos+=2;
							if(item!=null){//支持的数据标识
								parseValues(framedata,pos,item,result);
							}
							break;
						default:
							break;
					}
				}else{
					//异常应答
					
				}
			}
		}catch(Exception e){
			log.error("部颁表规约",e);
		}
		if(result!=null){
			return result.toArray();
		}
		return null;
	}
	
	/**
	 * 解析表数据
	 * @param data		表帧
	 * @param pos		当前解析开始位置
	 * @param item		当前待解析数据项
	 * @param results	结果集
	 */
	private int parseValues(byte[] data,int pos,MeterProtocolDataItem item,List results){
		int rt=0;
		try{
			int loc=pos;
			if(item.getChildarray()!=null && item.getChildarray().size()>0){
				List children=item.getChildarray();
				for(int i=0;i<children.size();i++){
					if((data[loc] & 0xFF)==BbMeterFrame.FLAG_BLOCK_DATA){//数据块结束
						rt+=1;
						break;
					}
					if(loc>=data.length){//无数据可以解析，也没遇到块结束符，理论上应该是错误发生，先忽略
						break;
					}
					int vlen=parseValues(data,loc,(MeterProtocolDataItem)children.get(i),results);
					if(vlen<=0){
						rt=0;
						break;
					}
					loc+=vlen;
					rt+=vlen;
				}
			}else{
				DataItem di=new DataItem();
				di.addProperty("datakey",item.getZjcode());
				Object val=parseItem(data,pos,item);
				di.addProperty("value",val);
				results.add(di);
				rt=item.getLength();
			}
		}catch(Exception e){
			rt=0;
			log.error("解析部颁表数据",e);
		}
		return rt;
	}
	
	/**
	 * 解析表数据项
	 * @param frame
	 * @param loc
	 * @param mpd
	 * @return
	 */
	private Object parseItem(byte[] frame,int loc,MeterProtocolDataItem mpd){
		Object val=DataItemParser.parsevalue(frame,loc,mpd.getLength(),mpd.getFraction(),mpd.getType());
		return val;
	}
}
