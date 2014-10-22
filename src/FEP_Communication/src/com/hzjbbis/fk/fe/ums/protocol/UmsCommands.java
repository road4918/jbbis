/**
 * UMS协议的命令处理
 */
package com.hzjbbis.fk.fe.ums.protocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.sockclient.SimpleSocket;

/**
 * @author bhw
 *
 */
public class UmsCommands {
	private static final Logger log = Logger.getLogger(UmsCommands.class);
	
	private List<UmsField> checkPasswordFields;			//登录UMS下行命令
	private List<UmsField> heartBeatFields;				//1003 链接保持报文
	private List<UmsField> sendSMSFields;				//3011发送消息报文格式(支持超长短信发生)
	private List<UmsField> sendRtuSMSFields;			//3002发送消息报文格式(固定长度短信280)
	private List<UmsField> retrieveSMSFields;			//3012收取消息报文(支持长短信)
	private List<UmsField> genReplyFields;				//通用错误应答
	private List<UmsField> smsReplyFields;				//收到短信的应答字段列表
	private List<UmsField> smsConfirmFields;			//收到消息后，给UMS中心回执
	
	//内部属性
	private static final Map<String,UmsField> emptyParam = new HashMap<String,UmsField>();
	private final byte[] buffer = new byte[4096];
	
	private String readReply(SimpleSocket socket){
		int offset = 0, len = buffer.length-offset;
		int n = -1;
		//1. 先读取长度
		n = socket.read(buffer, offset, len);
		if( n<=0 )
			return null;
		offset += n; len -=n;
		int f1Len = genReplyFields.get(0).getLength();
		int toRead = Integer.parseInt(new String(buffer,0,f1Len).trim());
		while( (offset< (toRead+f1Len)) && n>0 ){
			n = socket.read(buffer, offset, len);
			offset += n; len -=n;
		}
		return new String(buffer,0,offset);
	}
	
	public boolean login(SimpleSocket socket,String appid,String pwd ){
		Map<String,UmsField> param = new HashMap<String,UmsField>();
		UmsField field = new UmsField();
		field.setName(UmsField.FIELD_APPID);
		field.setValue(appid);
		param.put(field.getName(),field );
		field = new UmsField();
		field.setName(UmsField.FIELD_PWD);
		field.setValue(pwd);
		param.put(field.getName(),field );

		String strCommand = createCommand(checkPasswordFields,param);
		if( socket.write(strCommand)<=0 )
			return false;
		String strReply = readReply(socket);
		int f1Len = genReplyFields.get(0).getLength();
		int f2Len = genReplyFields.get(1).getLength();
		String retCode = strReply.substring(f1Len, f1Len+f2Len);
		if( "0000".equals(retCode) ){
			log.info("login UMS success. reply="+strReply);
			return true;
		}
		log.warn("登录UMS失败，原因＝"+strReply.substring(f1Len+f2Len));
		socket.close();
		return false;
	}
	
	public boolean heartBeat( SimpleSocket socket ){
		String strCommand = createCommand(heartBeatFields,emptyParam);
		if( socket.write(strCommand)<=0 )
			return false;
		String strReply = readReply(socket);
		int f1Len = genReplyFields.get(0).getLength();
		int f2Len = genReplyFields.get(1).getLength();
		String retCode = strReply.substring(f1Len, f1Len+f2Len);
		if( "0000".equals(retCode) )
			return true;
		log.warn("发送心跳失败，原因＝"+strReply.substring(f1Len+f2Len));
		return false;
	}
	
	public Map<String,String> retrieveSMS( SimpleSocket socket, String appid){
		//yj 解决取短信命令组请求帧有误的问题：增加SubType和AppId字段
		Map<String,UmsField> param = new HashMap<String,UmsField>();
		UmsField field = new UmsField();
		field.setName(UmsField.FIELD_SUBTYPE);
		field.setValue("  ");
		param.put(field.getName(),field );		
		field = new UmsField();
		field.setName(UmsField.FIELD_APPID);
		field.setValue(appid);
		param.put(field.getName(),field );
		
		String strCommand = createCommand(retrieveSMSFields,param);
		if (log.isDebugEnabled())
			log.debug("retrieveSMS strCommand="+strCommand);
		
		if( socket.write(strCommand)<=0 )
			return null;
		String strReply = readReply(socket);
		if( null == strReply )		//客户端被关闭，或者读数据异常，返回NULL。
			return null;
		if (log.isDebugEnabled())
			log.debug("retrieveSMS strReply="+strReply);
		int f1Len = genReplyFields.get(0).getLength();
		int f2Len = genReplyFields.get(1).getLength();
		String retCode = strReply.substring(f1Len, f1Len+f2Len);
		if( "1162".equals(retCode) ){
			//无短信。
			return null;
		}
		if ( "1041".equals(retCode) ){//短信接收异常,短信网关回应未登录
			log.warn(appid+" 短信接收异常,短信网关回应未登录");
			return null;
		}
		if( "2001".equals(retCode) ){
			Map<String,String> smsRep = parseReply(strReply);
			if (log.isDebugEnabled())
				log.debug("retrieveSMS smsRep="+smsRep);
			//有短信,先回复确认。
			Map<String,UmsField> para = new HashMap<String,UmsField>();
			field = new UmsField();
			field.setName(UmsField.FIELD_SERIALNO);
			field.setValue(smsRep.get(UmsField.FIELD_SERIALNO));
			para.put(field.getName(),field);
			field = new UmsField();
			field.setName(UmsField.FIELD_BATCHNO);
			field.setValue(smsRep.get(UmsField.FIELD_BATCHNO));
			para.put(field.getName(),field);
			String confirm = createCommand(this.smsConfirmFields,para);
			socket.write(confirm);
			return smsRep;
		}
		return null;
	}

	public int sendUserMessage(SimpleSocket socket,String mobilePhone, String content, String appid,String subAppId,String replyAddr){
		//短信类型0表示中文;1表示英文;2表示UCS2码;21,4为压缩PDU7801
		return sendMessage(socket,mobilePhone,content,"0", appid, subAppId, replyAddr );
	}

	public int sendRtuMessage(SimpleSocket socket,String mobilePhone, String content, String appid, String subAppId, String replyAddr){
		//短信类型0表示中文;1表示英文;2表示UCS2码;21,4为压缩PDU7801
		return sendMessage(socket,mobilePhone,content,"21",appid, subAppId, replyAddr);
	}

	public int sendMessage(SimpleSocket socket,String mobilePhone, String content, String msgType,String appid, String subAppId ,String replyAddr ){
		Map<String,UmsField> param = new HashMap<String,UmsField>();
		UmsField field = new UmsField();
		field.setName(UmsField.FIELD_MOBILES);
		field.setValue(mobilePhone);
		param.put(field.getName(),field );				
		
		field = new UmsField();
		field.setName(UmsField.FIELD_APPID);
		field.setValue(appid);
		param.put(field.getName(),field );
		
		field = new UmsField();
		field.setName(UmsField.FIELD_REPLY);
		if( null == replyAddr )
			replyAddr = "";
		field.setValue(replyAddr);
		param.put(field.getName(),field );
		
		if( null == msgType || msgType.length()==0 )
			msgType = "21";
		field = new UmsField();
		field.setName(UmsField.FIELD_MSGTYPE);
		field.setValue(msgType);
		param.put(field.getName(),field );
		
		if( null != subAppId && subAppId.length()>0 ){
			field = new UmsField();
			field.setName(UmsField.FIELD_SUBAPP);
			field.setValue(subAppId);
			param.put(field.getName(),field );
		}
		String strCommand=null;
		if (msgType.equals("0")){//中文采用3012变长内容接口
			field = new UmsField();
			field.setName(UmsField.FIELD_CONTENT);
			field.setValue(content);
			param.put(field.getName(),field );
			strCommand = createCommand(sendSMSFields,param);
		}
		else{					//rtu报文采用3002定长接口
			field = new UmsField();
			field.setName(UmsField.FIELD_RTUCONTENT);
			field.setValue(content);
			param.put(field.getName(),field );
			strCommand = createCommand(sendRtuSMSFields,param);
		}
		if( log.isDebugEnabled() )
			log.debug("sendMessage strCommand="+strCommand);
		if( socket.write(strCommand)<=0 )
			return -1;
		String strReply = readReply(socket);
		if (strReply==null||strReply.length()<4)
			return -1;

		int f1Len = genReplyFields.get(0).getLength();
		int f2Len = genReplyFields.get(1).getLength();
		String retCode = strReply.substring(f1Len, f1Len+f2Len);
		
		if( "0000".equals(retCode) ){
			log.info("send UMS success. reply="+strReply);
			return 0;
		}
		log.warn("发送失败，原因＝"+strReply.substring(f1Len+f2Len)+";strReply="+strReply);
		return -2;
	}

	/**
	 * 通用的命令字符串生成方法。
	 * @param define：下行命令配置信息
	 * @param param： 下行命令参数
	 * @return
	 */
	public String createCommand(List<UmsField>define,Map<String,UmsField> param){
		//1. 计算报文长度
		int len = 0;
		//2. 输出的结果
		StringBuffer sb = new StringBuffer(1024);
		for(int i=1; i<define.size(); i++ ){
			UmsField field = define.get(i);
			//从map中找参数
			UmsField p = param.get(field.getName());

			if( field.getLength()>0 ){
				len += field.getLength();
				if( null != p )
					sb.append(String.format("%-"+field.getLength()+"s", p.getValue()));
				else
					sb.append(String.format("%-"+field.getLength()+"s", field.getDefValue()));
			}
			else{
				//变长，最后一个字段
				assert( null != p);
				try{
					len += p.getValue().getBytes("GBK").length;
				}catch(Exception e){
					log.warn("getValue().getBytes(\"GBK\") exception:",e);
				}
				sb.append( p.getValue() );
			}
		}
		sb.insert(0, String.format("%-"+define.get(0).getLength()+"s", len));
		return sb.toString();
	}
	
	private Map<String,String> parseReply(String rep){
		Map<String,String> map = new HashMap<String,String>();
		int offset = 0;
		for(UmsField f: this.smsReplyFields ){
			if( f.getLength()>0 ){
				map.put(f.getName(), rep.substring(offset, offset+f.getLength()).trim());
				offset += f.getLength();
			}
			else{
				map.put(f.getName(), rep.substring(offset).trim() );
				break;
			}
		}
		return map;
	}
	
	public void setCheckPasswordFields(List<UmsField> checkPassword1001) {
		this.checkPasswordFields = checkPassword1001;
	}
	public void setHeartBeatFields(List<UmsField> heart1003) {
		this.heartBeatFields = heart1003;
	}
	public void setSendSMSFields(List<UmsField> sendSMS3011) {
		this.sendSMSFields = sendSMS3011;
	}
	public void setRetrieveSMSFields(List<UmsField> retrieveSMS3012) {
		this.retrieveSMSFields = retrieveSMS3012;
	}

	public void setGenReplyFields(List<UmsField> genReplyFields) {
		this.genReplyFields = genReplyFields;
	}

	public void setSmsReplyFields(List<UmsField> smsReplyFields) {
		this.smsReplyFields = smsReplyFields;
	}

	public void setSmsConfirmFields(List<UmsField> smsConfirmFields) {
		this.smsConfirmFields = smsConfirmFields;
	}

	public void setSendRtuSMSFields(List<UmsField> sendRtuSMS3002) {
		this.sendRtuSMSFields = sendRtuSMS3002;
	}
	
}
