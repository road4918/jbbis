package com.hzjbbis.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.List;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.model.HostCommandResult;
import com.hzjbbis.fas.protocol.data.DataItem;
import com.hzjbbis.fas.protocol.data.DataMappingZJ;
import com.hzjbbis.fas.protocol.meter.BbMeterFrame;
import com.hzjbbis.fas.protocol.meter.IMeterParser;
import com.hzjbbis.fas.protocol.meter.MeterParserFactory;
import com.hzjbbis.fas.protocol.meter.ZjMeterFrame;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;

/**
 * 读中继(功能码：00H)响应消息编码器
 *
 */
public class C00MessageDecoder  extends AbstractMessageDecoder{
	
	public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
    	List<HostCommandResult> value=new ArrayList<HostCommandResult>();		
		try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答                                      
				int rtype=(ParseTool.getErrCode(message));
				if(DataMappingZJ.ERROR_CODE_OK==rtype){		//终端正常应答
					byte[] data=ParseTool.getData(message);	//取应答数据
					if((data!=null) && (data.length>1)){											
			        	int rtua=((MessageZj)message).head.rtua;
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rtua));
			        	if(rtu==null){
			        		throw new MessageDecodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtua));
			        	}			        	
			        	//获取表规约,因为上行报文不带测量点号,所以解析结果不带测量点号
			        	String pm=getMeterProtocol(data,1,data.length-1);			        	
			        	IMeterParser mparser=MeterParserFactory.getMeterParser(pm);
			        	if(mparser==null){
			        		throw new MessageDecodeException("不支持的表规约："+pm);
			        	}
			        	Object[] dis=mparser.parser(data,1,data.length-1);
			        	if((dis!=null) && (dis.length>0)){		//过滤结果集--方法待改进
			        		for(int i=0;i<dis.length;i++){
			        			DataItem di=(DataItem)dis[i];
			        			String key=(String)di.getProperty("datakey");
			        			if(key==null || key.length()<4){
			        				continue;
			        			}
			        			boolean called=true;
			        			if(called){
			        				HostCommandResult hcr=new HostCommandResult();
	        						hcr.setCode(key);
	        						if(di.getProperty("value")==null){
	        							hcr.setValue(null);
	        						}else{
	        							hcr.setValue(di.getProperty("value").toString());
	        						}	        						
	        						hcr.setCommandId(hc.getId());
	        						value.add(hcr);
			        			}
			        		}
			        	}
			        	hc.setStatus(HostCommand.STATUS_SUCCESS);
			        	hc.setResults(value);
					}else{
						//数据错误						
						hc.setStatus(HostCommand.STATUS_RTU_FAILED);
						hc.setResults(null);
					}
				}else{
					//异常应答帧
					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
					hc.setResults(null);
				}
			}else{
				//主站召测
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}		
		return hc;
	}
		
	private String getMeterProtocol(byte[] data,int loc,int len){
		String Protocol="";
		BbMeterFrame bbFrame=new BbMeterFrame();
		bbFrame.parse(data,loc,len);
		if(bbFrame.getDatalen()>0)
			Protocol="BBMeter";
		else{
			ZjMeterFrame zjFrame=new ZjMeterFrame();
			zjFrame.parse(data, loc, len);
			if (zjFrame.getDatalen()>0){
				Protocol="ZJMeter";
			}
		}
    	return Protocol;
	}
}
