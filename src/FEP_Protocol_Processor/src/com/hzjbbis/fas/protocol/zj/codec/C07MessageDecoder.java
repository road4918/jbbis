package com.hzjbbis.fas.protocol.zj.codec;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.model.HostCommandResult;
import com.hzjbbis.fas.protocol.data.DataMappingZJ;
import com.hzjbbis.fas.protocol.zj.ErrorCode;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

public class C07MessageDecoder  extends AbstractMessageDecoder{

	public Object decode(IMessage message) {
		HostCommand hc=null;
		try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
				int rtype=(ParseTool.getErrCode(message));
				hc= new HostCommand();
				if(DataMappingZJ.ERROR_CODE_OK==rtype){		//终端正常应答
					hc.setStatus(HostCommand.STATUS_SUCCESS);
					byte[] data=ParseTool.getData(message);	//取应答数据
					int point=data[0];
					int loc=1;
					if(data.length>3){
						toResult(data,loc,point,hc);
					}else{
						//错误数据
						throw new MessageDecodeException("数据长度不对");
					}
				}else{
					//异常应答
					byte[] data=ParseTool.getData(message);
					if(data!=null && data.length>0){
        				if(data.length==1){//终端只回错误码
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        				}else{//终端回数据标识+错误码
        					toResult(data,1,data[0],hc);
        				}
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
				}
			}else{
				//主站召测
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return hc;
	}
	
	
	/**
	 * 返回设置结果
	 * @param data
	 * @return
	 */
	private void toResult(byte[] data,int loc,int point,HostCommand hc){
		
		try{
			int iloc=loc;
			while(iloc<data.length){				
				int datakey=((data[iloc+1] & 0xff)<<8)+(data[iloc] & 0xff);//数据标识
				iloc+=2;				
				String result=ParseTool.ByteToHex(data[iloc]);
				setItemResult(hc,point,ParseTool.IntToHex(datakey),result);		
				iloc+=1;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void setItemResult(HostCommand hc,int point,String code,String result){
		HostCommandResult hcr=new HostCommandResult();
		hcr.setTn(""+point);
		hcr.setCode(code);
		hcr.setValue(result);
		hc.addResult(hcr);		
	}
}
