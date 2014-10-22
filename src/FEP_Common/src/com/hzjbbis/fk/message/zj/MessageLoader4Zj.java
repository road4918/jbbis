/**
 * �㽭��Լ��Ϣ��ϵ�л��ͼ�����
 */
package com.hzjbbis.fk.message.zj;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageLoader;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author hbao
 * 2008-06-14 11:27
 */
public class MessageLoader4Zj implements MessageLoader {
	private static final Logger log = Logger.getLogger(MessageLoader4Zj.class);

	public MessageZj loadMessage(String serializedString) {
		StringTokenizer st = new StringTokenizer(serializedString,"|");
		MessageZj msg = new MessageZj();
		String token;
		boolean stop = false;
		try{
			token = st.nextToken();
			if( token.equals(MessageZj.class.getName()) ){
				//�ϻ����ʽ����һ����Class����|uprawstring=XXXXX
				token = st.nextToken().substring(12);
				stop = true;
			}
			if( !msg.read(HexDump.toByteBuffer(token)) ){
				log.info("�ӻ�����ص���Ϣ�����㽭��Լ��Ϣ��"+serializedString);
				return null;
			}
			if( stop )
				return msg;		//��������ȡ
			while(st.hasMoreTokens()){
				String item = st.nextToken();
				if( "ioti".equalsIgnoreCase(item.substring(0, 4))){
					token = item.substring(7);	//iotime=
					msg.setIoTime(Long.parseLong(token));
				}
				else if( "peer".equalsIgnoreCase(item.substring(0, 4))){
					token = item.substring(9);	//peeraddr=
					msg.setPeerAddr(token);
				}
				else if( "txfs".equalsIgnoreCase(item.substring(0, 4))){
					token = item.substring(5);	//peeraddr=
					msg.setTxfs(token);
				}
			}
			msg.setPriority(IMessage.PRIORITY_LOW);
			return msg;
		}catch(Exception exp){
			log.warn("������ش���buf="+serializedString+",exp="+exp.getLocalizedMessage());
		}
		return null;
	}

	public String serializeMessage(IMessage message) {
		if( message.getMessageType() != MessageType.MSG_ZJ )
			return null;
		MessageZj msg = (MessageZj)message;
		StringBuffer sb = new StringBuffer(512);
		sb.append(msg.getRawPacketString()).append("|iotime=");
		sb.append(msg.getIoTime()).append("|peeraddr=").append(msg.getPeerAddr());
		sb.append("|txfs=").append(msg.getTxfs());
		return sb.toString();
	}

}
