/**
 * 终端连接到网关的外部通信端口；通信前置机连接到网关内部端口。
 * 终端资产表的通信参数为：外部IP＋外部端口
 * 对于通信下行来说，网关的唯一性标识为：内部ip＋内部端口。
 * 对于短信下行，通道的唯一标识为：appid
 * 
 */
package com.hzjbbis.fk.fe;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.clientmod.ClientModule;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseClientChannel;
import com.hzjbbis.fk.fe.ums.UmsModule;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 *
 */
public class ChannelManage {
	private static final Logger log = Logger.getLogger(ChannelManage.class);
	//可配置属性
	private int rtuHeartbeatInterval = 15*60;	//15分钟心跳间隔。
	private int rtuTransferInterval = 60;		//两次请求1分钟间隔判断
	//内部属性
	private long hbInterval = rtuHeartbeatInterval * 1000;
	private long tfInterval = rtuTransferInterval * 1000;
	
	private static ChannelManage cm = new ChannelManage();
	private ChannelManage(){}
	
	public static ChannelManage getInstance(){
		return cm;
	}
	
	//根据rtua找到终端rtu对象；根据需要，依据activeGprs,activeUms来确定通道
	private final Map<String,BaseClientChannel> mapGates = new HashMap<String,BaseClientChannel>();
	private final Map<String,BaseClientChannel> mapUmsClients = new HashMap<String,BaseClientChannel>();
	private final Map<String,BaseClientChannel> mapGprsClients = new HashMap<String,BaseClientChannel>();
	public boolean testMode = false;	//测试时候，rtu可能没有加载，那么channel选择任何一个GPRS网关下行。
	
	
	public void setRtuHeartbeatInterval(int interval){
		rtuHeartbeatInterval = interval;
		hbInterval = rtuHeartbeatInterval * 1000; 
	}

	public void setRtuTransferInterval(int rtuTransferInterval) {
		this.rtuTransferInterval = rtuTransferInterval;
		tfInterval = rtuTransferInterval * 1000;
	}

	/**
	 * 添加：GPRS网关的客户端连接
	 * @param gprsClient
	 */
	public void addGprsClient(ClientModule gprsClient ){
		mapGates.put(gprsClient.getSocket().getPeerAddr(), gprsClient.getSocket());
		mapGprsClients.put(gprsClient.getSocket().getPeerAddr(), gprsClient.getSocket());
	}
	
	//还需要为UMS短信连接建立IClientChannel映射关系。....
	public void addUmsClient(UmsModule umsClient){
		mapGates.put(umsClient.getPeerAddr(), umsClient);
		mapUmsClients.put(umsClient.getPeerAddr(), umsClient);
	}
	
	/**
	 * 对于普通用户短信发送请求（0X28），需要随机选择一个有效的短信通道下行。
	 * @return
	 */
	public BaseClientChannel getActiveUmsChannel(){
		for( BaseClientChannel channel: mapUmsClients.values() )
			if( channel.isActive() )
				return channel;
		return null;
	}
	
	public BaseClientChannel getActiveGprsChannel(){
		for( BaseClientChannel channel: mapGprsClients.values() )
			if( channel.isActive() )
				return channel;
		return null;
	}
	/**
	 * 当主站命令下行时，需要依据RTUA取Rtu对象，然后依据通道管理决策，获取activeChannel，如ip:port或者appid
	 * ＃＃＃对于指定APPID下行的情况，直接调用 ＃＃＃
	 * @param key: GPRS通信为ip:port；短信UMS平台为appid
	 * @return
	 */
	public IChannel getChannel(String key){
		return mapGates.get(key);
	}
	
	/**
	 * 根据资产表的通信方式，转换为程序使用的通信方式
	 * 		01:短信; 02:GPRS;  03:DTMF;  04:Ethernet;
	 * 		05:红外; 06:RS232; 07:CSD;   08:Radio; 	09:CDMA;
	 * @param commType
	 * @return：1 短信； 2 GPRS/CDMA/Ethernet; 0 其它方式。通信前置机不需要支持
	 */
	private static int communicationType(String commType){
		if( null == commType )
			return -1;
		if( commType.equals("02") || commType.equals("09") || commType.equals("04") )
			return 2;
		else if( commType.equals("01") )
			return 1;
		else
			return 0;
	}
	
	/**
	 * 终端没有指定短信通道情况下，依据RTUA的配置选择通道
	 * 优先级：GRPS通道->短信主通道->备1短信通道->备2短信通道
	 * @param rtua
	 * @return
	 */
	public IChannel getChannel(int rtua){
		IChannel channel = getGPRSChannel(rtua);
		if( null != channel )
			return channel;
		channel = getUmsChannel(null,rtua);
		if( null != channel )
			return channel;
		//如果找不到该终端有效UMS通道，则任选当前有效UMS通道
		//以便终端有机会返回非法呼叫应答
		return getActiveUmsChannel();
	}
	
	/**
	 * 针对主站心跳下行，只能支持GPRS通道。
	 * @param rtua
	 * @return
	 */
	public IChannel getGPRSChannel(int rtua){
		//根据rtua来找RTU对象
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		if( null == rtu ){
			log.warn("终端资料没有加载。rtua="+HexDump.toHex(rtua));
			return null;
		}
		
		//1. 根据终端最新上行时间，取当前可用的通道。
		//解决问题：终端参数为短信，但是实际走GPRS通道。
		long timeSpan = System.currentTimeMillis() - rtu.getLastGprsTime();
		if( timeSpan< (hbInterval*2) && null != rtu.getActiveGprs()){
			long lastReq = rtu.getLastReqTime();
			//long tspan = Math.max(System.currentTimeMillis() - lastReq, lastReq-rtu.getLastGprsTime() ) ;			
			long tspan = Math.abs(System.currentTimeMillis() - lastReq) ;
			if( tspan> tfInterval && rtu.getLastGprsTime() < lastReq  )
				return null;		//走UMS通道
			IChannel channel = getChannel(rtu.getActiveGprs());
			return channel;
		}
		return null;
	}
	
	/**
	 * 终端可能指定短信通道。如果没有指定，依据RTUA的配置选择通道
	 * 优先级：短信主通道->备1短信通道->备2短信通道
	 * @param rtua
	 * @return
	 */
	public IChannel getUmsChannel(String appid, int rtua ){
		IChannel channel = null;
		if( null != appid && appid.length()>0 ){
			channel = getChannel(appid);
			if( null != channel )
				return channel;
		}
		if( 0 == rtua )
			return channel;
		
		//根据rtua来找RTU对象
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		//1. 走短信通道。优先使用当前活动短信通道
		String activeUms = rtu.getActiveUms();
		if( null != activeUms && activeUms.length()>2 )
			return getChannel(activeUms);
		
		//2. 如果终端主通道是GPRS，首先检测对应GPRS网关通信情况
		int cType = communicationType(rtu.getCommType());
		if( cType == 1 ){
			//955983501，子应用号码不取
			activeUms = rtu.getCommAddress().substring(0, 9);
			channel = getChannel(activeUms);
			if( null != channel )
				return channel;
		}
		
		//3. 从备1通道取
		cType = communicationType(rtu.getB1CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB1CommAddress().substring(0, 9);
			channel = getChannel(activeUms);
			if( null != channel )
				return channel;
		}
		
		//4. B1通道失败，检测B2通道
		cType = communicationType(rtu.getB2CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB2CommAddress().substring(0, 9);
			channel = getChannel(activeUms);
			if( null != channel )
				return channel;
		}
		
		//都失败。那么终端不能走短信通道
		log.warn("终端短信通道配置不正确。RTUA="+rtu.getLogicAddress());
		//如果找不到该终端有效UMS通道，则任选当前有效UMS通道
		//以便终端有机会返回非法呼叫应答
		return null;
	}
	
	public static final String getUmsAppId(int rtua){
		//根据rtua来找RTU对象
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		//1. 如果终端主通道是GPRS，首先检测对应GPRS网关通信情况
		int cType = communicationType(rtu.getCommType());
		if( cType <= 0 ){
			log.error("终端主通道不是GPRS/CDMA，或者短信。RTUA＝"+rtu.getLogicAddress());
			return null;
		}
		
		//2. 走短信通道。优先使用当前活动短信通道
		String activeUms = rtu.getActiveUms();
		if( null != activeUms && activeUms.length()>2 )
			return activeUms;
		
		//3. 当前有效短信通道没有设置，则如果主通道是短信，需要从主通道取
		if( cType == 1 ){
			//955983501，子应用号码不取
			activeUms = rtu.getCommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		
		//4. GPRS终端转短信通道情况，以及短信终端主通道失败情况。从备1通道取
		cType = communicationType(rtu.getB1CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB1CommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		//6. B1通道失败，检测B2通道
		cType = communicationType(rtu.getB2CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB2CommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		//都失败。那么终端不能走短信通道
		log.warn("终端短信通道配置不正确。RTUA="+rtu.getLogicAddress());
		return null;
	}
}
