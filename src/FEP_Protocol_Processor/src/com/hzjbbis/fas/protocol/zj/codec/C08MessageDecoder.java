package com.hzjbbis.fas.protocol.zj.codec;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.model.HostCommandResult;
import com.hzjbbis.fas.protocol.data.DataMappingZJ;
import com.hzjbbis.fas.protocol.zj.ErrorCode;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * д�������(�����룺08H)��Ӧ��Ϣ������
 * @author yangdinghuan
 *
 */
public class C08MessageDecoder  extends AbstractMessageDecoder{

	public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
		try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��
				int rtype=(ParseTool.getErrCode(message));
				
				if(DataMappingZJ.ERROR_CODE_OK==rtype){		//�ն�����Ӧ��
					hc.setStatus(HostCommand.STATUS_SUCCESS);
					byte[] data=ParseTool.getData(message);	//ȡӦ������
					if(data==null || data.length<=0){
						//��������
						throw new MessageDecodeException("��������");
					}
					int point=data[0];
					int loc=1;
					if(data.length>3){
						toResult(data,loc,point,hc);
					}else{
						//��������
						throw new MessageDecodeException("���ݳ��Ȳ���");
					}
				}else{
					//�쳣Ӧ��
					byte[] data=ParseTool.getData(message);
        			if(data!=null && data.length>0){
        				if(data.length==1){//�ն�ֻ�ش�����
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        				}else{//�ն˻����ݱ�ʶ+������
        					toResult(data,1,data[0],hc);
        				}
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
				}
			}else{
				//��վ�ٲ� Ŀǰ��ǰ�û�֮��ͨ�ţ�����ǰ�û�������
				byte[] data=ParseTool.getData(message);	//ȡ��������
				if((data!=null) && (data.length>0)){
					String code=ParseTool.BytesToHexC(data,5,2);
					if(code.equals("7100") || code.equals("7101") || code.equals("7102")){//ͬ���ն˲���
						//List rtus=(List)Parser39.parsevalue(data,7,0,0);		
					}
				}
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return hc;
	}
	
	/**
	 * �������ý��
	 * @param data
	 * @return
	 */
	private void toResult(byte[] data,int loc,int point,HostCommand hc){
		try{
			int iloc=loc;
			while(iloc<data.length){				
				int datakey=((data[iloc+1] & 0xff)<<8)+(data[iloc] & 0xff);//���ݱ�ʶ
				iloc+=2;				
				String result=ParseTool.ByteToHex(data[iloc]);
				setItemResult(hc,point,ParseTool.IntToHex(datakey),result);
				/*if(!result.equals("00")){
					hc.setStatus(ErrorCode.toHostCommandStatus(data[iloc]));
				}*/
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
