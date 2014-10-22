package com.hzjbbis.fk.fe.rawmsg2db;

import java.util.Date;

import org.apache.log4j.Logger;

import com.hzjbbis.db.batch.AsyncService;
import com.hzjbbis.db.batch.event.adapt.BaseLog2DbHandler;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.utils.HexDump;

public class RawMessage2DbHandler extends BaseLog2DbHandler {
	private static final Logger log = Logger.getLogger(RawMessage2DbHandler.class);

	@Override
	public void handleLog2Db(AsyncService service, MessageZj msg) {
		try{
			MessageLog msgLog=new MessageLog();
			msgLog.setLogicAddress(HexDump.toHex(msg.head.rtua));
			msgLog.setQym(msgLog.getLogicAddress().substring(0,2));
			msgLog.setKzm(HexDump.toHex(msg.head.c_func));

			if ( MessageConst.ZJ_FUNC_LOGIN == msg.head.c_func ) 	//对控制码为21的登录报文特殊处理，避免当做下行报文保存
				msg.head.c_dir = MessageConst.ZJ_DIR_UP;
			msgLog.setTxfs(msg.getTxfs());
			if( "01".equals(msg.getTxfs())){
				//UMS通道的消息
				boolean hasServerAddr = false;
				if( null != msg.getServerAddress() ){
					int index = msg.getServerAddress().indexOf(',');
					if( index>0 ){
						msgLog.setSrcAddr(msg.getServerAddress().substring(0, index));	//SIM卡号
						msgLog.setDestAddr(msg.getServerAddress().substring(index+1));  //appid+subid
						hasServerAddr = true;
					}
				}
				if( ! hasServerAddr )
					msgLog.setSrcAddr(msg.getPeerAddr());
			}
			else if( "02".equals(msg.getTxfs())){		//GPRS or CDMA通道消息
				//对于上行，peerAddr＝终端IP地址；对于下行，peerAddr为业务处理器的IP地址
				msgLog.setSrcAddr(msg.getPeerAddr());
				
				//上行和下行，source对应为网关连接client；
				if( null != msg.getSource() ){
					msgLog.setDestAddr(msg.getSource().getPeerAddr());
				}
			}
			else{
				msgLog.setSrcAddr(msg.getPeerAddr());
			}

			//取本机当前时间作为原始报文通讯时间
			msgLog.setTime(new Date(System.currentTimeMillis()));
			msgLog.setBody(msg.getRawPacketString());
			msgLog.setSize(msgLog.getBody().length());
			if (msg.head.c_dir==MessageConst.ZJ_DIR_UP)//上行报文
				service.addToDao(msgLog,Integer.parseInt("5000"));
			else{//下行报文
				if(msg.getStatus()!=null&&msg.getStatus().equals("1"))//下发失败
					msgLog.setResult("1");
				else
					msgLog.setResult("0");
				service.addToDao(msgLog,Integer.parseInt("5001"));
			}
		}catch(Exception ex){
			log.error("Error to processing message log:"+msg, ex);
		}
	}

}
