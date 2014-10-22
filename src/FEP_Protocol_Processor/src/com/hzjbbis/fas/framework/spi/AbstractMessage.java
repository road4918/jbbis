package com.hzjbbis.fas.framework.spi;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageType;


public abstract class AbstractMessage implements IMessage{
	
	public MessageType msgType=MessageType.MSG_INVAL;
	protected int state = IMessage.STATE_INVALID;		//��Ϣ����״̬
	private Object attachment;	//��Ϣ���󸽼���Ϣ
	/**
	 * �������ԣ� key="client"����ʾ�ͻ���socket���Ӷ���
	 */
	protected HashMap hmAttributes = new HashMap();
	protected int priority = IMessage.PRIORITY_LOW;
	/**
	 * �ض���Ϣ�����Լ����屨��ͷ����Э���йء�
	 * ���ﲻ������Ϣ����ͷ����Ҫ�Ƿ�ֹ��������ת������
	 * ҵ����ģ�鲿�֣���Ҫ�ж���Ϣ����ľ������ͣ� instanceof ����
	 * ת����������Ϣ��������Э��ͷ��������Ҳ����ȷ��
	 */
	public ByteBuffer dataOut; 	//��Ϣ����������
	public ByteBuffer dataIn;	//��ϢӦ��������
//	public ByteBuffer rawData;
	public String upRawString = "";	//Ӧ��ԭʼ����
	public String downRawString="";	//����ԭʼ����
	public AbstractMessage nextMessage; //Ӧ����Ϣ���ܰ������֡������ָ�����֡
	//ÿ����Ϣ��Ψһkey
	public long key = 0;	//key = rtua | fseq<<32
	public long reqTime = System.currentTimeMillis();
	public long repTime = 0;
	public int accesstime=0;	/*���ʴ���*/
	private boolean multiReply=false;	/*��֡��Ӧ��ʶ*/
	
	/**
	 * modulerStack����ά������Ϣ�ĺ�������ģ�飬�Ա���������зַ���
	 *  
	 */
	private Stack modulerStack = new Stack();
	
	public AbstractMessage(){
		nextMessage = null;
	}
	
	public int getPriority(){
		return priority;
	}
	
	public void setPriority(int priority){
		this.priority = priority;
	}
	

	


	
	public boolean hasNextModule(){
		return modulerStack.size()>0;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object obj) {
		attachment = obj;
	}
	
	public MessageType getMessageType()
	{
		return msgType;
	}
	
	public IMessage getNextMessage()
	{
		return nextMessage;
	}

	public String getUpRawString() {
		return this.upRawString;
	}
	
	public String getDownRawString(){
		return this.downRawString;
	}

	public Object getAttribute(Object key) {
		return hmAttributes.get(key);
	}

	public void setAttribute(Object key, Object value) {
		if(hmAttributes.containsKey(key)){
			hmAttributes.remove(key);
		}
		hmAttributes.put(key,value);
	}
	
	public void removeAttribute(Object key){
		hmAttributes.remove(key);
	}
	
	public void copyAttributes(AbstractMessage src){
		Iterator it = src.hmAttributes.keySet().iterator();
		Object key;
		while(it.hasNext()){
			key = it.next();
			setAttribute(key,src.getAttribute(key));
		}
	}
/*	
	public ByteBuffer getRawReply(){
		return HexDump.toByteBuffer(upRawString);
	}
*/
	public int getRtuaIn() {
		return 0;
	}

	public int getRtuaOut() {
		return 0;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isMultiReply() {
		return multiReply;
	}

	public void setMultiReply(boolean multiReply) {
		this.multiReply = multiReply;
	}
}
