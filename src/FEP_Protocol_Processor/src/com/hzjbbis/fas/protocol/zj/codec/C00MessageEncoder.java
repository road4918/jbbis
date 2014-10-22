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
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fas.model.FaalReadForwardDataRequest;
import com.hzjbbis.fas.model.FaalRequestParam;
import com.hzjbbis.fas.model.HostCommand;
import com.hzjbbis.fas.protocol.data.DataItem;
import com.hzjbbis.fas.protocol.meter.IMeterParser;
import com.hzjbbis.fas.protocol.meter.MeterParserFactory;
import com.hzjbbis.fas.protocol.zj.parse.ParseTool;


public class C00MessageEncoder  extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C00MessageEncoder.class);
	
	public IMessage[] encode(Object obj) {		
		List<MessageZj> rt=new ArrayList<MessageZj>();		
		try{
			if(obj instanceof FaalReadForwardDataRequest){
				FaalReadForwardDataRequest para=(FaalReadForwardDataRequest)obj;
				
				//组帧	        
		        int waittime=para.getTimeout();	//超时时间?????
		        byte character=(byte)0x0;	//截取用特征字 0x00表示不考虑特征字截取
		        int cutindex=0;		//截取开始位置,0表示第一个字节
		        int cutlen=0;	//截取长度，0表示不截取，全部接收        
		        		        
		        List dkeys=para.getParams();	//本次召测的数据项（用浙江规约数据标识）
		        String tn=para.getTn();
		        if(dkeys!=null){
			        //get what data will be called
		        	String[] datakeyzj=new String[dkeys.size()];			        
			        for(int index=0;index<dkeys.size();index++){
			        	FaalRequestParam frp=(FaalRequestParam)dkeys.get(index);
			        	datakeyzj[index]=frp.getName();
			        }
		        	//byte[] cmd=new byte[(cmdstring.length()>>>1)+(cmdstring.length() & 0x1)];	
			        
			        //int len=7+cmd.length;
			        
			        List rtuid=para.getRtuIds();
			        if(rtuid==null){
			        	throw new MessageEncodeException("未指定召测终端");
			        }
			        List cmdIds = para.getCmdIds();
			        if(cmdIds==null){
			        	throw new MessageEncodeException("命令ID缺失");
			        }
		        
			        for(int i = 0; i < rtuid.size(); i++){
			        	createRtuFrame(rt, para, waittime, character, cutindex, cutlen, tn, datakeyzj, rtuid, cmdIds, i);
			        }
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

	private void createRtuFrame(List<MessageZj> rt, FaalReadForwardDataRequest para, int waittime, byte character, int cutindex, int cutlen, String tn, String[] datakeyzj, List rtuid, List cmdIds, int i) {
		//int i=0;		//one a time
		try{
			MessageZj msg=null;
			String id=(String) rtuid.get(i);
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(id));
			if(rtu==null){
				throw new MessageEncodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtu.getRtua()));
			}
			
			IMeterParser mparser=null;
			String pm=null;
			String maddr=null;
			String portstr=null;
			
			if(para.getFixProto()==null){			
				pm=getProtoWithMpPara(rtu,tn);			
			}else{
				pm=ParseTool.getMeterProtocol(para.getFixProto());			
			}
			if(para.getFixAddre()==null){			
				maddr=getAddrWithMpPara(rtu,tn,para);
			}else{			
				maddr=para.getFixAddre();
			}
			if(para.getFixPort()==null){
				portstr=getPortWithMpPara(rtu,tn);
			}else{
				portstr=para.getFixPort();
			}
			
			mparser=MeterParserFactory.getMeterParser(pm);
			if(mparser==null){
				throw new MessageEncodeException("不支持的表规约");
			}
			
			if(maddr==null){
				throw new MessageEncodeException("测量点地址缺失！");
			}else{
				//华立的特殊地址
				if(pm.equalsIgnoreCase(ParseTool.METER_PROTOCOL_ZJ)){
					if(maddr.length()>2){
						String xxa=maddr.substring(maddr.length()-2);			        				
						if(xxa.equalsIgnoreCase("AA")){
							//错误
							maddr=maddr.substring(0,2);
						}else{
							maddr=xxa;
						}
					}
				}
			}
					
			DataItem dipara=new DataItem();
			dipara.addProperty("point",maddr);
			String[] dks=mparser.convertDataKey(datakeyzj);
			
			/*HostCommand hc=new HostCommand();
			hc.setId((Long)cmdIds.get(i));*/
			int msgcount=0;
			
			for(int k=0;k<dks.length;k++){
				if(dks[k]==null || dks[k].length()<=0){
					break;
				}
				byte[] cmd=mparser.constructor(new String[]{dks[k]},dipara);
				if(cmd==null){
					StringBuffer se=new StringBuffer();
					for(int j=0;j<datakeyzj.length;j++){
						se.append(datakeyzj[j]);
						se.append(" ");
					}
					throw new MessageEncodeException("不支持召测的表规约数据："+se.toString()+"  RTU:"+ParseTool.IntToHex4(rtu.getRtua()));
				}
				int len=cmd.length+7;
				
				//帧头数据
				MessageZjHead head=new MessageZjHead();
			    head.c_dir=0;	//主站下发
			    head.c_expflag=0;	//异常码
			    head.c_func=(byte)0x00;	//功能码
			    head.rtua=rtu.getRtua();
			    head.iseq=0;	//帧内序号
			    head.dlen=(short)len;
			    
			    int port=1;	//默认值
			    if(portstr!=null){
			    	port=Integer.parseInt(portstr);
			    }
			    byte[] frame=new byte[len];
			    frame[0]=(byte)port;
			    frame[1]=(byte)waittime;
			    frame[2]=character;
			    frame[3]=(byte)(cutindex & 0xff);
			    frame[4]=(byte)((cutindex & 0xff00)>>>8);
			    frame[5]=(byte)(cutlen & 0xff);
			    frame[6]=(byte)((cutlen & 0xff00)>>>8);
			    System.arraycopy(cmd,0,frame,7,cmd.length);
			    
			    msg=new MessageZj();
			    msg.data=ByteBuffer.wrap(frame);
			    
			    //List paralist=new ArrayList();
			    //paralist.add(para);
			    //hc.setResults(paralist);
			    
			    //hc.setRequest(para);
			    //by yangjie 屏蔽下行请求命令信息,增加下行请求命令ID
			    msg.setCmdId((Long)cmdIds.get(i));
			    //msg.setAttachment(hc);				        
			    msg.head=head;
			    //DataItemCoder.pushNextModule(msg,rtu);
			    rt.add(msg);
			    msgcount++;
			}
			//hc.setMessageCount(msgcount);
        	//每个报文设置此次单终端组帧总数
        	setMsgcount(rt,msgcount);
		}catch(Exception e){
			//
			try{
				MessageZj msg=new MessageZj();
				HostCommand hc=new HostCommand();
				hc.setId((Long)cmdIds.get(i));
				msg.setCmdId((Long)cmdIds.get(i));
				msg.setStatus(HostCommand.STATUS_PARA_INVALID);
				rt.add(msg);
			}catch(Exception ex){
				//
			}
		}
	}
	
	private String getProtoWithMpPara (BizRtu rtu,String tn){
		String proto=null;
		
		MeasuredPoint mp=rtu.getMeasuredPoint(tn);
		if(mp==null){
			throw new MessageEncodeException("指定测量点不存在！终端--"+ParseTool.IntToHex4(rtu.getRtua())+"  测量点--"+tn);
		}
		
		if(mp.getAtrProtocol()==null){
			log.error("表规约缺失，将使用默认规约类型------RTU:"+ParseTool.IntToHex4(rtu.getRtua()));
			proto=ParseTool.getMeterProtocol("20");
		}else{
			proto=ParseTool.getMeterProtocol(mp.getAtrProtocol());
		}
		
		return proto;
	}
	
	private String getPortWithMpPara(BizRtu rtu,String tn){
		String port=null;
		MeasuredPoint mp=rtu.getMeasuredPoint(tn);
		if(mp==null){
			throw new MessageEncodeException("指定测量点不存在！终端--"+ParseTool.IntToHex4(rtu.getRtua())+"  测量点--"+tn);
		}
		port=mp.getAtrPort();
		return port;
	}
	
	private String getAddrWithMpPara(BizRtu rtu,String tn,FaalReadForwardDataRequest para){
		String maddr=null;		
		if(para.isBroadcast()){			
			if(para.getBroadcastAddress()!=null){
				maddr=para.getBroadcastAddress();
			}else{
				MeasuredPoint mp=rtu.getMeasuredPoint(tn);
				if(mp==null){
					throw new MessageEncodeException("指定测量点不存在！终端--"+ParseTool.IntToHex4(rtu.getRtua())+"  测量点--"+tn);
				}
				maddr=getBroadcastAddress(mp);
			}			
		}else{			
			MeasuredPoint mp=rtu.getMeasuredPoint(tn);
			if(mp==null){
				throw new MessageEncodeException("指定测量点不存在！终端--"+ParseTool.IntToHex4(rtu.getRtua())+"  测量点--"+tn);
			}
			if(mp.getAtrAddress()==null){
				maddr=getBroadcastAddress(mp);
			}else{
				maddr=mp.getAtrAddress();
			}							
		}
		return maddr;
	}
	
	private String getBroadcastAddress(MeasuredPoint mp){
		String maddr=null;
		if(mp.getAtrProtocol()==null){
			maddr="FF";	//默认浙规
		}else{
			if(mp.getAtrProtocol().equalsIgnoreCase(ParseTool.METER_PROTOCOL_BB)){
				maddr="999999999999";
			}
			if(mp.getAtrProtocol().equalsIgnoreCase(ParseTool.METER_PROTOCOL_ZJ)){
				maddr="FF";
			}
			if(mp.getAtrProtocol().equalsIgnoreCase(ParseTool.METER_PROTOCOL_SM)){//西门子不要表地址
				maddr="FF";
			}
		}
		return maddr;
	}
    private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
