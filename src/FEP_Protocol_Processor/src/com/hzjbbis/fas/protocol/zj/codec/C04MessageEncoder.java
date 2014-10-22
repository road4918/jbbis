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
				
		        Calendar stime;//=Calendar.getInstance();	//召测的编程数据开始时间
		        stime=para.getStartTime();
		        if(stime==null){
		        	stime=Calendar.getInstance();
		        }
		        int point=Integer.parseInt(para.getTn());	//测量点号
		        int num=para.getCount();	//召测的编程项目数
		        
		        //组帧
		        int len=7;
		        
		        List rtuid=para.getRtuIds();		        
		        rt=new ArrayList<MessageZj>();
		        List cmdIds = para.getCmdIds();
		        for(int iter=0;iter<rtuid.size();iter++){
		        	String id=(String)rtuid.get(iter);
		        	
		        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(id));
		        	
		        	//int zonecode=ParseTool.HexToDecimal(rtu.getZoneCode());
		        	
		        	//帧头数据
		        	MessageZjHead head=new MessageZjHead();
			        head.c_dir=0;	//主站下发
			        head.c_expflag=0;	//异常码
			        head.c_func=(byte)0x04;	//功能码
			        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//地市吗 ??????
			        //head.rtua_a2=(byte)(zonecode & 0xff);	//区县码 ??????
			        //head.rtua_b1b2=(short)rtu.getRtua();	//终端地址
			        head.rtua=rtu.getRtua();
			        
			        head.iseq=0;	//帧内序号
			        //head.fseq		//帧序号???????
			        //head.msta=	//主站地址?????
			        head.dlen=(short)len;
			        
			        //head.cs=		//校验??????
			        
			        byte[] frame=new byte[len];
			        frame[0]=(byte)point;	//测量点 0xfe表示所有测量点 0xff表示所有测量点和终端
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
			        //by yangjie 屏蔽下行请求命令信息,增加下行请求命令ID
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
