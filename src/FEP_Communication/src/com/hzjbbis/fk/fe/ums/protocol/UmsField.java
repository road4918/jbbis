/**
 * UMSЭ����ֶζ���
 */
package com.hzjbbis.fk.fe.ums.protocol;

/**
 * @author bhw
 *
 */
public class UmsField {
	public static final String FIELD_LENGTH = "Length";
	public static final String FIELD_TRANSTYPE = "TransType";
	public static final String FIELD_SUBTYPE = "SubType";
	public static final String FIELD_APPID = "AppId";
	public static final String FIELD_PWD = "Passwd";

	public static final String FIELD_RETCODE = "RetCode";
	public static final String FIELD_RETMSG = "RetMsg";
	
	public static final String FIELD_APPSERIALNO = "AppSerialNo";		//Ӧ����ˮ��
	//��������0��ʾ����;1��ʾӢ��;2��ʾUCS2��;21,4Ϊѹ��PDU7801
	public static final String FIELD_MSGTYPE = "MessageType";		
	public static final String FIELD_MOBILES = "RecvId";		//���շ���ַ������ֻ���֮���ö��ŷֿ�
	//0��ʾ���ظ�,1��ʾ��Ҫ��ִ,2��ʾ��ظ���3��ʾ��Ҫ��ִ����ظ�
	public static final String FIELD_ACK = "Ack";		
	public static final String FIELD_REPLY = "Reply";		
	public static final String FIELD_PRIORITY = "Priority";		//���ȼ�0-9
	public static final String FIELD_REPEAT = "Rep";			//�ظ�����
	public static final String FIELD_SUBAPP = "SubApp";			//���ȼ�0-9
	public static final String FIELD_CHECKFLAG = "CheckFlag";	//�ظ�����
	public static final String FIELD_CONTENT = "Content";		//�������ݣ�Ŀǰ�ֻ�һ�����֧���һ������659������
	public static final String FIELD_RTUCONTENT = "RtuContent";	//������������280
	
	public static final String FIELD_BATCHNO = "BatchNO";		//����
	public static final String FIELD_SERIALNO = "SerialNO";		//�յ����ŵ���ˮ��
	public static final String FIELD_RETTYPE = "InfoType";		//��Ϣ����
	public static final String FIELD_MSGID = "MsgID";			//���ŵ�ID�ţ�����ǻ�ִ�����ID��Ӧ�ó��򷢶���ʱ��ID�������������ţ����ID��UMS�ṩ
	public static final String FIELD_RECEIVER = "Receive";		//UMS���յ��ط���,��955982
	public static final String FIELD_FROM = "From";				//���ͷ��ֻ���
	public static final String FIELD_RECVDATE = "ReceiveDate";			//���ŵ�ID�ţ�����ǻ�ִ�����ID��Ӧ�ó��򷢶���ʱ��ID�������������ţ����ID��UMS�ṩ
	public static final String FIELD_RECVTIME = "ReceiveTime";		//UMS���յ��ط���,��955982
	
	private int index;
	private String name;
	private int length;
	private String defValue = "";
	private String value;

	public UmsField(){}
	
	public UmsField(String n,String v){
		name = n;
		value = v;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getDefValue() {
		return defValue;
	}
	public void setDefValue(String defValue) {
		this.defValue = defValue;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
