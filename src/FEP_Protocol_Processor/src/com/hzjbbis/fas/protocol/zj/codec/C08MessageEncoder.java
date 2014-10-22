package com.hzjbbis.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hzjbbis.exception.MessageEncodeException;
import com.hzjbbis.fk.message.zj.MessageZjHead;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fas.model.FaalRequestParam;
import com.hzjbbis.fas.model.FaalWriteParamsRequest;
import com.hzjbbis.fas.protocol.conf.ProtocolDataItemConfig;
import com.hzjbbis.fas.protocol.zj.parse.DataItemCoder;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


/**
 * д�������(�����룺04H)��Ϣ������
 * @author yangdinghuan
 *
 */
public class C08MessageEncoder extends AbstractMessageEncoder{
	private static Log log=LogFactory.getLog(C08MessageEncoder.class);
	
	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{

				if(obj instanceof FaalWriteParamsRequest){
					FaalWriteParamsRequest para=(FaalWriteParamsRequest)obj;					
					//��֡
			        int point=Integer.parseInt(para.getTn());	//�������
			        List paras=para.getParams();	//���õ���������� FaalRequestParam����
			        
			        if(paras==null || paras.size()==0){
			        	throw new MessageEncodeException("�����ã���ָ�����ò���");
			        }			        
			        List nparas=paras;		        
			        int[] itemlen=new int[nparas.size()];
			        int[] keysinpara=new int[nparas.size()];
			        String[] valsinpara=new String[nparas.size()];
			        
			        byte[] rowdata=new byte[2048];
			        byte[] rowdataHL=new byte[2048];	//�������⴦��,��APN
			        byte[] rowdataHLi=new byte[2048];	//�������⴦��,��APN
			        
			        int loc=0;
			        
			        rowdata[0]=(byte)point;
			        rowdata[1]=0x11;	//ȡ���Ȩ��
			        
			        rowdataHL[0]=(byte)point;
			        rowdataHL[1]=0x11;	//ȡ���Ȩ��
			        
			        rowdataHLi[0]=(byte)point;
			        rowdataHLi[1]=0x11;	//ȡ���Ȩ��
			        
			        loc=5;
			        int index=0;
			        for(int iter=0;iter<nparas.size();iter++){
			        	FaalRequestParam fp=(FaalRequestParam)nparas.get(iter);
			        	ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(fp.getName());
			        	if(pdc!=null){	//֧�ֵĲ���
		        			rowdata[loc]=(byte)(pdc.getDataKey() & 0xff);
		        			rowdata[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
		        			
		        			rowdataHL[loc]=(byte)(pdc.getDataKey() & 0xff);
		        			rowdataHL[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
		        			
		        			rowdataHLi[loc]=(byte)(pdc.getDataKey() & 0xff);
		        			rowdataHLi[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
		        			
		        			loc+=2;
		        			int dlen=DataItemCoder.coder(rowdata,loc,fp,pdc);
				        	if(dlen<=0){
				        		//����Ĳ���
				        		throw new MessageEncodeException(fp.getName(),("����Ĳ���:"+fp.getName()+"---"+fp.getValue()));
				        	}
				        	
				        	if((pdc.getDataKey() & 0xffff)==0x8015){//apn���⴦��
				        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);	//����
				        		int zi=16;	//��0����
				        		int si=loc+15;
				        		for(int k=0;k<16;k++){
				        			if((rowdata[si] & 0xff)==0x0){
				        				rowdataHL[loc+k]=0;
				        				rowdataHLi[si]=(byte)0xAA;
				        				si--;
				        				zi--;
				        			}else{				        				
				        				break;
				        			}
				        		}
				        		if(zi>0){
				        			System.arraycopy(rowdata,loc,rowdataHL,loc+16-zi,zi);
				        		}
				        	}else if((pdc.getDataKey() & 0xffff)==0x8902){//���ַ
				        		System.arraycopy(rowdata,loc,rowdataHL,loc,dlen);	
				        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);
				        		int zi=6;	//��AA����
				        		int si=loc+5;
				        		for(int k=0;k<6;k++){
				        			if((rowdata[si] & 0xff)==0xAA){
				        				rowdataHLi[si]=0;
				        				si--;
				        				zi--;
				        			}else{				        				
				        				break;
				        			}
				        		}
				        	}else{
				        		System.arraycopy(rowdata,loc,rowdataHL,loc,dlen);
				        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);
				        	}
				        	
				        	itemlen[index]=dlen;
				        	keysinpara[index]=pdc.getDataKey();
				        	valsinpara[index]=fp.getValue();
				        	index++;
		        			loc+=dlen;
			        	}else{
			        		throw new MessageEncodeException(fp.getName(),"�����޷���ȡ�������"+fp.getName());
			        	}
			        }
			        
			        List rtuid=para.getRtuIds();		        
			        List cmdIds = para.getCmdIds();
			        rt=new ArrayList<MessageZj>();
			        for(int iter=0;iter<rtuid.size();iter++){
			        	String id=(String)rtuid.get(iter);
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(id));
			        	byte[] fdata=null;
			        	
			        	if(rtu==null){
			        		log.info("�ն���Ϣδ�ڻ����б�"+id);
			        		continue;
			        	}
			        	//by yangjie ���Ӷ���ʤ�ն˵�APN���⴦�� 2008/06/24
			        	if(rtu.getManufacturer()!=null && (rtu.getManufacturer().equalsIgnoreCase("11") || rtu.getManufacturer().equalsIgnoreCase("18") || rtu.getManufacturer().equalsIgnoreCase("33")|| rtu.getManufacturer().equalsIgnoreCase("27"))){	//��HL
			        		fdata=rowdataHL;
			        	}else if(rtu.getManufacturer()!=null && (rtu.getManufacturer().equalsIgnoreCase("13") || rtu.getManufacturer().equalsIgnoreCase("31"))){
			        		fdata=rowdataHLi;
			        	}else{
			        		fdata=rowdata;
			        	}
			        	
			        	int datamax=DataItemCoder.getDataMax(rtu);	//�ն�ÿ֡�������ֵ
			        	//HostCommand hcmd=new HostCommand();
				        //hcmd.setId((Long)cmdIds.get(iter)); 
				        int msgcount=0;
				        
//			        	if(datamax>=loc){//���÷�֡
//			        		byte[] frame=new byte[loc];
//					        System.arraycopy(rowdata,0,frame,0,loc);
//				        	String pwd=rtu.getHiAuthPassword();
//				        	if(pwd==null){
//				        		log.info("�ն�����ȱʧ,�ն�ID--"+rtu.getId());
//				        		continue;
//				        	}
//				        	ParseTool.StringToBcds(frame,2,pwd);
//				        	
//				        	HeadZj head=createHead(rtu);
//				        	head.dlen=(short)loc;
//				        	
//				        	MessageZj msg=new MessageZj();			        
//					        msg.dataOut=ByteBuffer.wrap(frame);
//					        msg.setAttachment(hcmd);
//					        msgcount++;
//					        msg.headOut=head;
//					        DataItemCoder.pushNextModule(msg,rtu);
//					        rt.add(msg);
//			        	}else{
			        		int dnum=0;
			        		int pos=0;
			        		int curlen=0;
			        		//List parainmsg=new ArrayList();			//�����а�����������
			        		
			        		for(int j=0;j<itemlen.length;j++){
			        			if((curlen+5+(2+itemlen[j]))>datamax){//����+1������+1Ȩ������+3����
			        				MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,cmdIds.get(iter));							        
							        if(msg!=null){							        	
							        	//msg.setAttachment(hcmd);
								        msgcount++;
							        	rt.add(msg);
							        }
			        				pos+=curlen;
							        dnum=1;
							        curlen=2+itemlen[j];
							        
							        //parainmsg.clear();
							        //HostCommandResult hcr=createResult(keysinpara[j],valsinpara[j],point,(Long)cmdIds.get(iter));			        				
			        				//parainmsg.add(hcr);							        
			        			}else{
			        				dnum+=1;
			        				curlen+=(2+itemlen[j]);			        				
			        				//HostCommandResult hcr=createResult(keysinpara[j],valsinpara[j],point,(Long)cmdIds.get(iter));
			        				//parainmsg.add(hcr);
			        				if(keysinpara[j]>0x8100 && keysinpara[j]<=0x81FE){//(��������ز������þͷ�֡����ӦһЩ�ն�ֻ֧��һ����������ÿ֡������)
			        					MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,cmdIds.get(iter));							        
								        if(msg!=null){							        	
								        	//msg.setAttachment(hcmd);
									        msgcount++;
								        	rt.add(msg);
								        }
								        dnum=0;
								        pos+=curlen;
								        curlen=0;
								       // parainmsg.clear();
			        				}
			        			}
			        		}
			        		if(dnum>0){
			        			MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,cmdIds.get(iter));
			        			if(msg!=null){
			        				//msg.setAttachment(hcmd);
							        msgcount++;
			        				rt.add(msg);
			        			}
			        		}
//			        	}
			        	//ÿ���������ô˴ε��ն���֡����
			        	setMsgcount(rt,msgcount);	
			        }
				}
			
		}catch(Exception e){
			//e.printStackTrace();
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
        head.c_func=(byte)0x08;	//������
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//������ ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//������ ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//�ն˵�ַ
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//֡�����
        //head.fseq		//֡���???????
        //head.msta=	//��վ��ַ?????
        return head;
    }
	
	private MessageZj createMessageZj(byte[] rowdata,BizRtu rtu,int pos,int dlen,Object cmdid){
		//��֡
		MessageZjHead head=createHead(rtu);
    	head.dlen=(short)(dlen+5);
    	
    	byte[] frameA=new byte[head.dlen];
		System.arraycopy(rowdata,0,frameA,0,5);
		System.arraycopy(rowdata,5+pos,frameA,5,dlen);
		
		String pwd=rtu.getHiAuthPassword();
		if(pwd==null){
			throw new MessageEncodeException("rtu password missing");
		}
    	ParseTool.HexsToBytesAA(frameA,2,pwd,3,(byte)0xAA);
		
		MessageZj msg=new MessageZj();
		msg.setCmdId((Long)cmdid);
		
        msg.data=ByteBuffer.wrap(frameA);
        msg.head=head;
        return msg;
	}
	
	private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
