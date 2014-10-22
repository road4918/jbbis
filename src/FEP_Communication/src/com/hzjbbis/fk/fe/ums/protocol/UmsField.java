/**
 * UMS协议的字段定义
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
	
	public static final String FIELD_APPSERIALNO = "AppSerialNo";		//应用流水号
	//短信类型0表示中文;1表示英文;2表示UCS2码;21,4为压缩PDU7801
	public static final String FIELD_MSGTYPE = "MessageType";		
	public static final String FIELD_MOBILES = "RecvId";		//接收方地址，多个手机号之间用逗号分开
	//0表示不回复,1表示需要回执,2表示需回复，3表示需要回执＋需回复
	public static final String FIELD_ACK = "Ack";		
	public static final String FIELD_REPLY = "Reply";		
	public static final String FIELD_PRIORITY = "Priority";		//优先级0-9
	public static final String FIELD_REPEAT = "Rep";			//重复次数
	public static final String FIELD_SUBAPP = "SubApp";			//优先级0-9
	public static final String FIELD_CHECKFLAG = "CheckFlag";	//重复次数
	public static final String FIELD_CONTENT = "Content";		//正文内容，目前手机一般可以支持最长一条短信659个汉字
	public static final String FIELD_RTUCONTENT = "RtuContent";	//定长正文内容280
	
	public static final String FIELD_BATCHNO = "BatchNO";		//批号
	public static final String FIELD_SERIALNO = "SerialNO";		//收到短信的流水号
	public static final String FIELD_RETTYPE = "InfoType";		//信息类型
	public static final String FIELD_MSGID = "MsgID";			//短信的ID号（如果是回执，则此ID是应用程序发短信时的ID；如果是请求短信，则此ID由UMS提供
	public static final String FIELD_RECEIVER = "Receive";		//UMS接收的特服号,如955982
	public static final String FIELD_FROM = "From";				//发送方手机号
	public static final String FIELD_RECVDATE = "ReceiveDate";			//短信的ID号（如果是回执，则此ID是应用程序发短信时的ID；如果是请求短信，则此ID由UMS提供
	public static final String FIELD_RECVTIME = "ReceiveTime";		//UMS接收的特服号,如955982
	
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
