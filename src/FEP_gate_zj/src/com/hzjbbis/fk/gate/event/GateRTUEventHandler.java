/**
 * ���ܸ�����
 * 		�����յ��ն����б��ġ����б��ķ��ͳɹ��¼���
 *    �ն����б��Ľ���MessageQueue���У��Ա㷢�͸�ǰ�û���
 *    �ն����б��ĳɹ��¼����򵥴�ӡ��־���޽�һ����������
 * ����ʵ�֣�
 * SimpleEventHandler�����ࡣ
 * override handleEvent���������ReceiveMessageEvent��SendMessageEvent�ر���
 * ע�������spring�����ļ��У�source��������������ն˽ӿڵ�SocketServer����
 */
package com.hzjbbis.fk.gate.event;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.EventType;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IEvent;
import com.hzjbbis.fk.gate.MessageQueue;
import com.hzjbbis.fk.gate.PrefixRtuManage;
import com.hzjbbis.fk.gate.RTUChannelManager;
import com.hzjbbis.fk.gate.event.autoreply.AutoReply;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.sockserver.event.ReceiveMessageEvent;

/**
 * @author bhw
 *	2008��06��03 22��37
 */
public class GateRTUEventHandler extends BasicEventHook {
	private static final Logger log = Logger.getLogger(GateRTUEventHandler.class);
	private int seq = 0;
	private MessageQueue queue;

	public void setQueue(MessageQueue queue) {
		this.queue = queue;
	}

	/**
	 * ���ظ÷�����
	 */
	public void handleEvent(IEvent e) {
		/** �����ն˷����յ����ģ����뾡�췢�͸�ǰ�û��������첽ԭ�򣬲���ֱ�ӵ���
		 *  ����ǰ�û�����Accept��client���͡���Ҫ�ŵ�ǰ�û����ж��С�
		 *  	1����ǰ�û����ӵ����أ�֪ͨ���ж��з������б��ģ�
		 *  	2����ǰ�û���Ӧclient�ɹ��������б��ģ�֪ͨ���ж��м������ͣ�
		 */
		if( e.getType() == EventType.MSG_RECV ){
			//�㽭��Լ���������
			long n1 = System.currentTimeMillis();
			ReceiveMessageEvent evt = (ReceiveMessageEvent)e;
			IMessage msg = e.getMessage();
			if( msg.getMessageType() == MessageType.MSG_ZJ ){
				MessageZj zjmsg = (MessageZj)msg;
				RTUChannelManager.addClient(zjmsg.head.rtua, evt.getClient());
			}
			long n2 = System.currentTimeMillis();
			if( n2-n1> 15 )
				log.warn("RTUChannelManager.addClient����>N����,time="+(n2-n1));
			queue.offerUpMessageInQueue(e.getMessage());
			long n3 = System.currentTimeMillis();
			if( n3-n2> 80 )
				log.warn("offer.UpMessageInQueue����>N����,time="+(n3-n2));
			
			//���ԣ������Զ�Ӧ��ԭ��Ϣ���ء�
			IMessage rep = AutoReply.reply(e.getMessage());
			long n4 = System.currentTimeMillis();
			if( n4-n3>15 )
				log.warn("AutoReply.reply����>N����, time="+(n4-n3));
			if( null != rep ){
				try{
					MessageZj zjmsg = (MessageZj)rep;
					if( null != zjmsg ){
						//���������ļ��ĸ߿��ն�������Ҫ����ǰ���ַ�
						zjmsg.setPrefix(PrefixRtuManage.getInstance().getRtuPrefix(zjmsg.head.rtua));
						queue.sendDownMessage(zjmsg);
					}
				}catch(Exception exp){
					log.warn(exp.getLocalizedMessage(),exp);
				}
				//queue.sendDownMessage(rep);
				seq++;
				if( log.isDebugEnabled())
					log.debug("send msg="+seq+" msg="+rep);
			}
			long n5 = System.currentTimeMillis();
			if( n5-n3> 80 )
				log.warn("queue.sendDownMessage����>N���룬��"+(n5-n3));
		}
		super.handleEvent(e);
	}

}
