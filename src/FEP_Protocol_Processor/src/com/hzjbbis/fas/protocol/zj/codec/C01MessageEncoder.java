package com.hzjbbis.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.MessageEncodeException;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.zj.MessageZjHead;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fas.model.FaalReadCurrentDataRequest;
import com.hzjbbis.fas.model.FaalRequestParam;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.zj.parse.DataItemCoder;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


/**
 * ����ǰ����(�����룺01H)��Ϣ������
 * @author yangdh
 */
public class C01MessageEncoder extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C01MessageEncoder.class);
    /* (non-Javadoc)
     * @see com.hzjbbis.fas.protocol.codec.MessageEncoder#encode(java.lang.Object)
     */
    public IMessage[] encode(Object obj) {        
        List<MessageZj> rt=null;
    	try{	    	
        	if(obj instanceof FaalReadCurrentDataRequest){	//��ȡ��ǰ���ݵ��������
        		FaalReadCurrentDataRequest para=(FaalReadCurrentDataRequest)obj;
        		
        		//ȡ�����㼯��
		        String[] sps=para.getTn();
		        if(sps==null){
		        	throw new MessageEncodeException("δָ��������");
		        }
        		byte[] points=new byte[sps.length];
		        for(int i=0;i<sps.length;i++){
		        	points[i]=Byte.parseByte(sps[i]);
		        }
		        //sps=null;
		        //ȡ�ٲ���������
		        List dks=para.getParams();
		        if(dks==null || dks.size()<=0){
		        	throw new MessageEncodeException("δָ���ٲ�������");
		        }
		        int[] datakeys=new int[dks.size()];
		        int[] itemlen=new int[dks.size()];
		        
		        for(int i=0;i<dks.size();i++){
		        	FaalRequestParam frp=(FaalRequestParam)dks.get(i);
		        	datakeys[i]=ParseTool.HexToDecimal(frp.getName());
		        	try{
		        		itemlen[i]=getDataItemConfig(frp.getName()).getLength();
		        	}catch(Exception e){
		        		throw new MessageEncodeException("�ٲⲻ֧�ֵĲ���--"+frp.getName());
		        	}
		        }
		        dks=null;
		        
		        //��֡
		        int len=datakeys.length*2+8;	//8�ֽ�TNM+2*���������
		        
		        byte[] frame=new byte[len];
		        for(int i=0;i<points.length;i++){	//TNM
		        	int index=0;
		        	int flag=0x01;
		        	index=(points[i] & 0xff)/8;
		        	flag=flag<<((points[i] & 0xff)%8);
		        	frame[index]=(byte)(((frame[index] & 0xff) | flag) & 0xff);
		        }
		        int loc=8;
		        int fdlen=0;	//��֡���ݳ���
		        int ntnum=0;	//���������ø���
		        List<byte[]> titems=new ArrayList<byte[]>();
		        for(int j=0;j<datakeys.length;j++){	//���ݱ�ʶ
		        	if(ParseTool.isTask(datakeys[j])){//��������
		        		titems.add(new byte[]{(byte)(datakeys[j] & 0xff),(byte)((datakeys[j] & 0xff00)>>>8)});
		        	}else{
		        		frame[loc]=(byte)(datakeys[j] & 0xff);		//DI0
			        	frame[loc+1]=(byte)((datakeys[j] & 0xff00)>>>8);	//DI1
			        	loc+=2;
			        	fdlen+=2;
			        	fdlen+=itemlen[j]*sps.length;
			        	ntnum++;
		        	}		        	
		        }
		        
		        List rtuid=para.getRtuIds();
		        if(rtuid==null){
		        	throw new MessageEncodeException("δָ���ٲ��ն�");
		        }
		        List cmdIds = para.getCmdIds();
		        if(cmdIds==null){
		        	throw new MessageEncodeException("����IDȱʧ");
		        }
		        rt=new ArrayList<MessageZj>();
		        for(int i = 0; i < rtuid.size(); i++){
		        	String id=(String) rtuid.get(i);
		        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(id));
		        	if(rtu==null){
		        		log.info("�ն���Ϣδ�ڻ����б�"+id);
		        		continue;
		        	}
		        	//int zonecode=ParseTool.HexToDecimal(rtu.getZoneCode());
		        	int datamax=DataItemCoder.getDataMax(rtu);	//�ն�ÿ֡�������ֵ
		        	
		        	//HostCommand hcmd=new HostCommand();
			        //hcmd.setId((Long)cmdIds.get(i)); 
			        int msgcount=0;
			        
		        	if(titems.size()>0){//�������ٲ�
		        		for(int k=0;k<titems.size();k++){
		        			//֡ͷ����
		        			MessageZjHead head=createHead(rtu);
					        head.dlen=(short)10;
					        
					        byte[] frameA=new byte[10];
					        System.arraycopy(frame,0,frameA,0,8);
					        System.arraycopy((byte[])(titems.get(k)),0,frameA,8,2);
					        MessageZj msg=new MessageZj();
					        //by yangjie ������������������Ϣ,����������������ID
					        msg.setCmdId((Long)cmdIds.get(i));
					        //msg.setAttachment(cmdIds.get(i));
					        
					        msgcount++;
					        msg.data=ByteBuffer.wrap(frameA);
					        msg.head=head;
					        //DataItemCoder.pushNextModule(msg,rtu);
					        rt.add(msg);
		        		}
		        	}
		        	//by yangjie ����ٲⵥ���㽭�����������ֵ����100���·�֡������2008/04/17
		        	if(datamax>=(fdlen+8)||datakeys.length==1){//�����֡
		        		//֡ͷ����
		        		if(ntnum>0){//�з��������������ٲ�
				        	len=8+ntnum*2;
				        	MessageZjHead head=createHead(rtu);
					        head.dlen=(short)len;
					        
					        byte[] frameA=new byte[len];
					        System.arraycopy(frame,0,frameA,0,len);
					        
					        MessageZj msg=new MessageZj();
					        //by yangjie ������������������Ϣ,����������������ID
					        msg.setCmdId((Long)cmdIds.get(i));
					        //msg.setAttachment(hcmd);
					        msgcount++;
					        msg.data=ByteBuffer.wrap(frameA);
					        msg.head=head;
					        //DataItemCoder.pushNextModule(msg,rtu);
					        rt.add(msg);
				      	}
		        	}else{
		        		int dnum=0;
		        		int pos=0;
		        		int curlen=0;
		        		for(int j=0;j<ntnum;j++){
		        			//by yangjie 2008/04/28 ����ٲ�������·�֡���������
		        			dnum+=1;	
		        			curlen+=(2+itemlen[j]*sps.length);
		        			if((curlen+8)>datamax||j==ntnum-1){//����+8�ֽڲ�������				
			        			//��֡
		        				MessageZjHead head=createHead(rtu);			        						        						        		
			        			head.dlen=(short)(8+dnum*2);
			        			
			        			byte[] frameA=new byte[head.dlen];
			        			System.arraycopy(frame,0,frameA,0,8);
			        			System.arraycopy(frame,8+pos*2,frameA,8,head.dlen-8);
						        
						        MessageZj msg=new MessageZj();
						        //by yangjie ������������������Ϣ,����������������ID
						        msg.setCmdId((Long)cmdIds.get(i));						        
						        
						        msgcount++;
						        msg.data=ByteBuffer.wrap(frameA);
						        msg.head=head;
						        rt.add(msg);
						        pos+=dnum;
						        dnum=0;	
						        curlen=0;
			        		}		        					        				        	
			        	}		        		
		        	}
		        	//hcmd.setMessageCount(msgcount);
	        		//ÿ���������ô˴ε��ն���֡����
		        	setMsgcount(rt,msgcount);
		        }
        	}
        }catch(Exception e){
        	throw new MessageEncodeException(e);
        }
        if(rt!=null){
        	IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
        return null;           	
    }
    
    private MessageZjHead createHead(BizRtu rtu){
    	//    	֡ͷ����
    	MessageZjHead head=new MessageZjHead();
        head.c_dir=0;	//��վ�·�
        head.c_expflag=0;	//�쳣��
        head.c_func=(byte)0x01;	//������
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//������ ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//������ ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//�ն˵�ַ
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//֡�����
        //head.fseq		//֡���???????
        //head.msta=	//��վ��ַ?????
        return head;
    }
    
    private ProtocolDataItemConfig getDataItemConfig(String datakey){    	
    	return super.dataConfig.getDataItemConfig(datakey);
    }
	private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
