/**
 * �㽭�������ع�Լ ���Ķ��塣
 * �㽭��Լ��������13���ֽڡ�����ͷ��������11����������ݲ��������ܶ�ȡ��
 * ���ǵ������ն˴���ǰ���ַ�������˶�ȡʱ���ȰѲ����Ϲ�Լ���ֶ�ȡ������
 */
package com.hzjbbis.fk.message.zj;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.exception.MessageParseException;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageConst;
import com.hzjbbis.fk.message.MessageType;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 * 2008-06-02 21:18
 */
public class MessageZj implements IMessage {
	private static final Logger log = Logger.getLogger(MessageZj.class);
	private static final int MAX_LEN = 1024;		//�㽭��Լ��һ��֡�����
	private static final MessageType type = MessageType.MSG_ZJ;
	private IChannel source;
	public int rtua = 0;
	public MessageZjHead head = new MessageZjHead();
	public ByteBuffer data;
	private StringBuffer rawPacket = new StringBuffer(256);
	private byte[] prefix;		//ǰ���ַ���
	private int priority = IMessage.PRIORITY_LOW;	//low priority
	private long ioTime;		//�����յ���Ϣ���߷������ʱ��
	private String peerAddr;	//�Է���IP:PORT��ַ
	private String serverAddress;	//�ն�ʵ�����ӵ�����IP��PORT�����ڽ��յ���Ϣʱ���ն��ʲ���ȶԡ�
	private String txfs="";
	//�����ڲ�����
	private int state = IMessage.STATE_INVALID;
	private byte _cs = 0;		//����ʱ����������У����
	private static final String charset = "ISO-8859-1";
	
	//by yangjie Ϊ��Լ������������
	/**ÿ����Ϣ��Ψһkey */
	public long key = 0;	//key = rtua | fseq<<32
    /** ��֡������״̬ */
    private String status;
    /** �������ݿ�����ID */
    private Long cmdId;
    private int msgCount;

	public MessageType getMessageType() {
		return type;
	}
	
	/**
	 * �㽭��Լ֡��λ
	 * @param readBuffer
	 * @return
	 */
	public int locatePacket(ByteBuffer readBuffer)  throws MessageParseException{
		int pos = -1;
		int pos0 = readBuffer.position();
		boolean located = false;
		int posMark = -1;
		for( pos=pos0; pos+13 <= readBuffer.limit(); pos++){
			if( 0x68 != readBuffer.get(pos) )
				continue;
			if( 0x68 == readBuffer.get(pos+7) ){
				//ò���ҵ��㽭��Լ֡����Ҫ�Ѹոն�λ��֮ǰ������Ϊprefix����������
				//��68xxxxxxxxxx68֮ǰ������Ϊprefix��������Ƕ�λʧ�ܣ���68xxxxxxxxxx������Ϊ����������68��ʼ�ҡ�
				//��1����������ݳ��ȣ������Ƿ�Ƿ���
				head.dlen = (short)(readBuffer.get(pos+9) & 0xFF);
				head.dlen |= ( 0xFF & readBuffer.get(pos+10)) << 8;
				if( head.dlen<0 ){
					//�϶����󡣶���68�Լ�֮��6�ֽڡ���ʾcontinue֮���ִ��pos++
					pos += 6;
					continue;
				}
				head.c_func = (byte) (readBuffer.get(pos+8) & 0x3f);
				//��2����⣺�����û��Զ������ݣ�����֡���Ȳ��ܴ���MAX_LEN
				if( head.c_func != 0x0F && head.dlen>= MAX_LEN ){
					pos += 6;
					continue;
				}
				//��3�����:dlen>���ݳ��ȣ������ݳ���<buffer capacity����������68��λ
				//��������������ղ�֡��ʽ���󣬻�����������ԭ��֡����û�з���������
				//�����֡��ʽ��������Ҫ����λ���������������ܷ����������֡��������Ϊ���ݲ������������ȴ�����
				if( (head.dlen+13)>(readBuffer.limit()-pos0) && readBuffer.limit()<readBuffer.capacity() ){
					posMark = pos;		//���������ݲ���������Ҫ��¼���ζ�λ��
					located = true;
					//���������⡣
					pos += 6;
					continue;
				}
				
				//��4���������ǺϷ�ֱ֡�ӵ����Ҳ�����ǵ�3��������⵽���
				if( pos>pos0 ){
					prefix = new byte[pos-pos0];
					//�ȶ�λ�����һ��'|'
					int lastDelimiter = -1;
					for(int i=0;i<prefix.length;i++){
						prefix[i] = readBuffer.get();
						if( '|' == prefix[i] )
							lastDelimiter = i;
					}
					//���°����������㽭��Լ��Ϣ����ʽ����
					//iotime=xxxx|peeraddr=xxxxx|txfs=xxx|�㽭��Լԭʼ֡
					if( prefix.length> 16 ){
						byte[] iot = "iotime=".getBytes();
						boolean isAttr = true;
						for(int j=0; j<iot.length; j++){
							if( iot[j] != prefix[j] ){
								isAttr = false;
								break;
							}
						}
						if( isAttr ){
							String attrs;
							try{
								attrs = new String(prefix,0,lastDelimiter,charset);
							}catch(UnsupportedEncodingException e){
								attrs = new String(prefix);
							}
							StringTokenizer st = new StringTokenizer(attrs,"|");
							String token = st.nextToken().substring(7);
							this.ioTime = Long.parseLong(token);
							this.peerAddr = st.nextToken().substring(9);
							if( st.hasMoreTokens() )
								this.txfs = st.nextToken().substring(5);
							byte[]p = new byte[prefix.length-lastDelimiter-1];
							for(int i=0;i<p.length; i++)
								p[i] = prefix[lastDelimiter+1+i];
							prefix = p;
						}
					}
					rawPacket.append(HexDump.hexDumpCompact(prefix,0, prefix.length));
				}
				located = true;
				break;
			}
		}
		if( !located ){
			//��������ݿ���ȫ�����ǷǷ����ݡ���Ҫ����
			for(; pos<readBuffer.limit(); pos++ ){
				if( 0x68 != readBuffer.get(pos) )
					continue;
			}
			int posEnd = Math.max(readBuffer.limit()-13, pos);
			byte[] bts = new byte[posEnd-pos0];
			readBuffer.get(bts);
			String expInfo = "exp�������㽭��Լ�����ı�������"+ HexDump.hexDumpCompact(bts, 0, bts.length);
			log.warn(expInfo);
			throw new MessageParseException(expInfo);
		}
		//�Ӻ���ĵط���ʼ�����ģ�����ǰ�����ݡ�
		return posMark<0 ? pos : posMark;
	}
	
	
	/**
	 * ����ϱ��뿼�ǵ�GPRSͨѶ�������⣬TCP�����ܲ���һ�ζ�ȡ����һ֡���ġ�
	 */
	public boolean read(ByteBuffer readBuffer) throws MessageParseException{
		if( state == IMessage.STATE_INVALID && readBuffer.remaining()<13 ){
			if( log.isDebugEnabled() )
				log.debug("���Ȳ����Զ�ȡ�㽭��Լ����ͷ���Ȼ��������ȡ��readBuffer.remaining="+readBuffer.remaining());
			return false;		//�������Բ��㣬�Ȼ��������ȡ
		}
		if( state == IMessage.STATE_INVALID ){	//��Ϣͷû�ж�ȡ
			//���ǰ���ַ�������λ�㽭��Լ֡ͷ
			int pos= locatePacket(readBuffer);
			if( readBuffer.limit()-pos < 13 ){
				if( log.isDebugEnabled() )
					log.debug("�����㽭��Լ���Ķ�λ����˺󣬳��Ȳ����Զ�ȡ����ͷ��readBuffer.remaining="+readBuffer.remaining());
				return false;
			}
			
			//���ζ�ȡ ����ͷ�������塢����β
			pos = readBuffer.position();
			byte [] bts = new byte[11];
			_cs = 0;
			for( int i = 0; i< 11 ; i++ ){
				bts[i] = readBuffer.get(i+pos);		//����λ�ö���Ӱ��position()
				_cs += bts[i];
			}
			rawPacket.append(HexDump.hexDumpCompact(bts, 0, bts.length));
			
			head.flag1 = readBuffer.get();
			head.rtua_a1 = readBuffer.get();
			head.rtua_a2 = readBuffer.get();
			short iTemp = 0;
			byte c1 = readBuffer.get();
			iTemp = (short) ((0xff & c1) << 8);
			head.rtua_b1b2 = (short)(0xFF & c1);
			c1 = readBuffer.get();
			iTemp |= (short) (0xff & c1);
			head.rtua_b1b2 |= (short)((0xFF & c1 )<<8);
			
			head.rtua = (head.rtua_a1 & 0xFF) << 24;
			head.rtua |= (0xFF & head.rtua_a2) << 16;
			head.rtua |= 0xFFFF & head.rtua_b1b2;
			head.rtua_b1b2 = iTemp;
			rtua = head.rtua;
			
			iTemp = (short) (0xFF & readBuffer.get());
			iTemp |= (0xFF & readBuffer.get()) << 8;
			head.msta = (byte) (0x003f & iTemp);
			head.fseq = (byte) ((0x1fc0 & iTemp) >> 6);
			head.iseq = (byte) ((0xe000 & iTemp) >> 13);
	
			head.flag2 = readBuffer.get();
			
			c1 = readBuffer.get();
			head.c_func = (byte) (c1 & 0x3f);
			//�����ն�Ӧ���ģ����ȼ���ߡ����ǣ�������ٲ����������ȼ���λΪnormal��
			if( head.msta != 0 ){
				if( head.c_func != MessageConst.ZJ_FUNC_READ_TASK )
					priority = IMessage.PRIORITY_VIP;
				else
					priority = IMessage.PRIORITY_NORMAL;
			}
			else{
				if( head.c_func == MessageConst.ZJ_FUNC_EXP_ALARM )		//��Ϣ�����ȼ�
					priority = IMessage.PRIORITY_HIGH;
				else if( head.c_func == MessageConst.ZJ_FUNC_READ_TASK )
					priority = IMessage.PRIORITY_LOW;
				else
					priority = IMessage.PRIORITY_NORMAL;
			}
			
			head.c_expflag = (byte) ((c1 & 0x40) >> 6);
			head.c_dir = (byte) ((c1 & 0x80) >> 7);
	
			head.dlen = (short)(readBuffer.get() & 0xFF);
			head.dlen |= ( 0xFF & readBuffer.get()) << 8;
			
			state = IMessage.STATE_READ_DATA;
			if( head.dlen+2 >readBuffer.remaining() ){	//��Ҫ�����ȴ�����
				if( log.isDebugEnabled() )
					log.debug("���л��������ݳ���[buflen="+readBuffer.remaining()+"]<�㽭��Լ����������["+head.dlen+2+"]");
				return false;
			}
			//��Ϣ�ܹ�������ȡ
			return readDataSection(readBuffer);
		}
		else if( state == IMessage.STATE_READ_DATA || state == IMessage.STATE_READ_TAIL ){
			return readDataSection(readBuffer);
		}
		else{
			//״̬�Ƿ�
			log.error("��Ϣ��ȡ״̬�Ƿ�,state="+state);
		}
		return true;
	}
	
	private boolean readDataSection(ByteBuffer readBuffer) throws MessageParseException{
		//��һ�ζ�ȡ�����岿��
		if( null == data ){
			data = ByteBuffer.allocate(head.dlen);
		}
		if( state == IMessage.STATE_READ_DATA ){
			while( data.hasRemaining() ){		//������û�ж�ȡ���
				if( readBuffer.hasRemaining() )
					data.put(readBuffer.get());
				else
				{//������û�������ˣ����Ǳ����廹û�ж�ȡ���
					return false;
				}
			}
			data.flip();		//ready for read.
			byte[] d = data.array();
			for(int i=0; i<d.length; i++){
				_cs += d[i];
			}
			state = IMessage.STATE_READ_TAIL;
			rawPacket.append(HexDump.hexDumpCompact(data));
		}
		if( readBuffer.remaining()>=2 ){
			head.cs = readBuffer.get();
			rawPacket.append(HexDump.toHex(head.cs));
			head.flag3 = readBuffer.get();
			rawPacket.append(HexDump.toHex(head.flag3));
			if( _cs != head.cs ){
				data = null;
				String packet = rawPacket.toString();
				rawPacket.delete(0, packet.length());
				state = IMessage.STATE_INVALID;		//���¿�ʼ����Ϣ��״̬��
				throw new MessageParseException("expУ���벻��ȷ:"+packet);
			}
			if( 0x16 != head.flag3 ){
				//֡���Դ��ڴ���������Ϊ0x16
				data = null;
				String packet = rawPacket.toString();
				rawPacket.delete(0, packet.length());
				state = IMessage.STATE_INVALID;		//���¿�ʼ����Ϣ��״̬��
				throw new MessageParseException("exp�����16��־����֡��ʽ����packet="+packet);
			}
			state = IMessage.STATE_READ_DONE;
			return true;
		}
		//���б���βû�ж�ȡ��
		return false;
	}

	public boolean write(ByteBuffer writeBuffer) {
		synchronized(rawPacket){
			return _write(writeBuffer);
		}
	}

	/**
	 * д��Ϣ��Ҳ���뿼�ǲ��ֿ�д���������Ϣ���ܲ���һ���������ͳ�ȥ��
	 */
	private boolean _write(ByteBuffer writeBuffer) {
		int prefixLen = null==prefix ? 0 : prefix.length ;
		if( state == IMessage.STATE_INVALID && writeBuffer.remaining()<13+prefixLen ){
			log.info("д���峤�Ȳ��㣬�Ȼ������д��");
			return false;		//�������Բ��㣬�Ȼ��������ȡ
		}
		if( state == IMessage.STATE_INVALID //����´�������Ϣ
			|| IMessage.STATE_READ_DONE == state //������ʹ�����ͨ����ȡ����Ϣ��
			){	//��Ϣͷû��д
			if( rawPacket.length() >0 ){
				rawPacket.delete(0, rawPacket.length());
			}
			if( null != prefix ){
				writeBuffer.put(prefix);		//ǰ���ַ���
				rawPacket.append(HexDump.hexDumpCompact(prefix, 0, prefix.length));
			}
			//д��Ϣͷ
			int pos0 = writeBuffer.position();	//��Ϣд��֮ǰ��λ��
			byte c = 0;
			_cs = 0;
			writeBuffer.put((byte) 0x68);
			_cs += 0x68;
			c = (byte)( (head.rtua>>24) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = (byte)( (head.rtua>>16) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			//�ߵ�
			c = (byte)( (head.rtua) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = (byte)( (head.rtua>>8) & 0xFF);
			_cs += c;
			writeBuffer.put(c);

			short iTemp = 0;
			iTemp |= (short) head.msta;
			iTemp |= (short) (head.fseq << 6);
			iTemp |= (short) (head.iseq << 13);
			c = (byte) (iTemp & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = (byte) ((iTemp >> 8) & 0xFF );
			_cs += c;
			writeBuffer.put(c);
			
			_cs += 0x68;
			writeBuffer.put((byte) 0x68);
			
			c = head.c_func; // & 0x3f
			c |= 0x40 & (head.c_expflag << 6);
			c |= 0x80 & (head.c_dir << 7);
			_cs += c;
			writeBuffer.put(c);

			if( null == data )
				head.dlen = 0;
			else{
				if( data.position()>0 )
					data.position(0);
				head.dlen = (short)data.remaining();
			}
			iTemp = head.dlen;
			c = (byte) (iTemp & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = (byte) ((iTemp >> 8) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			int pos1 = writeBuffer.position();
			byte[] bts = new byte[pos1-pos0];
			for(int i=0;i<bts.length;i++){
				bts[i] = writeBuffer.get(pos0+i);
			}
			rawPacket.append(HexDump.hexDumpCompact(bts, 0, bts.length));
			//֡ͷд��ϣ����濪ʼд�����岿��
			state = IMessage.STATE_SEND_DATA;
			return _writeDataSection(writeBuffer);
		}
		if( IMessage.STATE_SEND_DATA == state ){
			return _writeDataSection(writeBuffer);
		}
		return IMessage.STATE_SEND_DONE == state;
	}
	
	private boolean _writeDataSection(ByteBuffer writeBuffer){
		byte c;
		int bufLen = writeBuffer.remaining();
		if( IMessage.STATE_SEND_DATA == state ){
			if( bufLen < head.dlen+2 ){
				log.info("������̫�̣�����һ�ΰ������巢�����");
				return false;
			}
			if( head.dlen>0 ){
				while( data.hasRemaining() ){
					c = data.get();
					_cs += c;
					writeBuffer.put(c);
				}
				data.rewind();
				rawPacket.append(HexDump.hexDumpCompact(data));
			}
			head.cs = _cs;
			writeBuffer.put(_cs);		rawPacket.append(HexDump.toHex(_cs));
			writeBuffer.put((byte) 0x16); rawPacket.append("16");
			
			state = IMessage.STATE_SEND_DONE;
			return true;
		}
		//����β��������һ��д�������㽭��Լ��Ϣ����Ӧ��������״̬
		state = IMessage.STATE_SEND_DONE;
		return true;
	}

	public IChannel getSource() {
		return source;
	}

	public void setSource(IChannel src) {
		source = src;
	}

	public String getRawPacketString() {
		//���ڷ��ͳ�ȥ����Ϣ���ڷ���֮ǰ��rawPacketΪ��
		synchronized(rawPacket){
			if( IMessage.STATE_INVALID == state || 0 == rawPacket.length() ){
				//��û��write����rawPacketΪ�մ���
				ByteBuffer buf = ByteBuffer.allocate(512);
				int _state = state;
				write(buf);
				state = _state;
				return rawPacket.toString();
			}
			else{
				//�Ѿ����ܲ���д��ע�Ᵽ��rawPacket��Ϣ
				if( IMessage.STATE_SEND_DONE == state || IMessage.STATE_READ_DONE == state )
					return rawPacket.toString();
				else if( IMessage.STATE_SEND_DATA == state || IMessage.STATE_SEND_HEAD == state 
						|| IMessage.STATE_SEND_TAIL == state ){
					ByteBuffer buf = ByteBuffer.allocate(512);
					String old = rawPacket.toString();
					rawPacket.delete(0, old.length());
					int _state = state;
					write(buf);
					String raw = rawPacket.toString();
					rawPacket.delete(0, raw.length());
					rawPacket.append(old);
					state = _state;
					return raw;
				}
				//���ܶ�ȡ�������ݣ�Ȼ�󲶻��쳣������Ϣ�����������Դ�ӡ������������
				return rawPacket.toString();
			}
		}
	}
	
	/**
	 * �÷�����һ�������յ���������µ��á�
	 * �㽭��Լ��Ϣ��ioTime=xx|peeraddr=xxx|txfs=xx|���㽭��Լԭʼ֡��
	 */
	public byte[] getRawPacket() {
		byte[] ret;
		byte[] raw = HexDump.toByteBuffer(getRawPacketString()).array();
		if( ioTime>0 ){
			StringBuffer sb = new StringBuffer(64);
			sb.append("iotime=").append(ioTime);
			sb.append("|peeraddr=").append(peerAddr).append("|txfs=");
			sb.append(txfs).append("|");
			byte[] att = null;
			try{
				att = sb.toString().getBytes(charset);
			}catch(UnsupportedEncodingException e){
				att = sb.toString().getBytes();
			}
			ret = new byte[att.length+raw.length];
			System.arraycopy(att, 0, ret, 0, att.length);
			System.arraycopy(raw, 0, ret, att.length, raw.length);
		}
		else
			ret = raw;
		return ret;
	}

	public long getIoTime() {
		return ioTime;
	}

	public void setIoTime(long ioTime) {
		this.ioTime = ioTime;
	}

	public String getPeerAddr() {
		return peerAddr;
	}

	public void setPeerAddr(String peerAddr) {
		this.peerAddr = peerAddr;
	}

	/**
	 * ��Ϣ���л���
	 */
	public String toString(){
		return getRawPacketString();
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		if( priority> IMessage.PRIORITY_MAX )
			priority = IMessage.PRIORITY_MAX;
		else if( priority< IMessage.PRIORITY_LOW )
			priority = IMessage.PRIORITY_LOW;
		this.priority = priority;
	}

	public String getTxfs() {
		return txfs;
	}

	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}
	
	public int length(){
		//֡�̶�����+����������
		return 13+head.dlen;
	}
	
	public void setPrefix(byte[] pre){
		this.prefix = pre;
	}
	
	/**
	 * ����ʧ��Ӧ��
	 * 
	 */
	public MessageZj createSendFailReply(){
		return createExtErrorReply((byte)0xF2);
	}

	//�㽭��Լ��������ʱ�����ܷ������Ľ����쳣��������ͨ��������ʧ�ܣ�����ʱ�ȴ���
	private MessageZj createExtErrorReply(byte errcode){
		MessageZj msg = new MessageZj();
		msg.setIoTime(System.currentTimeMillis());
		msg.setPeerAddr(this.getPeerAddr());
		msg.setTxfs(this.getTxfs());
		msg.setSource(this.getSource());
		
		MessageZjHead h = msg.head;
		h.c_dir = 1;
		h.c_expflag = 1;
		h.c_func = this.head.c_func;
		h.dlen = 0x01;
		h.fseq = this.head.fseq;
		h.iseq = 0;
		h.msta = this.head.msta;
		h.rtua = this.head.rtua;
		h.rtua_a1 = this.head.rtua_a1;
		h.rtua_a2 = this.head.rtua_a2;
		h.rtua_b1b2 = this.head.rtua_b1b2;
		
		byte [] bts = new byte[1];
		bts[0] = errcode;
		msg.data = ByteBuffer.wrap(bts);
		return msg;

/*		//����ԭʼ��Ϣ.
		byte c = 0;
		msg._cs = 0;
		ByteBuffer raw = ByteBuffer.wrap(new byte[14]);
		raw.put((byte) 0x68);
		msg._cs += 0x68;
		c = (byte)( (h.rtua>>24) & 0xFF);
		raw.put(c);
		msg._cs += c;
		c = (byte)( (h.rtua>>16) & 0xFF);
		raw.put(c);
		msg._cs += c;
		//�ߵ�
		c = (byte)( (h.rtua) & 0xFF);
		raw.put(c);
		msg._cs += c;
		c = (byte)( (h.rtua>>8) & 0xFF);
		raw.put(c);
		msg._cs += c;
		
		short iTemp = 0;
		iTemp |= (short) h.msta;
		iTemp |= (short) (h.fseq << 6);
		iTemp |= (short) (h.iseq << 13);
		c = (byte) (iTemp & 0xFF);
		raw.put(c);
		msg._cs += c;
		c = (byte) (iTemp >> 8);
		raw.put(c);
		msg._cs += c;

		raw.put((byte) 0x68);
		msg._cs += 0x68;

		c = h.c_func; // & 0x3f
		c |= 0x40 & (h.c_expflag << 6);
		c |= 0x80 & (h.c_dir << 7);
		raw.put(c);
		msg._cs += c;
		
		// ģ�鷢��Ԥ�������Ѿ��������
		iTemp = h.dlen;
		c = (byte) (iTemp & 0xFF);
		raw.put(c);
		msg._cs += c;
		c = (byte) (iTemp >> 8);
		raw.put(c);
		msg._cs += c;
		
		//put data field
		raw.put(bts[0]);
		msg._cs += bts[0];
		// check sum
		h.cs = msg._cs;

		raw.put(h.cs);
		raw.put((byte) 0x16);
		raw.flip();
*/	
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCmdId() {
		return cmdId;
	}

	public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}

	public int getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public boolean isExceptionPacket(){
		return head.c_expflag == 0x01;
	}
	
	public byte getErrorCode(){
		if( isExceptionPacket() ){
			data.rewind();
			if( data.remaining()>0 )
				return data.get(0);
		}
		return 0;
	}

	public boolean isHeartbeat() {
		return head.c_func == MessageConst.ZJ_FUNC_HEART;
	}
}
