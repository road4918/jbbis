/**
 * 简化系统配置。配置文件为application.properties。
 * 格式：
 * [ip:]port [?参数名1][=参数值1][&参数名2][=参数值2]... 
 * gate.tcp.servers=127.0.0.1:1002?name=gprs-t-31&bufLength=10240&requestNum=500;127.0.0.1:10004?name=gprs-t-32
 * gate.udp.servers=127.0.0.1:1003?name=gprs-u-31;127.0.0.1:1005?name=gprs-u-32
 * 
 */
package com.hzjbbis.fk.fe.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.clientmod.ClientModule;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.socket.IClientIO;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseSocketServer;
import com.hzjbbis.fk.fe.ChannelManage;
import com.hzjbbis.fk.fe.fiber.FiberManage;
import com.hzjbbis.fk.fe.gprs.GateMessageEventHandler;
import com.hzjbbis.fk.fe.ums.SmsMessageEventHandler;
import com.hzjbbis.fk.fe.ums.UmsModule;
import com.hzjbbis.fk.fe.ums.protocol.UmsCommands;
import com.hzjbbis.fk.message.IMessageCreator;
import com.hzjbbis.fk.message.gate.MessageGateCreator;
import com.hzjbbis.fk.sockserver.TcpSocketServer;
import com.hzjbbis.fk.sockserver.io.SimpleIoHandler;
import com.hzjbbis.fk.utils.ApplicationContextUtil;

/**
 * @author bhw
 * 2009-01-10 18:17
 */
public class ApplicationPropertiesConfig {
	private static final Logger log = Logger.getLogger(ApplicationPropertiesConfig.class);
	private static final ApplicationPropertiesConfig config = new ApplicationPropertiesConfig();

	//GPRS网关连接的公共属性设置
	private int bufLength = 10240;
	private int timeout = 2;
	private int heartInterval = 1800;
	private int requestNum = 500;
	
	//UMS网关连接的公共属性设置
	private String umsServerAddr;
	private int umsSendSpeed = 1000;
	private int sendUserLimit = 10;
	private int sendRtuLimit = 50;
	private int retrieveMsgLimit = 30;
	private long noUpLogAlertTime = 1800000;
	private String noUpAlertMobiles = "";
	private String noUpAlertContent = "警告：30分钟没有收到短信上行";
	private String umsProtocolId = "ums.protocol";

	private String gprsGateClients;
	private String smsGateClients;
	private String bpServer;
	private String monitorServer;
	
	private GateMessageEventHandler gprsMessageEventHandler;
	private String gprsMessageEventHandlerId = "fe.event.handle.gprs";
	private SmsMessageEventHandler smsMessageEventHandler;
	private String smsMessageEventHandlerId = "fe.event.handle.ums";
	private BasicEventHook bpMessageEventHandler,monitorEventHandler;
	private String bpMessageEventHandlerId = "bpserver.event.hook";
	private String monitorEventHandlerId = "monitor.event.handler";
	private String monitorMessageCreator = "messageCreator.Monitor";

	private List<ClientModule> gprsClientModules = new ArrayList<ClientModule>();
	private List<UmsModule> umsClientModules = new ArrayList<UmsModule>();
	private List<BaseSocketServer> socketServers = new ArrayList<BaseSocketServer>();
	private List<BasicEventHook> eventHandlers = new ArrayList<BasicEventHook>();
	//对于SocketServer来说，需要配置的默认属性定义如下
	//1.name属性，如果没有配置，则按照gprs-ip(最后一段）-port-t(or-u);
	//2.bufLength 512 对于终端,10240对于其它
	//3.requestNum 500
	//4.

	private ApplicationPropertiesConfig(){}
	
	public static final ApplicationPropertiesConfig getInstance(){
		return config;
	}
	
	public void setGprsGateClients(String gprsClients) {
		this.gprsGateClients = gprsClients.trim();
	}
	
	public boolean addGprsGates(String clientsUrl){
		List<ClientModule> gateClients = createGprsGateClients(clientsUrl);
		boolean result = false;
		for(ClientModule gate: gateClients ){
			gate.init();
			ChannelManage.getInstance().addGprsClient(gate);
			FasSystem.getFasSystem().addModule(gate);
			gate.start();
			result = true;
		}
		return result;
	}
	
	public boolean addGprsGate(String hostIp,int port, String gateName ){
		String url = hostIp + ":" + port ;
		if( null != gateName && gateName.length()>=1 ){
			url += "?name=" + gateName;
		}
		return addGprsGates(url);
	}
	
	@SuppressWarnings("unchecked")
	public List<ClientModule> createGprsGateClients(String clientsUrl){
		List<ClientModule> clients = new ArrayList<ClientModule>(); 
		
		if( null == clientsUrl || clientsUrl.length()<2 )
			return clients;

		String[] urls = clientsUrl.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			String ip=null, param = null,gateIpPostFix=null;
			int port = 0;
			try{
				ip = result.get("ip");
				int index = ip.lastIndexOf(".");
				if( index>0 ){
					gateIpPostFix = ip.substring(index+1);
				}
				else{
					gateIpPostFix = ip;
				}
				param = result.get("port");
				if( null == param ){
					log.error("TCP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("gprs client config exception,port="+param,e);
				continue;
			}
			
			ClientModule gprsClient = new ClientModule();
			gprsClient.setModuleType(IModule.MODULE_TYPE_GPRS_CLIENT);
			gprsClient.setHostIp(ip);
			gprsClient.setHostPort(port);

			//开始设置tcpSocketServer的参数.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+gateIpPostFix;
			}
			gprsClient.setName(param);
			
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gprsClient.setBufLength(bufLength);
			
			IMessageCreator messageCreator = new MessageGateCreator();
			param = result.get("messageCreator");
			if( null != param ){
				//只能定义bean id，从spring取。
				IMessageCreator mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
				if( null == mc ){
					try{
						Class clz = Class.forName(param);
						mc = (IMessageCreator)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != mc ){
					messageCreator = mc;
				}
			}
			gprsClient.setMessageCreator(messageCreator);
			
			param = result.get("txfs");
			if( null != param )
				gprsClient.setTxfs(param);
			else
				gprsClient.setTxfs("02");
			
			param = result.get("timeout");
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gprsClient.setTimeout(timeout);
			
			if( null == gprsMessageEventHandler )
				gprsMessageEventHandler = (GateMessageEventHandler)ApplicationContextUtil.getBean(gprsMessageEventHandlerId);
			if( null == gprsMessageEventHandler ){
				log.error("gprsMessageEventHandler == null.");
				return clients;
			}
			gprsClient.setEventHandler(gprsMessageEventHandler);
			//还需要设置公共属性
			gprsClient.setHeartInterval(heartInterval);
			gprsClient.setRequestNum(requestNum);
			
			clients.add(gprsClient);
		}
		return clients;
	}
	
	private void parseGprsGateClients() {
		gprsClientModules.addAll(createGprsGateClients(gprsGateClients) );
	}

	public void setSmsGateClients(String smsGateClients) {
		this.smsGateClients = smsGateClients.trim();
	}

	public boolean addUmsClients(String clientsUrl ){
		List<UmsModule> clients = createSmsGateClients(clientsUrl);
		boolean result = false;
		for( UmsModule ums: clients ){
			ChannelManage.getInstance().addUmsClient(ums);
			FiberManage.getInstance().schedule(ums);
			FasSystem.getFasSystem().addModule(ums);
			ums.start();
			result = true;
		}
		return result;
	}
	
	public boolean addUmsClient(String appid, String pwd){
		return addUmsClients(appid + ":" + pwd );
	}
	
	@SuppressWarnings("unchecked")
	private List<UmsModule> createSmsGateClients(String clientsUrl){
		List<UmsModule> clients = new ArrayList<UmsModule>();
		if( null == clientsUrl || clientsUrl.length()<2 )
			return clients;
		
		String hostIp,hostPort;
		if( null == this.umsServerAddr || this.umsServerAddr.length()<5){
			log.error("UMS服务器IP:PORT没有定义");
			return clients;
		}
		int index = this.umsServerAddr.indexOf(":");
		if( index<=0 ){
			log.error("ums服务器地址错误="+umsServerAddr);
			return clients;
		}
		
		hostIp = umsServerAddr.substring(0, index).trim();
		hostPort = umsServerAddr.substring(index+1).trim();
		int port = 0;
		try{
			port = Integer.parseInt(hostPort);
		}catch(Exception e){
			log.error("hostPort="+hostPort);
			return clients;
		}
		
		String[] urls = clientsUrl.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			String appid = null, password = null;
			appid = result.get("ip");
			password = result.get("port");
			if( null == appid ){
				appid = password;
			}

			UmsModule umsClient = new UmsModule();
			umsClient.setHostIp(hostIp);
			umsClient.setHostPort(port);
			umsClient.setAppid(appid);
			umsClient.setApppwd(password);
			umsClient.setFiber(true);
			
			umsClient.setName("ums-"+appid);
			umsClient.setReply("95598"+appid);

			String param = result.get("umsProtocol");
			if( null == param )
				param = umsProtocolId ;
			
			//只能定义bean id，从spring取。
			UmsCommands umsProto = null;
			try{
				umsProto = (UmsCommands)ApplicationContextUtil.getBean(param);
				if( null == umsProto ){
					Class clz = Class.forName(param);
					umsProto = (UmsCommands)clz.newInstance();
				}
			}catch(Exception e){
				log.error("appid="+appid+",channel's umsProtocol error:" + param);
				return clients;
			}
			umsClient.setUmsProtocol(umsProto);
			
			param = result.get("txfs");
			if( null != param )
				umsClient.setTxfs(param);
			
			if( null == smsMessageEventHandler )
				smsMessageEventHandler = (SmsMessageEventHandler)ApplicationContextUtil.getBean(smsMessageEventHandlerId);
			if( null == smsMessageEventHandler ){
				log.error("smsMessageEventHandler == null.");
				return clients;
			}
			umsClient.setEventHandler(smsMessageEventHandler);
			
			//开始设置公共属性
			umsClient.setUmsSendSpeed(umsSendSpeed);
			umsClient.setSendUserLimit(sendUserLimit);
			umsClient.setSendRtuLimit(sendRtuLimit);
			umsClient.setRetrieveMsgLimit(retrieveMsgLimit);
			umsClient.setNoUpLogAlertTime(noUpLogAlertTime);
			if( null != noUpAlertMobiles && noUpAlertMobiles.length()>0 ){
				List<String> mobiles = new ArrayList<String>();
				String[] mbs = noUpAlertMobiles.split(";");
				for(String m: mbs )
					mobiles.add(m.trim());
				umsClient.setSimNoList(mobiles);
			}
			
			umsClient.setAlertContent("UMS Channel:"+appid+". "+noUpAlertContent);
			clients.add(umsClient);
		}
		return clients;
	}

	@SuppressWarnings("unchecked")
	private void parseSmsGateClients() {
		this.umsClientModules.addAll(createSmsGateClients(smsGateClients)); 
	}

	public void setBpServer(String bpServers) {
		this.bpServer = bpServers.trim();
	}
	
	@SuppressWarnings("unchecked")
	private void parseBpServer() {
		Map<String,String> result = parseUrlConfig(this.bpServer);
		int port = 0, bufLength = 10240;
		String param = null;
		try{
			param = result.get("port");
			if( null == param ){
				log.error("Business Processor TCP Socket Server config miss port");
				return;
			}
			port = Integer.parseInt(param);
		}catch(Exception e){
			log.error("Business Processor TCP Socket Server config exception,port="+param,e);
			return;
		}
		
		TcpSocketServer socketServer = new TcpSocketServer();
		socketServer.setPort(port);
		param = result.get("ip");
		if( null != param )
			socketServer.setIp(param);
		//开始设置tcpSocketServer的参数.
		param = result.get("name");
		if( null == param ){
			param = "bp-"+port;
		}
		socketServer.setName(param);
		
		param = result.get("bufLength");
		if( null != param ){
			try{
				bufLength = Integer.parseInt(param);
			}catch(Exception e){
				log.error("bufLength config err:"+param);
			}
		}
		socketServer.setBufLength(bufLength);
		
		int ioThreadSize = 2;
		param = result.get("ioThreadSize");
		if( null != param ){
			try{
				ioThreadSize = Integer.parseInt(param);
				socketServer.setIoThreadSize(ioThreadSize);
			}catch(Exception e){}
		}
		
		IMessageCreator messageCreator = new MessageGateCreator();
		param = result.get("messageCreator");
		if( null != param ){
			//只能定义bean id，从spring取。
			IMessageCreator mc = null;
			try{
				mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
			}catch(Exception e){}
			if( null == mc ){
				try{
					Class clz = Class.forName(param);
					mc = (IMessageCreator)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != mc ){
				messageCreator = mc;
			}
		}
		socketServer.setMessageCreator(messageCreator);

		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = null;
			try{
				ioh = (IClientIO)ApplicationContextUtil.getBean(param);
			}catch(Exception e){}
			if( null == ioh ){
				try{
					Class clz = Class.forName(param);
					ioh = (IClientIO)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != ioh )
				ioHandler = ioh;
		}
		socketServer.setIoHandler(ioHandler);
		
		param = result.get("timeout");
		int timeout = 3*60;
		if( null != param ){
			try{
				timeout = Integer.parseInt(param);
			}catch(Exception e){}
		}
		socketServer.setTimeout(timeout);
		
		socketServers.add(socketServer);
		if( null == bpMessageEventHandler ){
			bpMessageEventHandler = (BasicEventHook)ApplicationContextUtil.getBean(bpMessageEventHandlerId);
		}
		bpMessageEventHandler.setSource(socketServer);
		eventHandlers.add(bpMessageEventHandler);
	}

	public void setMonitorServer(String monitorServers) {
		this.monitorServer = monitorServers.trim();
	}
	
	@SuppressWarnings("unchecked")
	private void parseMonitorServer() {
		if( null == monitorServer || monitorServer.length()<2 )
			return;
		Map<String,String> result = parseUrlConfig(this.monitorServer);
		int port = 0, bufLength = 1024*50;
		String param = null;
		try{
			param = result.get("port");
			if( null == param ){
				log.error("Monitor Socket Server config miss port");
				return;
			}
			port = Integer.parseInt(param);
		}catch(Exception e){
			log.error("Monitor Socket Server config exception,port="+param,e);
			return;
		}
		
		TcpSocketServer mServer = new TcpSocketServer();
		mServer.setPort(port);
		param = result.get("ip");
		if( null != param )
			mServer.setIp(param);
		//开始设置tcpSocketServer的参数.
		param = result.get("name");
		if( null == param ){
			param = "monitor-"+port;
		}
		mServer.setName(param);
		
		param = result.get("bufLength");
		if( null != param ){
			try{
				bufLength = Integer.parseInt(param);
			}catch(Exception e){
				log.error("bufLength config err:"+param);
			}
		}
		mServer.setBufLength(bufLength);
		
		int ioThreadSize = 1;
		param = result.get("ioThreadSize");
		if( null != param ){
			try{
				ioThreadSize = Integer.parseInt(param);
				mServer.setIoThreadSize(ioThreadSize);
			}catch(Exception e){}
		}
		
		IMessageCreator messageCreator = (IMessageCreator)ApplicationContextUtil.getBean(monitorMessageCreator);
		param = result.get("messageCreator");
		if( null != param ){
			//只能定义bean id，从spring取。
			IMessageCreator mc = null;
			try{
				mc = (IMessageCreator)ApplicationContextUtil.getBean(param);
			}catch(Exception exp){
				mc = null;
			}
			if( null == mc ){
				try{
					Class clz = Class.forName(param);
					mc = (IMessageCreator)clz.newInstance();
				}catch(Exception e){
					mc = null;
				}
			}
			if( null != mc ){
				messageCreator = mc;
			}
		}
		mServer.setMessageCreator(messageCreator);
		
		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = null;
			try{
				ioh = (IClientIO)ApplicationContextUtil.getBean(param);
			}catch(Exception e){
				ioh = null;
			}
			if( null == ioh ){
				try{
					Class clz = Class.forName(param);
					ioh = (IClientIO)clz.newInstance();
				}catch(Exception e){}
			}
			if( null != ioh )
				ioHandler = ioh;
		}
		mServer.setIoHandler(ioHandler);
		
		param = result.get("timeout");
		int timeout = 30*60;
		if( null != param ){
			try{
				timeout = Integer.parseInt(param);
			}catch(Exception e){}
		}
		mServer.setTimeout(timeout);
		
		socketServers.add(mServer);
		if( null == monitorEventHandler ){
			monitorEventHandler = (BasicEventHook)ApplicationContextUtil.getBean(monitorEventHandlerId);
		}
		monitorEventHandler.setSource(mServer);
		eventHandlers.add(monitorEventHandler);
	}

	private Map<String,String> parseUrlConfig(String url){
		Map<String,String> result = new HashMap<String,String>();
		//[ip:]port [?参数名1][=参数值1][&参数名2][=参数值2]... 
		String hostAddr = null, params = null;
		int index =  url.indexOf("?");
		if( index>0 ){
			hostAddr = url.substring(0, index);
			params = url.substring(index+1);
		}
		else{
			hostAddr = url;
		}
		if( null != hostAddr ){
			index = hostAddr.indexOf(":");
			if( index>0 ){
				String ip = hostAddr.substring(0, index);
				String port = hostAddr.substring(index+1);
				result.put("ip", ip);
				result.put("port", port);
			}
			else{
				result.put("port", hostAddr);
			}
		}
		if( null != params ){
			String[] paramArray = params.split("&");
			if( null != paramArray ){
				for( String param: paramArray ){
					index = param.indexOf("=");
					if( index<0 ){
						log.error("Server config malformed,miss'=' :"+param);
						continue;
					}
					String name = param.substring(0, index);
					String value = param.substring(index+1);
					result.put(name, value);
				}
			}
		}
		return result;
	}

	public final List<BaseSocketServer> getSocketServers() {
		return socketServers;
	}

	public final List<BasicEventHook> getEventHandlers() {
		return eventHandlers;
	}

	public final void setMonitorEventHandler(BasicEventHook monitorEventHandler) {
		this.monitorEventHandler = monitorEventHandler;
	}

	public final void setMonitorEventHandlerId(String monitorEventHandlerId) {
		this.monitorEventHandlerId = monitorEventHandlerId;
	}

	public final void setMonitorMessageCreator(String monitorMessageCreator) {
		this.monitorMessageCreator = monitorMessageCreator;
	}
	
	public void parseConfig(){
		this.parseGprsGateClients();
		this.parseSmsGateClients();
		this.parseBpServer();
		this.parseMonitorServer();
	}

	public final void setBufLength(int bufLength) {
		this.bufLength = bufLength;
	}

	public final void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public final void setHeartInterval(int heartInterval) {
		this.heartInterval = heartInterval;
	}

	public final void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	public final void setUmsSendSpeed(int umsSendSpeed) {
		this.umsSendSpeed = umsSendSpeed;
	}

	public final void setSendUserLimit(int sendUserLimit) {
		this.sendUserLimit = sendUserLimit;
	}

	public final void setSendRtuLimit(int sendRtuLimit) {
		this.sendRtuLimit = sendRtuLimit;
	}

	public final void setRetrieveMsgLimit(int retrieveMsgLimit) {
		this.retrieveMsgLimit = retrieveMsgLimit;
	}

	public final void setNoUpLogAlertTime(long noUpLogAlertTime) {
		this.noUpLogAlertTime = noUpLogAlertTime;
	}

	public final void setNoUpAlertMobiles(String noUpAlertMobiles) {
		this.noUpAlertMobiles = noUpAlertMobiles;
	}

	public final void setNoUpAlertContent(String noUpAlertContent) {
		this.noUpAlertContent = noUpAlertContent;
	}

	public final void setUmsProtocolId(String umsProtocolId) {
		this.umsProtocolId = umsProtocolId;
	}

	public final void setGprsMessageEventHandler(
			GateMessageEventHandler gprsMessageEventHandler) {
		this.gprsMessageEventHandler = gprsMessageEventHandler;
	}

	public final void setGprsMessageEventHandlerId(String gprsMessageEventHandlerId) {
		this.gprsMessageEventHandlerId = gprsMessageEventHandlerId;
	}

	public final void setSmsMessageEventHandler(
			SmsMessageEventHandler smsMessageEventHandler) {
		this.smsMessageEventHandler = smsMessageEventHandler;
	}

	public final void setSmsMessageEventHandlerId(String smsMessageEventHandlerId) {
		this.smsMessageEventHandlerId = smsMessageEventHandlerId;
	}

	public final void setBpMessageEventHandler(BasicEventHook bpMessageEventHandler) {
		this.bpMessageEventHandler = bpMessageEventHandler;
	}

	public final void setBpMessageEventHandlerId(String bpMessageEventHandlerId) {
		this.bpMessageEventHandlerId = bpMessageEventHandlerId;
	}

	public final List<ClientModule> getGprsClientModules() {
		return gprsClientModules;
	}

	public final List<UmsModule> getUmsClientModules() {
		return umsClientModules;
	}

	public final void setUmsServerAddr(String umsServerAddr) {
		this.umsServerAddr = umsServerAddr;
	}
}
