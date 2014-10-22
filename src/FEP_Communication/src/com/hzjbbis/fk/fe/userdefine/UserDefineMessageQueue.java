/**
 * �����Զ��屨�ĵ����ж����Լ�����ͨ����
 * 
 */
package com.hzjbbis.fk.fe.userdefine;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.fe.ChannelManage;
import com.hzjbbis.fk.message.zj.MessageZj;

/**
 * @author bhw
 *
 */
public class UserDefineMessageQueue {
	private static final Logger log = Logger.getLogger(UserDefineMessageQueue.class);
	private static final UserDefineMessageQueue instance = new UserDefineMessageQueue();
	private Map<Byte,IChannel> userMap = new HashMap<Byte,IChannel>();
	private UserDefineMessageQueue(){}
	
	public static final UserDefineMessageQueue getInstance(){
		return instance;
	}
	
	public void offer(MessageZj msg){
		//��GPRS�����յ��û��Զ��屨�����У���ѱ���ֱ�ӷ��͸����ҽ���ģ��
		//���ճ��ұ��룬�ѱ������͸���������ģ�顣
		IChannel srcChannel = userMap.get(msg.head.msta);
		if( null == srcChannel ){
			log.error("�յ������Զ��屨�ģ������ҽ���ģ����ͨ��ǰ�û��������Ҳ�����msg="+msg.getRawPacketString());
			return;
		}
		srcChannel.send(msg);
		//�����Զ��屨�ļ�¼��־
		log.info("�����Զ��屨��Ӧ��"+msg.getRawPacketString());
	}
	
	/**
	 * ���ҽ���ģ�飬���ӵ�ͨ��ǰ�û���ĳ������˿ڡ�
	 * ��������MessageZj��source��һ�����첽socket client����
	 * @param msg
	 * @return
	 */
	public boolean sendMessageDown(MessageZj msg){
		//1. �������еı��ģ������ұ����볧�ҽ���ģ�鵽ͨ��ǰ�û�֮���clientChannel��
		IChannel srcChannel = (IChannel)msg.getSource();
		userMap.put(msg.head.msta, srcChannel);
		//2. ѡ������ͨ����ֱ�����С�
		IChannel channel = ChannelManage.getInstance().getChannel(msg.head.rtua);
		if( null == channel )
			return false;
		//�����ն˲�֧���Զ�����
		channel.send(msg);
		return true;
	}
}
