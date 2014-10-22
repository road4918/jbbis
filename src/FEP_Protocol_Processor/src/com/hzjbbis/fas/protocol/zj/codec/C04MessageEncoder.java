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
import com.hzjbbis.fas.model.FaalReadProgramLogRequest;
import com.hzjbbis.fas.model.FaalRequest;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


public class C04MessageEncoder  extends AbstractMessageEncoder {

	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;
		
		try{
			if(obj instanceof FaalRequest){
				FaalReadProgramLogRequest para=(FaalReadProgramLogRequest)obj;
				
		        Calendar stime;//=Calendar.getInstance();	//�ٲ�ı�����ݿ�ʼʱ��
		        stime=para.getStartTime();
		        if(stime==null){
		        	stime=Calendar.getInstance();
		        }
		        int point=Integer.parseInt(para.getTn());	//�������
		        int num=para.getCount();	//�ٲ�ı����Ŀ��
		        
		        //��֡
		        int len=7;
		        
		        List rtuid=para.getRtuIds();		        
		        rt=new ArrayList<MessageZj>();
		        List cmdIds = para.getCmdIds();
		        for(int iter=0;iter<rtuid.size();iter++){
		        	String id=(String)rtuid.get(iter);
		        	
		        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(id));
		        	
		        	//int zonecode=ParseTool.HexToDecimal(rtu.getZoneCode());
		        	
		        	//֡ͷ����
		        	MessageZjHead head=new MessageZjHead();
			        head.c_dir=0;	//��վ�·�
			        head.c_expflag=0;	//�쳣��
			        head.c_func=(byte)0x04;	//������
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
			        frame[0]=(byte)point;	//������ 0xfe��ʾ���в����� 0xff��ʾ���в�������ն�
			        frame[1]=ParseTool.IntToBcd(stime.get(Calendar.YEAR)%100);	//year
			        frame[2]=ParseTool.IntToBcd(stime.get(Calendar.MONTH)+1);	//month
			        frame[3]=ParseTool.IntToBcd(stime.get(Calendar.DAY_OF_MONTH));	//day
			        frame[4]=ParseTool.IntToBcd(stime.get(Calendar.HOUR_OF_DAY));	//hour
			        frame[5]=ParseTool.IntToBcd(stime.get(Calendar.MINUTE));	//minute	        
			        frame[6]=(byte)num;	//num
			        
			        MessageZj msg=new MessageZj();
			        msg.data=ByteBuffer.wrap(frame);
			        HostCommand hcmd=new HostCommand();
			        hcmd.setId((Long)cmdIds.get(iter)); 
			        hcmd.setMessageCount(1);
			        //by yangjie ������������������Ϣ,����������������ID
			        msg.setCmdId(hcmd.getId());
			        msg.head=head;
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
