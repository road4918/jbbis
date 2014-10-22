package com.hzjbbis.fk.message.gate;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.utils.HexDump;

public class GateHead {
	private static final Logger log = Logger.getLogger(GateHead.class);
	public static final byte ATT_VERSION = 0x01; // Э��汾
	public static final byte ATT_ENCRIPT = 0x02; // ���ܱ�־
	public static final byte ATT_DESTID = 0x03; // Ŀ��ģ��ID
	public static final byte ATT_PRIVATE = 0x04; // ˽����Ϣ�����ܹ㲥
	public static final byte ATT_CPYATT = 0x05; // Ӧ����Ϣ�������������Ϣ������
	public static final byte ATT_LOGTYPE = 0X06; // ��monitor��Ϣ����
	public static final byte ATT_FILEPATH = 0x07; // ���·��:"log/my.xml"
	public static final byte ATT_SRCADDR = 0x08; // �ն���ԴIP��ַ
	public static final byte ATT_DESTADDR = 0x09; // ����Ŀ��IP��ַ
	public static final byte ATT_TXFS = 0x0a; // ͨѶ��ʽ
	public static final byte ATT_TXSJ = 0x0b; // ͨѶʱ��
	public static final byte ATT_MSGTYPE = 0x0c; // ������������Ϣ���ͣ���IMessage����
	public static final byte ATT_MSGSEQ = 0x10; // ��Ϣ���
	public static final byte ATT_SERVERADDR	= 0x11;	//���ص�ʵ�ʷ�������ַ(ip:port)

	private final String flag = "JBBS";
	private byte src = 0x02;
	private short cmd;
	private int intBodyLen;
	private short headAttrLen;
	private HashMap<Byte, HeadAttribute> hmAttr = new HashMap<Byte, HeadAttribute>();
	private MessageType msgType = MessageType.MSG_INVAL;
	private byte[] rawHead = null;		//������Ϣ��λ֮ǰ���������ݡ�

	public GateHead() {
		cmd = 0;
		intBodyLen = 0;
		headAttrLen = 0;
	}

	public boolean isValid() {
		return cmd != 0x00;
	}
	
	public MessageType testMessageType(ByteBuffer buffer){
		if( msgType != MessageType.MSG_INVAL )
			return msgType;
		byte[] gateFlag = flag.getBytes();

		//�ر�ע�⣺�ڼ��buffer���ݹ����У����þ���λ�ö�����ҪӰ��buffer.position
		int plen = gateFlag.length;
		boolean matched = true;
		int pos0 = buffer.position();
		int pos = -1;
		for( pos=buffer.position(); pos+plen<buffer.limit(); pos++ ){
			matched = true;
			for( int i=0;i<plen; i++){
				if( buffer.get(pos+i) != gateFlag[i] ){
					matched = false;
					break;
				}
			}
			if( matched )
				break;
		}
		if( matched ){
			if( pos-pos0>0 ){
				rawHead = new byte[pos-pos0];
				buffer.get(rawHead);
//				for(int i=0;i<rawHead.length;i++)
//					rawHead[i] = buffer.get(pos0+i);
//				buffer.position(pos);
				log.warn("������Ϣ�����Ƿ�����:"+HexDump.hexDumpCompact(rawHead, 0, rawHead.length));
			}
			msgType = MessageType.MSG_GATE;
			return msgType;
		}
		
		msgType = MessageType.MSG_INVAL;
		//�����Ƿ����㽭��Լ��Ϣ
		pos0 = buffer.position();
		for( pos=pos0; pos+13 <= buffer.limit(); pos++){
			if( 0x68 != buffer.get(pos) )
				continue;
			if( 0x68 == buffer.get(pos+7) ){
				//�ҵ��㽭��Լ֡
				if( pos>pos0 ){
					rawHead = new byte[pos-pos0];
					for(int i=0;i<rawHead.length;i++)
						rawHead[i] = buffer.get(pos0+i);
				}
				msgType = MessageType.MSG_ZJ;
				cmd = MessageGate.CMD_WRAP_ZJ;
				return msgType;
			}
		}
		log.warn("����Э�鲻��ʶ��ķǷ�����:"+HexDump.hexDumpCompact(buffer));
		buffer.position(buffer.limit());		//����ȫ��������
		return msgType;
	}

	/**
	 * �ӻ�������ȡ���ݡ�
	 * @param buffer
	 * @return true�����������ȡ����ͷ��false otherwise
	 */
	public boolean read(ByteBuffer buffer) {
		if( buffer.remaining()<13 )
			return false;
		if( msgType == MessageType.MSG_ZJ )
			return true;		//��⵽�㽭��Լ��Ϣ����£�����ͷ������
		//Ԥ�ȶ�ȡHead���������������ȡ���򷵻�false������buffer�ָ�δ��״̬
		buffer.mark();
		int posOld = buffer.position();		//����ͷ��ʼ��λ��
		int index = 0;
		// skip 4 bytes flag
		for (index = 0; index < 4; index++)
			buffer.get();
		src = buffer.get();
		cmd = buffer.getShort();
		intBodyLen = buffer.getInt();
		headAttrLen = buffer.getShort();
		if( headAttrLen<0 || headAttrLen>4096 || intBodyLen<0 ){
			log.warn("���ĸ�ʽ����headAttrLen="+headAttrLen+",intBodyLen="+intBodyLen);
			buffer.reset();
			return false;		//�����׳��쳣��
		}
		//��ȡ��������ͷ
		if( headAttrLen > buffer.remaining() ){
			log.info("���������Ȳ����Զ�ȡ���ر���ͷ��");
			buffer.reset();
			return false;
		}
		index = 0;
		while( index< headAttrLen ){
			byte id = buffer.get();
			short attlen = buffer.getShort();
			setAttribute(id, buffer, attlen);
			index += attlen + 3;
		}
		int headLen = 13+index;
		rawHead = new byte[headLen];
		buffer.position(posOld);
		buffer.get(rawHead);
		return true;
	}

	public int getHeadLen() {
		if (headAttrLen == 0)
			headAttrLen = getHeadAttrLen();
		return 13 + headAttrLen;
	}

	public int getTotalLen() {
		return getHeadLen() + intBodyLen;
	}

	/**
	 * ����ͷ����д�뻺������
	 * @param buffer
	 * @return true������ͷ����д�뻺������false otherwise
	 */
	public boolean write(ByteBuffer buffer) {
		short attrLen = getHeadAttrLen();
		if( buffer.remaining()< 13+ attrLen )
			return false;
		int posOld = buffer.position();
		buffer.put(flag.getBytes());
		buffer.put(src);
		buffer.putShort(cmd);
		buffer.putInt(intBodyLen);
		buffer.putShort(attrLen);
		for(HeadAttribute attr: hmAttr.values() ){
			buffer.put(attr.id).putShort(attr.len).put(attr.attr);
		}
		rawHead = new byte[this.getHeadLen()];
		buffer.position(posOld);
		int limitOld = buffer.limit();
		buffer.limit(posOld+rawHead.length);
		buffer.get(rawHead,0,rawHead.length);
		buffer.limit(limitOld);
		return true;
	}

	public void setCommand(short command) {
		cmd = command;
	}

	public void setCommand(int command) {
		cmd = (short) command;
	}

	public short getCommand() {
		return cmd;
	}

	public int getIntBodylen() {
		return intBodyLen;
	}

	public void setIntBodylen(int bodylen) {
		this.intBodyLen = bodylen;
	}

	public short getHeadAttrLen() {
		short hlen = 0;
		for(HeadAttribute attr: hmAttr.values() ){
			hlen += attr.len + 3;
		}
		return hlen;
	}

	public void setAttribute(byte id, ByteBuffer buf, short len) {
		assert buf.remaining() >= len;

		HeadAttribute attr = new HeadAttribute();
		attr.id = id;
		attr.len = len;
		attr.attr = new byte[len];
		for(int i=0;i<len;i++)
			attr.attr[i] = buf.get();
		hmAttr.put(id, attr);
	}

	public void setAttribute(byte id, byte[] attr, int pos, int len) {
		assert null != attr;
		assert len < 1024 && len > 0 && (pos + len) < attr.length;

		HeadAttribute attrObj = new HeadAttribute();
		attrObj.id = id;
		attrObj.len = (short) len;
		attrObj.attr = new byte[len];
		System.arraycopy(attr, pos, attrObj.attr, 0, len);
		hmAttr.put(new Byte(id), attrObj);
	}

	public void setAttribute(byte id, byte[] attr) {
		assert null != attr;
		setAttribute(id, attr, 0, attr.length);
	}

	public void setAttribute(byte id, int data) {
		byte[] val = new byte[4];
		ByteBuffer bf = ByteBuffer.wrap(val);
		bf.putInt(data);
		bf.flip();
		setAttribute(id, bf.array());
	}

	public void setAttribute(byte id, long data) {
		byte[] val = new byte[8];
		ByteBuffer bf = ByteBuffer.wrap(val);
		bf.putLong(data);
		bf.flip();
		setAttribute(id, bf.array());
	}

	public void setAttribute(byte id, short data) {
		byte[] val = new byte[2];
		ByteBuffer bf = ByteBuffer.wrap(val);
		bf.putShort(data);
		bf.flip();
		setAttribute(id, bf.array());
	}

	public void setAttribute(byte id, byte data) {
		byte[] val = new byte[1];
		ByteBuffer bf = ByteBuffer.wrap(val);
		bf.put(data);
		bf.flip();
		setAttribute(id, bf.array());
	}

	public void setAttribute(byte id, String data) {
		byte[] val = data.getBytes();
		setAttribute(id, val);
	}

	public byte[] getAttribute(byte id) {
		HeadAttribute attr = (HeadAttribute) hmAttr.get(new Byte(id));
		if (null == attr)
			return null;
		return attr.attr;
	}

	public int getAttributeAsInt(byte id) {
		byte[] val = getAttribute(id);
		if (null == val || val.length != 4)
			return 0;
		ByteBuffer bf = ByteBuffer.wrap(val);
		return bf.getInt();
	}

	public long getAttributeAsLong(byte id) {
		byte[] val = getAttribute(id);
		if (null == val || val.length != 8)
			return 0;
		ByteBuffer bf = ByteBuffer.wrap(val);
		return bf.getLong();
	}

	public short getAttributeAsShort(byte id) {
		byte[] val = getAttribute(id);
		if (null == val || val.length != 2)
			return 0;
		ByteBuffer bf = ByteBuffer.wrap(val);
		return bf.getShort();
	}

	public String getAttributeAsString(byte id) {
		byte[] val = getAttribute(id);
		if (null == val)
			return "";
		try {
			return new String(val);
		} catch (Exception exp) {
			return HexDump.hexDumpCompact(val, 0, val.length);
		}
	}

	public byte getAttributeAsByte(byte id) {
		byte[] val = getAttribute(id);
		if (null == val)
			return 0x00;
		return val[0];
	}

	public byte[] getRawHead(){
		if( null == rawHead ){
			rawHead = new byte[13];
			ByteBuffer headBuffer = ByteBuffer.wrap(rawHead);
			this.write(headBuffer);
		}
		return rawHead;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("|source=").append(HexDump.toHex(this.src));
		sb.append(",cmd=").append(HexDump.toHex(this.cmd));
		sb.append(",bodylen=").append(this.getIntBodylen());
		sb.append(",attrlen=").append(this.getHeadAttrLen());
		if (hmAttr.size() > 0) {
			sb.append(",attributes:");
			for(HeadAttribute attr: hmAttr.values() ){
				sb.append(attr);
			}
		}
		return sb.toString();
	}
	
	public MessageType getMessageType(){
		return msgType;
	}

	class HeadAttribute {
		public byte id;
		public short len;
		public byte[] attr;

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(";att.id=").append(HexDump.toHex(id));
			sb.append(",att.len=").append(len);
			String str = "";
			try {
				str = new String(attr);
			} catch (Exception exp) {
				str = HexDump.hexDumpCompact(attr, 0, attr.length);
			}
			sb.append(",att.str=").append(str);
			return sb.toString();
		}
	}
}
