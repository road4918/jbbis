package com.hzjbbis.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;


import com.hzjbbis.exception.MessageDecodeException;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.model.HostCommandResult;

import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.data.DataMappingZJ;
import com.hzjbbis.fas.protocol.zj.ErrorCode;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


/**]
 * �������־(�����룺03H)��Ӧ��Ϣ������
 * @author yangdinghuan
 *
 */
public class C04MessageDecoder  extends AbstractMessageDecoder{

	public Object decode(IMessage message) {
		List<HostCommandResult> datas = null;
		HostCommand hc=new HostCommand();
		
        try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��
				int rtype=(ParseTool.getErrCode(message));
				//by yangjie ����������������ID,��Ҫ�����ݿ��ѯ���
				/*HostCommand hcmd=(HostCommand)message.getAttachment();
        		CopyUtil.copyProperties(hc,hcmd);
        		Long cmdid=hcmd.getId();*/
				
				if(DataMappingZJ.ERROR_CODE_OK==rtype){		//�ն�����Ӧ��
					byte[] data=ParseTool.getData(message);	//ȡӦ������
					if((data!=null) && (data.length>17)){	//1�ֽڲ�����+5�ֽڱ��ʱ��+9�ֽ�ͨѶͨ��+����һ�����������ݣ�������3�ֽڣ�
						
						datas=new ArrayList<HostCommandResult>();
						//Long cmdid=(Long)message.getAttachment();	//��վ����ID��just for debug
						//hc.setId(cmdid);
						hc.setStatus(HostCommand.STATUS_SUCCESS);
						int point =ParseTool.BCDToDecimal(data[0]);						
						Calendar optime=ParseTool.getTime(data,1);		//���ʱ��							
						Object comm=DataItemParser.parsevalue(data,6,5,0,14);	//ͨѶͨ��
						
						int loc=15;						
						while(loc<data.length){	//��������
							int datakey=((data[loc+1] & 0xff)<<8)+(data[loc] & 0xff);	//�������ʶ
							loc+=2;							
							ProtocolDataItemConfig dic=getDataItemConfig(datakey);		//����������
							if(dic!=null){
								
								int itemlen=0;
        						if((0x8100<datakey) && (0x81fe>datakey)){//����������
        							int tasktype=(data[loc] & 0xff);	//????���������� ��ͨ �м� �쳣��Ҫ�����ͼ���
        							if(tasktype==DataItemParser.TASK_TYPE_NORMAL){
        								if(16<(data.length-loc)){
	        								itemlen=(data[loc+15] & 0xff)*2+16;	
	        							}else{
	        								throw new MessageDecodeException("֡����̫��");	        								
	        							}
        							}
        							if(tasktype==DataItemParser.TASK_TYPE_RELAY){
        								if(21<(data.length-loc)){
	        								itemlen=(data[loc+20] & 0xff)+21;	
	        							}else{
	        								throw new MessageDecodeException("֡����̫��");
	        							}
        							}
        							if(tasktype==DataItemParser.TASK_TYPE_EXCEPTION){
        								if(7<(data.length-loc)){
	        								itemlen=(data[loc+6] & 0xff)*3+8;	
	        							}else{
	        								throw new MessageDecodeException("֡����̫��");
	        							}
        							}
        						}else{
        							itemlen=dic.getLength();
        						}
								if(itemlen<=(data.length-loc)){
									Object di=DataItemParser.parsevalue(data,loc,itemlen,dic.getFraction(),dic.getParserno());
									
									HostCommandResult hcr=new HostCommandResult();
									hcr.setChannel((String)comm);
									hcr.setCode(dic.getCode());
									hcr.setCommandId(new Long(0));
									hcr.setProgramTime(optime.getTime());
									hcr.setTn(String.valueOf(point));
									if(di!=null){
										hcr.setValue(di.toString());
									}
									datas.add(hcr);
									
									loc+=itemlen;
								}else{
									//����ȱʧ
									throw new MessageDecodeException("֡����̫��");
								}
							}else{
								//�޷�ʶ�������
								throw new MessageDecodeException("δ���õ�������");
							}
						}
						
					}else{
						//��������
						if(data.length>0){
							throw new MessageDecodeException("֡����̫��");
						}else{
							datas=null;	//�յ�����
						}
					}
				}else{
					//�쳣Ӧ��
					byte[] data=ParseTool.getData(message);
        			if(data!=null && data.length>0){
        				hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
				}
			}else{
				//��վ�ٲ�
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		hc.setResults(datas);
		return hc;
	}
		
	/**
	 * ȡ����������
	 * @param datakey
	 * @return
	 */
	private ProtocolDataItemConfig getDataItemConfig(int datakey){    	
    	return super.dataConfig.getDataItemConfig(ParseTool.IntToHex(datakey));
    }
}
