/**
 * 通信前置机主程序。
 */
package com.hzjbbis.fk.fe;

import java.util.ArrayList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.DbMonitor;
import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.spi.IEventHook;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.fe.config.ApplicationPropertiesConfig;
import com.hzjbbis.fk.fe.fiber.FiberManage;
import com.hzjbbis.fk.fe.fiber.IFiber;
import com.hzjbbis.fk.utils.ApplicationContextUtil;

/**
 * @author bhw
 * 2008-10-31 15:57
 */
public class FeCommunication {

	public static void main(String[] args) {
		//初始化class path
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-monitor.xml",
				"classpath*:applicationContext-db-batch.xml",
				"classpath*:applicationContext-ums.xml",
				"classpath*:applicationContext-fec.xml"
				};
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		ApplicationContextUtil.setContext(context);
		//1. 加载FasSystem，负责所有模块的启动和停止。
		FasSystem fasSystem = (FasSystem)context.getBean("fasSystem");
		fasSystem.setApplicationContext(context);
		
		//2. 加载GateClientManage，确保所有网关通道client对象被加载到ChannelManage。
		GateClientManage gateClients = GateClientManage.getInstance();
		
		ApplicationPropertiesConfig config = ApplicationPropertiesConfig.getInstance();
		config.parseConfig();
		gateClients.setGprsGateClients(config.getGprsClientModules());
		gateClients.setUmsClients(config.getUmsClientModules());

		//先设置bpServer以及MonitorServer模块
		fasSystem.setModules(new ArrayList<IModule>());
		for(IModule mod : config.getSocketServers() ){
			fasSystem.addModule(mod);
		}
		
		fasSystem.setEventHooks(new ArrayList<IEventHook>());
		for(IEventHook hook: config.getEventHandlers() ){
			fasSystem.addEventHook(hook);
		}
		
		for( IModule mod : gateClients.getGprsGateClients() ){
			fasSystem.addModule(mod);
		}
		for( IModule mod : gateClients.getUmsClients() ){
			fasSystem.addModule(mod);
		}
		
		DbMonitor mastDbMonitor = (DbMonitor)context.getBean("master.dbMonitor");
		mastDbMonitor.testDbConnection();
		DbMonitor feDbMonitor = (DbMonitor)context.getBean("fe.dbMonitor");
		feDbMonitor.testDbConnection();
		
		//3. 数据库初始化:Initialize 完成终端初始化加载。
/*		ManageRtu initRtu = (ManageRtu)context.getBean("app.init.rtu");
		if( mastDbMonitor.isAvailable() )
			initRtu.loadComRtu();
*/
		//4. 加载数据库接口模块: 批量保存接口、业务处理DB接口
//		AsyncService asyncService = (AsyncService)context.getBean("asyncService");
//		fasSystem.addUnMonitoredModules(asyncService);

		//5. 加载UMS纤程管理对象。通过配置纤程管理器，使得70个UMS client，只需要5个线程即可。
		FiberManage fiberManage = FiberManage.getInstance();
		fiberManage.setFibers(new ArrayList<IFiber>());
		for(IFiber fiber: config.getUmsClientModules() ){
			fiberManage.schedule(fiber);
		}
		fasSystem.addUnMonitoredModules(fiberManage);
		
		//6. 心跳初始化

		//7. 最后一步：启动系统（启动所有模块以及事件处理钩子
		fasSystem.startSystem();
	}
}
