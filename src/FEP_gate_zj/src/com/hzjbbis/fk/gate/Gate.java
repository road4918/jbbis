/**
 * ���ض���ͨ��spring���ö���ն���������TCP/UDP����
 * ͬʱֻ�ܰ���Ψһǰ�û��ӿڷ���
 * ÿ��Socket���񣬶���Ҫ����һ���¼���������
 * �����¼��������Ѿ�������Socket������ˣ�Gate������Ҫ�ٰ���SocketServer����
 * һ����˵��ÿ��Socket�����¼����������ܹ��ã���Ϊ�յ����Ķ���ҵ����ͬ��
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
