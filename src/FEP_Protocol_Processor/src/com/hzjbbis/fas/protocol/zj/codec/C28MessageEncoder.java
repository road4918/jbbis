package com.hzjbbis.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.MessageEncodeException;
import com.hzjbbis.fk.message.zj.MessageZjHead;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.FaalSendSmsRequest;
import com.hzjbbis.fas.protocol.zj.parse.Parser43;

/**
 * @filename	C28MessageCoder.java
 * @auther 		netice
 * @date		2006-4-14 9:00:35
 * @version		1.0
 * TODO			�����Ͷ��ŷ���֡��֡
 */
public class C28MessageEncoder extends AbstractMessageEncoder{
	private static Log log=LogFactory.getLog(C28MessageEncoder.class);
	
	public IMessage[] encode(Object obj){
		List<MessageZj> rt=null;
		try{
    		if(obj instanceof FaalSendSmsRequest){
    			FaalSendSmsRequest para=(FaalSendSmsRequest)obj;
    			String content=para.getContent();	//��������
    			if(content==null || content.length()==0 ){
    				//����Ĳ���
        			throw new MessageEncodeException("��ָ����������");
    			}
    			//byte[] bcont=content.getBytes("UNICODE");
    			byte[] bcont=content.getBytes("GB2312");
    			exchangeBytes(bcont,1);	//?????? ����ֻ�и߿�ǰ�û�������Ҫ����(�߿Ʊ���ΪUNICODE,Ҫ����һ�ַ�����������ֽڽ����ߵ�λ�ã���¡ΪGBK���룬Ҫ���������ı�������)
    			String[] phones=para.getMobiles();
    			if((phones!=null) && (phones.length>0)){
    				rt=new ArrayList<MessageZj>();    				
    				int len=14+bcont.length;
    				if(bcont.length>(content.length()*2)){
		        		len-=2;
		        	}                   
    				for(int i=0;i<phones.length;i++){
    					//֡ͷ����
    					MessageZjHead head=new MessageZjHead();
    			        head.c_dir=0;	//��վ�·�
    			        head.c_expflag=0;	//�쳣��
    			        head.c_func=(byte)0x28;	//������
    			        
    			        head.rtua_a1=(byte)0x92;//ParseTool.StringToBcd(FasConfig.getConfig().getModuleConfig("fe-module").getFrontEndConfig().getZoneCode().substring(0,2));	//get a1
    			        head.rtua_a2=0;
    			        head.rtua_b1b2=0x1E00;	//0x001E��ǰ�û�
                        
    			        head.iseq=0;	//֡�����
    			        //head.fseq		//֡���???????
    			        //head.msta=	//��վ��ַ?????
    			        head.dlen=(short)len;
    			        
    			        byte[] frame=new byte[len];    			       
    			        int dlen=Parser43.constructor(frame,phones[i],0,14,0);	//phone
			        	if(dlen<=0){
			        		continue;
			        	}
			        	
			        	if(bcont.length>(content.length()*2)){
			        		System.arraycopy(bcont,2,frame,14,bcont.length-2);	//content
			        	}else{
			        		System.arraycopy(bcont,0,frame,14,bcont.length);	//content
			        	}
			        				        	
			        	MessageZj msg=new MessageZj();			        
				        msg.data=ByteBuffer.wrap(frame);
				        msg.head=head;
				        rt.add(msg);
				        if(log.isDebugEnabled()){
				        	log.debug(content+" to "+phones[i]);
				        }
    				}
    			}else{
    				//����Ĳ���
        			throw new MessageEncodeException("��ָ�����ŷ��Ͷ�����ֻ�����");
    			}
    		}else{
    			//����Ĳ���
    			throw new MessageEncodeException("����Ĳ���������ʹ�ã�FaalSendSmsRequest");
    		}
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		
		if(rt!=null && rt.size()>0){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
		return null;
	}
	
	private void exchangeBytes(byte[] data){
		int i=0;
		while(i<data.length){
			//exchange i and i+1
			byte cc=data[i];
			data[i]=data[i+1];
			data[i+1]=cc;
			i+=2;
		}
	}
	
	private void exchangeBytes(byte[] data,int type){
		if(type==0){
			exchangeBytes(data);
		}else{
			int i=0;
			int j=data.length-1;
			while(i<(data.length/2)){
				//exchange i and i+1
				byte cc=data[i];
				data[i]=data[j];
				data[j]=cc;
				i++;
				j--;
			}
		}
	}	
}
