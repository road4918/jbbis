package com.hzjbbis.fk.bp.processor;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.fk.utils.HexDump;
import com.hzjbbis.fk.bp.model.MessageLog;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.db.batch.event.adapt.BaseLog2DbHandler;

public class Log2DbHandler extends BaseLog2DbHandler {
	private static final Logger log = Logger.getLogger(Log2DbHandler.class);
	
	public void handleLog2Db(AsyncService service,MessageZj msg){
		try{
			MessageLog msgLog=new MessageLog();
			msgLog.setLogicAddress(HexDump.toHex(msg.head.rtua));
			msgLog.setQym(msgLog.getLogicAddress().substring(0,2));
			msgLog.setKzm(Integer.toString(msg.head.c_func,16));	
			if (msgLog.getKzm().equals("21"))//对控制码为21的登录报文特殊处理，避免当做下行报文保存
				msg.head.c_dir=MessageConst.ZJ_DIR_UP;
			msgLog.setTxfs(msg.getTxfs());
			if (msg.getTxfs()!=null){//目前上行才有通讯方式
				if (msg.getTxfs().equals("01")){//UMS
					if (log.isDebugEnabled())
						log.debug("msg.getServerAddress():"+msg.getServerAddress());
					if (msg.getServerAddress()!=null){					
						String[] strList=msg.getServerAddress().split(",");
						if (strList.length>0){
							msgLog.setSrcAddr(strList[0]);//SIM卡号
							msgLog.setDestAddr(strList[1]);//appid+subid
						}
					}
				}
				else
					msgLog.setSrcAddr(msg.getPeerAddr());
			}
			//取本机当前时间作为原始报文通讯时间
			msgLog.setTime(new Date(Calendar.getInstance().getTimeInMillis()));
			msgLog.setBody(msg.getRawPacketString());		
			msgLog.setSize(msgLog.getBody().length());
			if (msg.head.c_dir==MessageConst.ZJ_DIR_UP)//上行报文
				service.addToDao(msgLog,Integer.parseInt("5000"));
			else//下行报文
				service.addToDao(msgLog,Integer.parseInt("5001"));
		}catch(Exception ex){
			log.error("Error to processing message log:"+msg, ex);
		}
		
	}
}
