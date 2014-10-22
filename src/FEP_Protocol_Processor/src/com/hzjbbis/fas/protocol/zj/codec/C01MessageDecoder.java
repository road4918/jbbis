package com.hzjbbis.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.model.HostCommandResult;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.data.DataMappingZJ;
import com.hzjbbis.fas.protocol.zj.ErrorCode;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * ����ǰ����(�����룺01H)��Ӧ��Ϣ������
 * @author yangdh
 * 
 */
public class C01MessageDecoder extends AbstractMessageDecoder {	
	private static Log log=LogFactory.getLog(C01MessageDecoder.class);
	/* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageDecoder#decode(com.hzjbbis.fas.framework.IMessage)
     */
    public Object decode(IMessage message) {
    	HostCommand hc=new HostCommand();
    	List<HostCommandResult> value=new ArrayList<HostCommandResult>();
    	try{
    		//RTUReply reply=new RTUReply();
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��
        		//Ӧ������
        		int rtype=(ParseTool.getErrCode(message));
        		
        		//by yangjie ����������������ID,��Ҫ�����ݿ��ѯ���
        		Long cmdid=new Long(0);
        		/*
        		  HostCommand hcmd=(HostCommand)message.getAttachment();      		
        		  CopyUtil.copyProperties(hc,hcmd);
        		  Long cmdid=hcmd.getId();
        		  log.info("��ǰ����� "+hcmd.getMessageCount()+" ֡");	//just for debug
        		*/
        		
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//�����ն�Ӧ��
        			//ȡ��վ�����
        			//Long cmdid=(Long)message.getAttachment();	
        			//hc.setId(cmdid);
        			hc.setStatus(HostCommand.STATUS_SUCCESS);
        			//ȡӦ������
        			byte[] data=ParseTool.getData(message);
	        		if(data!=null && data.length>10){	//�������ݲ���������6��byte��8�ֽ�TNM+����һ�����������ݣ�
	        									
	        			//����Ӧ������
	        			byte points[]=new byte[64];		//����������飨���64�������㣬�������ԼTNM��
	        			byte pn=0;
	        			byte pnum=0;	//���������
	        			for(int i=0;i<8;i++){	//���������
	        				int flag=0x1;
	        				int tnm=(data[i] & 0xff);
	        				for(int j=0;j<8;j++){        					
	        					if((tnm & (flag<<j))>0){
	        						points[pnum]=pn;
	        						pnum++;
	        					}
	        					pn++;
	        				}
	        			}
	        			if(pnum>0){
		        			
	        				int index=8;	//��������������
		        			while(index<data.length){
		        				if(2<(data.length-index)){	//����Ҫ��3�ֽ����ݣ�2�ֽ����ݱ�ʾ+����1�ֽ����ݣ�
		        					int datakey=((data[index+1] & 0xff)<<8)+(data[index] & 0xff); //���ݱ�ʾ��		        					
		        					ProtocolDataItemConfig dic=getDataItemConfig(datakey);
		        					if(dic!=null){
		        						int loc=index+2;
		        						int itemlen=0;

		        						for(int j=0;j<pnum;j++){
		        							itemlen=parseBlockData(data,loc,dic,points[j],cmdid,value);
		        							loc+=itemlen;
		        							if(ParseTool.isTask(datakey)){
		        								loc=data.length;
		        								break;//�߿��ն�ֻ�ܵ����ٲ��������ã����ҷ��ص�����Ϊ��������+�������ݣ�Ŀǰ�򵥴���Ϊ�����ٲ���������
		        							}
		        						}
		        						index=loc;
		        					}else{
		        						//��֧�ֵ�����
		        						//throw new MessageDecodeException("��֧�ֵ�����:"+ParseTool.IntToHex(datakey));	
		        						log.info("��֧�ֵ�����:"+ParseTool.IntToHex(datakey));	
		        						break;	//�߿Ƶ��������ݱȽ����⣬��ʱ����˴���
		        					}
		        				}else{
		        					//����֡����
		        					throw new MessageDecodeException("֡����̫��");	
		        				}
		        			}		        			
	        			}else{
	        				throw new MessageDecodeException("֡���ݴ���δָ��������");
	        			}
        			}else{
        				//throw new MessageDecodeException("֡����̫��");	//��Щ�ն����ٲⲻ֧������ʱ���������֡
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
        		}else{
    				//�쳣Ӧ��֡
        			byte[] data=ParseTool.getData(message);
        			if(data!=null && data.length>0){
        				if(data.length==1){
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        				}else if(data.length==9){
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[8]));
        				}else{
        					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        				}
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
    			}
        	}else{
    			//��վ�ٲ�֡
    			
    		}
        }catch(Exception e){
        	throw new MessageDecodeException(e);
        }
        hc.setResults(value);        
        return hc;
    }
    
    /**
     * ����������
     * @param data		����֡
     * @param loc		������ʼλ��
     * @param pdc		����������
     * @param points	�ٲ�Ĳ���������
     * @param pnum		�ٲ�Ĳ��������
     * @param result	�������
     */
    private int parseBlockData(byte[] data,int loc,ProtocolDataItemConfig pdc,byte point,Long cmdid,List<HostCommandResult> result){
    	int rt=0;
    	try{    		
    		List children=pdc.getChildItems();
    		int index=loc;
    		if((children!=null) && (children.size()>0)){	//���ݿ��ٲ�    			
    			for(int i=0;i<children.size();i++){
    				ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)children.get(i);
    				int dlen=parseBlockData(data,index,cpdc,point,cmdid,result);
    				index+=dlen;
    				rt+=dlen;
    			}    			
    		}else{
    			int dlen=parseItem(data,loc,pdc,point,cmdid,result);
    			rt+=dlen;
    		}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    
    private int parseItem(byte[] data,int loc,ProtocolDataItemConfig pdc,byte point,Long cmdid,List<HostCommandResult> result){
    	int rt=0;
    	try{
    		int datakey=pdc.getDataKey();
    		int itemlen=0;
    		if((0x8100<datakey) && (0x81fe>datakey)){//����������
				int tasktype=(data[loc] & 0xff);	//????���������� ��ͨ �м� �쳣��Ҫ�����ͼ���
				if(tasktype==DataItemParser.TASK_TYPE_NORMAL){
					if(16<(data.length-loc)){
						itemlen=(ParseTool.BCDToDecimal(data[loc+15]))*2+16;	
					}else{
						throw new MessageDecodeException(
								"�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�>16"+" �������ȣ�"+(data.length-loc));
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_RELAY){
					if(21<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+20])+21;	
					}else{
						throw new MessageDecodeException(
								"�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�>21"+" �������ȣ�"+(data.length-loc));
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_EXCEPTION){
					if(7<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+6])*3+8;	
					}else{
						throw new MessageDecodeException(
								"�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�>7"+" �������ȣ�"+(data.length-loc));
					}
				}
			}else{
				itemlen=pdc.getLength();
			}
			if(itemlen<=(data.length-loc)){	//���㹻����				
				Object di=DataItemParser.parsevalue(data,loc,itemlen,pdc.getFraction(),pdc.getParserno());
				HostCommandResult hcr=new HostCommandResult();
				hcr.setCode(pdc.getCode());
				if(di!=null){
					hcr.setValue(di.toString());
				}
				hcr.setCommandId(cmdid);
				hcr.setTn(point+"");
				result.add(hcr);
				rt=itemlen;
			}else{
				//��������
				if((data.length-loc)==0){
					//û�и����ֽڽ������������ն��п����ݲ�ȫ���������ݶ�ʧ
					
				}else{
					throw new MessageDecodeException(
							"�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�"+itemlen+" �������ȣ�"+(data.length-loc));
				}				      							
			}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    
    private ProtocolDataItemConfig getDataItemConfig(int datakey){    	
    	return super.dataConfig.getDataItemConfig(ParseTool.IntToHex(datakey));
    }
}
