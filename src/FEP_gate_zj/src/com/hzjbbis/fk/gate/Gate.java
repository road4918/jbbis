/**
 * 网关对象。通过spring配置多个终端侦听服务（TCP/UDP），
 * 同时只能包含唯一前置机接口服务。
 * 每个Socket服务，都需要配置一个事件处理器。
 * 由于事件处理器已经包含了Socket对象，因此，Gate对象不需要再包含SocketServer对象。
 * 一般来说，每个Socket服务事件处理器不能共用，因为收到报文对象，业务处理不同。
 * 
 */
package com.hzjbbis.fk.gate;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.events.BasicEventHook;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseSocketServer;
import com.hzjbbis.fk.gate.config.ApplicationPropertiesConfig;
import com.hzjbbis.fk.utils.ApplicationContextUtil;

/**
 * @author bhw
 * 2008-06-06 15:52
 */
public class Gate {
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-monitor.xml",
				"classpath*:applicationContext-gate.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		ApplicationContextUtil.setContext(context);
		FasSystem fasSystem = (FasSystem)context.getBean("fasSystem");
		ApplicationPropertiesConfig config = (ApplicationPropertiesConfig)context.getBean("applicationPropertiesConfig");
		config.parseConfig();
		for(BaseSocketServer sockServer: config.getSocketServers())
			fasSystem.addModule(sockServer);
		for(BasicEventHook eventHandler: config.getEventHandlers() )
			fasSystem.addEventHook(eventHandler);
		fasSystem.startSystem();
	}
}
