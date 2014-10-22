/**
 * ��ϵͳ���á������ļ�Ϊapplication.properties��
 * ��ʽ��
 * [ip:]port [?������1][=����ֵ1][&������2][=����ֵ2]... 
 * gate.tcp.servers=127.0.0.1:1002?name=gprs-t-31&bufLength=10240&requestNum=500;127.0.0.1:10004?name=gprs-t-32
 * gate.udp.servers=127.0.0.1:1003?name=gprs-u-31;127.0.0.1:1005?name=gprs-u-32
 * 
 */
package com.hzjbbis.fk.gate.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.socket.IClientIO;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseSocketServer;
import com.hzjbbis.fk.gate.event.GateFEEventHandler;
import com.hzjbbis.fk.gate.event.GateRTUEventHandler;
import com.hzjbbis.fk.message.IMessageCreator;
import com.hzjbbis.fk.message.gate.MessageGateCreator;
import com.hzjbbis.fk.message.zj.MessageZjCreator;
import com.hzjbbis.fk.sockserver.SyncUdpServer;
import com.hzjbbis.fk.sockserver.TcpSocketServer;
import com.hzjbbis.fk.sockserver.io.SimpleIoHandler;
import com.hzjbbis.fk.sockserver.io.SimpleUdpIoHandler;
import com.hzjbbis.fk.utils.ApplicationContextUtil;

/**
 * @author bhw
 * 2009-01-10 18:17
 */
public class ApplicationPropertiesConfig {
	private static final Logger log = Logger.getLogger(ApplicationPropertiesConfig.class);
	private static final ApplicationPropertiesConfig config = new ApplicationPropertiesConfig();
	
	private String gateTcpServers;
	private String gateUdpServers;
	private String feServer;
	private String monitorServer;
	
	private GateFEEventHandler feServerEventHandler;
	private String feServerEventHandlerId = "gate.event.handler.fe";
	private GateRTUEventHandler rtuServerEventHandler;
	private String rtuServerEventHandlerId = "gate.event.handler.rtu";
	private BasicEventHook monitorEventHandler;
	private String monitorEventHandlerId = "monitor.event.handler";
	private String monitorMessageCreator = "messageCreator.Monitor";

	private List<BaseSocketServer> socketServers = new ArrayList<BaseSocketServer>();
	private List<BasicEventHook> eventHandlers = new ArrayList<BasicEventHook>();
	//����SocketServer��˵����Ҫ���õ�Ĭ�����Զ�������
	//1.name���ԣ����û�����ã�����gprs-ip(���һ�Σ�-port-t(or-u);
	//2.bufLength 512 �����ն�,10240��������
	//3.requestNum 500
	//4.

	private ApplicationPropertiesConfig(){}
	
	public static final ApplicationPropertiesConfig getInstance(){
		return config;
	}
	
	public void setGateTcpServers(String gateTcpServers) {
		this.gateTcpServers = gateTcpServers.trim();
	}
	
	@SuppressWarnings("unchecked")
	private void parseGateTcpServers() {
		if( null == gateTcpServers || gateTcpServers.length()<2 )
			return;
		String[] urls = this.gateTcpServers.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			int port = 0, bufLength = 1024;
			String param = null;
			try{
				param = result.get("port");
				if( null == param ){
					log.error("TCP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("TCP Socket Server config exception,port="+param,e);
				continue;
			}
			TcpSocketServer gateServer = new TcpSocketServer();
			gateServer.setPort(port);
			param = result.get("ip");
			if( null != param )
				gateServer.setIp(param);
			//��ʼ����tcpSocketServer�Ĳ���.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+port+"-t";
			}
			gateServer.setName(param);
			
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gateServer.setBufLength(bufLength);
			
			int ioThreadSize = 2;
			param = result.get("ioThreadSize");
			if( null != param ){
				try{
					ioThreadSize = Integer.parseInt(param);
					gateServer.setIoThreadSize(ioThreadSize);
				}catch(Exception e){}
			}
			IMessageCreator messageCreator = new MessageZjCreator();
			param = result.get("messageCreator");
			if( null != param ){
				//ֻ�ܶ���bean id����springȡ��
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
			gateServer.setMessageCreator(messageCreator);
			
			IClientIO ioHandler = new SimpleIoHandler();
			param = result.get("ioHandler");
			if( null != param ){
				IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
				if( null == ioh ){
					try{
						Class clz = Class.forName(param);
						ioh = (IClientIO)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != ioh )
					ioHandler = ioh;
			}
			gateServer.setIoHandler(ioHandler);
			
			param = result.get("txfs");
			if( null != param )
				gateServer.setTxfs(param);
			
			param = result.get("timeout");
			int timeout = 30*60;
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gateServer.setTimeout(timeout);
			
			socketServers.add(gateServer);
		}
		if( null == rtuServerEventHandler )
			rtuServerEventHandler = (GateRTUEventHandler)ApplicationContextUtil.getBean(rtuServerEventHandlerId);
		if( null != rtuServerEventHandler ){
			rtuServerEventHandler.addSource(socketServers.toArray());
			boolean found = false;
			for(BasicEventHook handler: eventHandlers ){
				if( handler == rtuServerEventHandler ){
					found = true;
					break;
				}
			}
			if( !found )
				eventHandlers.add(rtuServerEventHandler);
		}
	}

	public void setGateUdpServers(String gateUdpServers) {
		this.gateUdpServers = gateUdpServers.trim();
	}
	
	@SuppressWarnings("unchecked")
	private void parseGateUdpServers() {
		if( null == gateUdpServers || gateUdpServers.length() <2 )
			return;
		String[] urls = this.gateUdpServers.split(";");
		for(String url: urls ){
			Map<String,String> result = parseUrlConfig(url);
			int port = 0, bufLength = 1024;
			String param = null;
			try{
				param = result.get("port");
				if( null == param ){
					log.error("UDP Socket Server config miss port");
					continue;
				}
				port = Integer.parseInt(param);
			}catch(Exception e){
				log.error("UDP Socket Server config exception,port="+param,e);
				continue;
			}
			SyncUdpServer gateServer = new SyncUdpServer();
			gateServer.setPort(port);
			param = result.get("ip");
			if( null != param )
				gateServer.setIp(param);
			//��ʼ����tcpSocketServer�Ĳ���.
			param = result.get("name");
			if( null == param ){
				param = "gprs-"+port+"-u";
			}
			gateServer.setName(param);
			
			param = result.get("bufLength");
			if( null != param ){
				try{
					bufLength = Integer.parseInt(param);
				}catch(Exception e){
					log.error("bufLength config err:"+param);
				}
			}
			gateServer.setBufLength(bufLength);

			IMessageCreator messageCreator = new MessageZjCreator();
			param = result.get("messageCreator");
			if( null != param ){
				//ֻ�ܶ���bean id����springȡ��
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
			gateServer.setMessageCreator(messageCreator);
			
			IClientIO ioHandler = new SimpleUdpIoHandler();
			param = result.get("ioHandler");
			if( null != param ){
				IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
				if( null == ioh ){
					try{
						Class clz = Class.forName(param);
						ioh = (IClientIO)clz.newInstance();
					}catch(Exception e){}
				}
				if( null != ioh )
					ioHandler = ioh;
			}
			gateServer.setIoHandler(ioHandler);
			
			param = result.get("txfs");
			if( null != param )
				gateServer.setTxfs(param);
			
			param = result.get("timeout");
			int timeout = 30*60;
			if( null != param ){
				try{
					timeout = Integer.parseInt(param);
				}catch(Exception e){}
			}
			gateServer.setTimeout(timeout);
			
			socketServers.add(gateServer);
		}
		if( null == rtuServerEventHandler )
			rtuServerEventHandler = (GateRTUEventHandler)ApplicationContextUtil.getBean(rtuServerEventHandlerId);
		if( null != rtuServerEventHandler ){
			rtuServerEventHandler.addSource(socketServers.toArray());
			boolean found = false;
			for(BasicEventHook handler: eventHandlers ){
				if( handler == rtuServerEventHandler ){
					found = true;
					break;
				}
			}
			if( !found )
				eventHandlers.add(rtuServerEventHandler);
		}
	}

	public void setFeServer(String feServers) {
		this.feServer = feServers.trim();
	}
	
	@SuppressWarnings("unchecked")
	private void parseFeServer() {
		Map<String,String> result = parseUrlConfig(this.feServer);
		int port = 0, bufLength = 10240;
		String param = null;
		try{
			param = result.get("port");
			if( null == param ){
				log.error("Front End TCP Socket Server config miss port");
				return;
			}
			port = Integer.parseInt(param);
		}catch(Exception e){
			log.error("Front End TCP Socket Server config exception,port="+param,e);
			return;
		}
		
		TcpSocketServer socketServer = new TcpSocketServer();
		socketServer.setPort(port);
		param = result.get("ip");
		if( null != param )
			socketServer.setIp(param);
		//��ʼ����tcpSocketServer�Ĳ���.
		param = result.get("name");
		if( null == param ){
			param = "fe-"+port;
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
			//ֻ�ܶ���bean id����springȡ��
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
		socketServer.setMessageCreator(messageCreator);
		
		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
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
		if( null == feServerEventHandler ){
			feServerEventHandler = (GateFEEventHandler)ApplicationContextUtil.getBean(feServerEventHandlerId);
		}
		feServerEventHandler.setSource(socketServer);
		eventHandlers.add(feServerEventHandler);
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
		//��ʼ����tcpSocketServer�Ĳ���.
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
			//ֻ�ܶ���bean id����springȡ��
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
		mServer.setMessageCreator(messageCreator);
		
		IClientIO ioHandler = new SimpleIoHandler();
		param = result.get("ioHandler");
		if( null != param ){
			IClientIO ioh = (IClientIO)ApplicationContextUtil.getBean(param);
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
		//[ip:]port [?������1][=����ֵ1][&������2][=����ֵ2]... 
		String hostAddr = null, params = null;
		int index =  url.indexOf("?");
		if( index>0 ){
			hostAddr = url.substring(0, index).trim();
			params = url.substring(index+1).trim();
		}
		else{
			hostAddr = url.trim();
		}
		if( null != hostAddr ){
			index = hostAddr.indexOf(":");
			if( index>0 ){
				String ip = hostAddr.substring(0, index).trim();
				String port = hostAddr.substring(index+1).trim();
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
					String name = param.substring(0, index).trim();
					String value = param.substring(index+1).trim();
					result.put(name, value);
				}
			}
		}
		return result;
	}

	public void setFeServerEventHandler(GateFEEventHandler feServerEventHandler) {
		this.feServerEventHandler = feServerEventHandler;
	}

	public void setFeServerEventHandlerId(String feServerEventHandlerId) {
		this.feServerEventHandlerId = feServerEventHandlerId;
	}

	public void setRtuServerEventHandler(GateRTUEventHandler rtuServerEventHandler) {
		this.rtuServerEventHandler = rtuServerEventHandler;
	}

	public void setRtuServerEventHandlerId(String rtuServerEventHandlerId) {
		this.rtuServerEventHandlerId = rtuServerEventHandlerId;
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
		this.parseGateTcpServers();
		this.parseGateUdpServers();
		this.parseFeServer();
		this.parseMonitorServer();
	}
}
