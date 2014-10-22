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
			if (msgLog.getKzm().equals("21"))//�Կ�����Ϊ21�ĵ�¼�������⴦�����⵱�����б��ı���
				msg.head.c_dir=MessageConst.ZJ_DIR_UP;
			msgLog.setTxfs(msg.getTxfs());
			if (msg.getTxfs()!=null){//Ŀǰ���в���ͨѶ��ʽ
				if (msg.getTxfs().equals("01")){//UMS
					if (log.isDebugEnabled())
						log.debug("msg.getServerAddress():"+msg.getServerAddress());
					if (msg.getServerAddress()!=null){					
						String[] strList=msg.getServerAddress().split(",");
						if (strList.length>0){
							msgLog.setSrcAddr(strList[0]);//SIM����
							msgLog.setDestAddr(strList[1]);//appid+subid
						}
					}
				}
				else
					msgLog.setSrcAddr(msg.getPeerAddr());
			}
			//ȡ������ǰʱ����Ϊԭʼ����ͨѶʱ��
			msgLog.setTime(new Date(Calendar.getInstance().getTimeInMillis()));
			msgLog.setBody(msg.getRawPacketString());		
			msgLog.setSize(msgLog.getBody().length());
			if (msg.head.c_dir==MessageConst.ZJ_DIR_UP)//���б���
				service.addToDao(msgLog,Integer.parseInt("5000"));
			else//���б���
				service.addToDao(msgLog,Integer.parseInt("5001"));
		}catch(Exception ex){
			log.error("Error to processing message log:"+msg, ex);
		}
		
	}
}
