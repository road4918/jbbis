package com.hzjbbis.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.hzjbbis.fk.message.zj.MessageZjHead;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fas.model.FaalReadAlertRequest;
import com.hzjbbis.fas.model.FaalRequestParam;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


public class C09MessageEncoder extends AbstractMessageEncoder{

	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{
			if(obj instanceof FaalReadAlertRequest){
				FaalReadAlertRequest para=(FaalReadAlertRequest)obj;
				
		        Calendar stime=para.getStartTime();	//�ٲ�����ݿ�ʼʱ�� ????		        
		        int point=Integer.parseInt(para.getTn());	//�������
		        int num=para.getCount();	//�ٲ����Ŀ��
		        int alr=ParseTool.HexToDecimal(((FaalRequestParam)para.getParams().get(0)).getName());	
		        
		        //��֡
		        int len=9;
		        
		        List rtuid=para.getRtuIds();		        
		        List cmdIds = para.getCmdIds();
		        rt=new ArrayList<MessageZj>();
		        for(int iter=0;iter<rtuid.size();iter++){
		        	String id=(String)rtuid.get(iter);
		        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(id));
		        	
		        	//int zonecode=ParseTool.HexToDecimal(rtu.getZoneCode());
		        	
		        	//֡ͷ����
		        	MessageZjHead head=new MessageZjHead();
			        head.c_dir=0;	//��վ�·�
			        head.c_expflag=0;	//�쳣��
			        head.c_func=(byte)0x09;	//������
			        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//������ ??????
			        //head.rtua_a2=(byte)(zonecode & 0xff);	//������ ??????
			        //head.rtua_b1b2=(short)rtu.getRtua();	//�ն˵�ַ
			        head.rtua=rtu.getRtua();
			        
			        head.iseq=0;	//֡�����
			        //head.fseq		//֡���???????
			        //head.msta=	//��վ��ַ?????
			        head.dlen=(short)len;
			        
			        //head.cs=		//У��??????
			        
			        byte[] frame=new byte[len];
			        frame[0]=(byte)point;	//������ 0xFF��ʾ���в�����
			        frame[1]=(byte)(alr & 0xff);
			        frame[2]=(byte)((alr & 0xff00)>>>8);
			        frame[3]=ParseTool.IntToBcd(stime.get(Calendar.YEAR)%100);	//year
			        frame[4]=ParseTool.IntToBcd(stime.get(Calendar.MONTH)+1);	//month
			        frame[5]=ParseTool.IntToBcd(stime.get(Calendar.DAY_OF_MONTH));	//day
			        frame[6]=ParseTool.IntToBcd(stime.get(Calendar.HOUR_OF_DAY));	//hour
			        frame[7]=ParseTool.IntToBcd(stime.get(Calendar.MINUTE));	//minute	        
			        frame[8]=(byte)num;	//num
			        
			        MessageZj msg=new MessageZj();
			        msg.data=ByteBuffer.wrap(frame);
			        //by yangjie ������������������Ϣ,����������������ID 
			        /*HostCommand hc=new HostCommand();	
			        hc.setId((Long)cmdIds.get(iter));
			        hc.setDoUpdate(para.isDoUpdate());	        
			        //msg.setAttachment(hc);*/
			        msg.setCmdId((Long)cmdIds.get(iter));
			        
			        msg.head=head;
			        msg.setMsgCount(1);
			        //DataItemCoder.pushNextModule(msg,rtu);
			        rt.add(msg);
		        }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(rt!=null){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
        return null;  
	}

}
