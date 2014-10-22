package com.hzjbbis.fas.protocol.zj.viewer;

import java.util.List;

import com.hzjbbis.exception.MessageDecodeException;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.zj.parse.DataItemParser;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	FrameC07.java
 * @auther 		yangdh
 * @date		2006-8-2 15:11:57
 * @version		1.0
 * TODO
 */
public class FrameC07 extends AbstractFrame{
	public static final String FUNC_NAME="ʵʱ�����ն˲���";
	public FrameC07(){
		//
	}
	
	public FrameC07(byte[] frame){
		super(frame);
	}
	
	public FrameC07(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("��������--").append(FUNC_NAME);
			sb.append("\n");
			if(direction>0){	//�ն�Ӧ��
				descRtuReply(sb);
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			if(fexp>0){
				if(length>1){
					parseErr(buffer);
				}else{
					buffer.append("�쳣Ӧ��--").append(errCode(frame[11]));					
				}
			}else{
				parseErr(buffer);				
			}
		}catch(Exception e){
			//
		}
	}
	
	private void parseErr(StringBuffer buffer){
		buffer.append("���õĲ�����--");
		int point=frame[11] & 0xFF;
		buffer.append(point).append("\n");
		
		int index=12;
		int tail=length+11;
		
		while(index<tail){
			if(2<(tail-index)){
				buffer.append(ParseTool.BytesToHexC(frame, index, 2));
				buffer.append("���ý��:").append(errCode(frame[index+2])).append("\n");
				index+=3;
			}else{
				break;
			}
		}
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("���õĲ�����--");
			int point=frame[11] & 0xFF;
			buffer.append(point).append("    ");
			buffer.append("ʹ�õ�Ȩ�޵ȼ�--");
			buffer.append((frame[12] & 0xFF)==0x11?"�߼�":"�ͼ�").append("    ");
			buffer.append("����--");
			buffer.append(ParseTool.BytesToHexC(frame, 13, 3)).append("\n");
			buffer.append("�����·�ʱ��---");
			buffer.append("20").append(Util.ByteToHex(frame[16])).append("-").append(Util.ByteToHex(frame[17]))
					.append("-").append(Util.ByteToHex(frame[18])).append(" ").append(Util.ByteToHex(frame[19]))
					.append(":").append(Util.ByteToHex(frame[20])).append(":00    ");
			buffer.append("������Чʱ��---").append(Util.ByteToHex(frame[21])).append("min\n");
			buffer.append("���õ�������---");
			int index=22;
			int tail=length+11;	
			
			while(index<tail){
				if(2<(tail-index)){	//����Ҫ��3�ֽ����ݣ�2�ֽ����ݱ�ʾ+����1�ֽ����ݣ�
					int datakey=((frame[index+1] & 0xff)<<8)+(frame[index] & 0xff); //���ݱ�ʾ��
					ProtocolDataItemConfig dic=DataConfigZj.getInstance().getDataConfig().getDataItemConfig(ParseTool.IntToHex(datakey));
					if(dic==null){
						ProtocolDataItemConfig di=DataConfigZjpb.getInstance().getDataConfig().getDataItemConfig(ParseTool.IntToHex(datakey));
						if(di!=null)
							dic = di;
					}
					if(dic!=null){
						int loc=index+2;
						int itemlen=0;
						
						itemlen=parseBlockData(frame,loc,dic,point,buffer);
						loc+=itemlen;
						if(ParseTool.isTask(datakey)){
							loc=tail;
							break;//�߿��ն�ֻ�ܵ����ٲ��������ã����ҷ��ص�����Ϊ��������+�������ݣ�Ŀǰ�򵥴���Ϊ�����ٲ���������
						}
						
						index=loc;
					}else{
						//��֧�ֵ�����						
						buffer.append("\n");
						buffer.append("��֧�ֵ�����:"+ParseTool.IntToHex(datakey));	
						break;	//�߿Ƶ��������ݱȽ����⣬��ʱ����˴���
					}
				}else{
					break;
				}
			}
		}catch(Exception e){
			//
		}
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
    private int parseBlockData(byte[] data,int loc,ProtocolDataItemConfig pdc,int point,StringBuffer buffer){
    	int rt=0;
    	try{    		
    		List children=pdc.getChildItems();
    		int index=loc;
    		if((children!=null) && (children.size()>0)){	//���ݿ��ٲ�    			
    			for(int i=0;i<children.size();i++){
    				ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)children.get(i);
    				int dlen=parseBlockData(data,index,cpdc,point,buffer);
    				if(dlen<=0){
    					return -1;
    				}
    				index+=dlen;
    				rt+=dlen;
    			}    			
    		}else{
    			int dlen=parseItem(data,loc,pdc,point,buffer);
    			if(dlen<=0){
					return -1;
				}
    			rt+=dlen;
    		}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    
    private int parseItem(byte[] data,int loc,ProtocolDataItemConfig pdc,int point,StringBuffer buffer){
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
						buffer.append("�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�>16"+" �������ȣ�"+(data.length-loc));
						return -1;
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_RELAY){
					if(21<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+20])+21;	
					}else{
						buffer.append("�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�>21"+" �������ȣ�"+(data.length-loc));
						return -1;
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_EXCEPTION){
					if(7<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+6])*3+8;	
					}else{
						buffer.append("�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�>7"+" �������ȣ�"+(data.length-loc));
						return -1;
					}
				}
			}else{
				itemlen=pdc.getLength();
			}
			if(itemlen<=(data.length-loc)){	//���㹻����				
				Object di=DataItemParser.parsevalue(data,loc,itemlen,pdc.getFraction(),pdc.getParserno());				
				buffer.append(pdc.getCode()).append("=");				
				if(di!=null){
					buffer.append(di.toString());
				}
				buffer.append("\n");				
				rt=itemlen;
			}else{
				//��������
				if((data.length-loc)==0){
					//û�и����ֽڽ������������ն��п����ݲ�ȫ���������ݶ�ʧ
					
				}else{
					buffer.append("�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�"+itemlen+" �������ȣ�"+(data.length-loc));
					return -1;
				}				      							
			}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
}
