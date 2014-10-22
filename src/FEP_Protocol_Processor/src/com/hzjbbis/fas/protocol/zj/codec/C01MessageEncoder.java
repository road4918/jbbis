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
 * 读当前数据(功能码：01H)消息编码器
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
        	if(obj instanceof FaalReadCurrentDataRequest){	//读取当前数据的请求对象
        		FaalReadCurrentDataRequest para=(FaalReadCurrentDataRequest)obj;
        		
        		//取测量点集合
		        String[] sps=para.getTn();
		        if(sps==null){
		        	throw new MessageEncodeException("未指定测量点");
		        }
        		byte[] points=new byte[sps.length];
		        for(int i=0;i<sps.length;i++){
		        	points[i]=Byte.parseByte(sps[i]);
		        }
		        //sps=null;
		        //取召测的数据项集合
		        List dks=para.getParams();
		        if(dks==null || dks.size()<=0){
		        	throw new MessageEncodeException("未指定召测数据项");
		        }
		        int[] datakeys=new int[dks.size()];
		        int[] itemlen=new int[dks.size()];
		        
		        for(int i=0;i<dks.size();i++){
		        	FaalRequestParam frp=(FaalRequestParam)dks.get(i);
		        	datakeys[i]=ParseTool.HexToDecimal(frp.getName());
		        	try{
		        		itemlen[i]=getDataItemConfig(frp.getName()).getLength();
		        	}catch(Exception e){
		        		throw new MessageEncodeException("召测不支持的参数--"+frp.getName());
		        	}
		        }
		        dks=null;
		        
		        //组帧
		        int len=datakeys.length*2+8;	//8字节TNM+2*数据项个数
		        
		        byte[] frame=new byte[len];
		        for(int i=0;i<points.length;i++){	//TNM
		        	int index=0;
		        	int flag=0x01;
		        	index=(points[i] & 0xff)/8;
		        	flag=flag<<((points[i] & 0xff)%8);
		        	frame[index]=(byte)(((frame[index] & 0xff) | flag) & 0xff);
		        }
		        int loc=8;
		        int fdlen=0;	//回帧数据长度
		        int ntnum=0;	//非任务配置个数
		        List<byte[]> titems=new ArrayList<byte[]>();
		        for(int j=0;j<datakeys.length;j++){	//数据标识
		        	if(ParseTool.isTask(datakeys[j])){//任务配置
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
		        	throw new MessageEncodeException("未指定召测终端");
		        }
		        List cmdIds = para.getCmdIds();
		        if(cmdIds==null){
		        	throw new MessageEncodeException("命令ID缺失");
		        }
		        rt=new ArrayList<MessageZj>();
		        for(int i = 0; i < rtuid.size(); i++){
		        	String id=(String) rtuid.get(i);
		        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(id));
		        	if(rtu==null){
		        		log.info("终端信息未在缓存列表："+id);
		        		continue;
		        	}
		        	//int zonecode=ParseTool.HexToDecimal(rtu.getZoneCode());
		        	int datamax=DataItemCoder.getDataMax(rtu);	//终端每帧数据最大值
		        	
		        	//HostCommand hcmd=new HostCommand();
			        //hcmd.setId((Long)cmdIds.get(i)); 
			        int msgcount=0;
			        
		        	if(titems.size()>0){//有任务召测
		        		for(int k=0;k<titems.size();k++){
		        			//帧头数据
		        			MessageZjHead head=createHead(rtu);
					        head.dlen=(short)10;
					        
					        byte[] frameA=new byte[10];
					        System.arraycopy(frame,0,frameA,0,8);
					        System.arraycopy((byte[])(titems.get(k)),0,frameA,8,2);
					        MessageZj msg=new MessageZj();
					        //by yangjie 屏蔽下行请求命令信息,增加下行请求命令ID
					        msg.setCmdId((Long)cmdIds.get(i));
					        //msg.setAttachment(cmdIds.get(i));
					        
					        msgcount++;
					        msg.data=ByteBuffer.wrap(frameA);
					        msg.head=head;
					        //DataItemCoder.pushNextModule(msg,rtu);
					        rt.add(msg);
		        		}
		        	}
		        	//by yangjie 解决召测单个浙江配变数据项数值超过100导致分帧的问题2008/04/17
		        	if(datamax>=(fdlen+8)||datakeys.length==1){//无须分帧
		        		//帧头数据
		        		if(ntnum>0){//有非任务配置数据召测
				        	len=8+ntnum*2;
				        	MessageZjHead head=createHead(rtu);
					        head.dlen=(short)len;
					        
					        byte[] frameA=new byte[len];
					        System.arraycopy(frame,0,frameA,0,len);
					        
					        MessageZj msg=new MessageZj();
					        //by yangjie 屏蔽下行请求命令信息,增加下行请求命令ID
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
		        			//by yangjie 2008/04/28 解决召测多个命令导致分帧错误的问题
		        			dnum+=1;	
		        			curlen+=(2+itemlen[j]*sps.length);
		        			if((curlen+8)>datamax||j==ntnum-1){//数据+8字节测量点组				
			        			//组帧
		        				MessageZjHead head=createHead(rtu);			        						        						        		
			        			head.dlen=(short)(8+dnum*2);
			        			
			        			byte[] frameA=new byte[head.dlen];
			        			System.arraycopy(frame,0,frameA,0,8);
			        			System.arraycopy(frame,8+pos*2,frameA,8,head.dlen-8);
						        
						        MessageZj msg=new MessageZj();
						        //by yangjie 屏蔽下行请求命令信息,增加下行请求命令ID
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
	        		//每个报文设置此次单终端组帧总数
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
    	//    	帧头数据
    	MessageZjHead head=new MessageZjHead();
        head.c_dir=0;	//主站下发
        head.c_expflag=0;	//异常码
        head.c_func=(byte)0x01;	//功能码
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//地市吗 ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//区县码 ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//终端地址
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//帧内序号
        //head.fseq		//帧序号???????
        //head.msta=	//主站地址?????
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
