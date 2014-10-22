/**
 * ͨ��ǰ�û�������
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
		//��ʼ��class path
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
		//1. ����FasSystem����������ģ���������ֹͣ��
		FasSystem fasSystem = (FasSystem)context.getBean("fasSystem");
		fasSystem.setApplicationContext(context);
		
		//2. ����GateClientManage��ȷ����������ͨ��client���󱻼��ص�ChannelManage��
		GateClientManage gateClients = GateClientManage.getInstance();
		
		ApplicationPropertiesConfig config = ApplicationPropertiesConfig.getInstance();
		config.parseConfig();
		gateClients.setGprsGateClients(config.getGprsClientModules());
		gateClients.setUmsClients(config.getUmsClientModules());

		//������bpServer�Լ�MonitorServerģ��
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
		
		//3. ���ݿ��ʼ��:Initialize ����ն˳�ʼ�����ء�
/*		ManageRtu initRtu = (ManageRtu)context.getBean("app.init.rtu");
		if( mastDbMonitor.isAvailable() )
			initRtu.loadComRtu();
*/
		//4. �������ݿ�ӿ�ģ��: ��������ӿڡ�ҵ����DB�ӿ�
//		AsyncService asyncService = (AsyncService)context.getBean("asyncService");
//		fasSystem.addUnMonitoredModules(asyncService);

		//5. ����UMS�˳̹������ͨ�������˳̹�������ʹ��70��UMS client��ֻ��Ҫ5���̼߳��ɡ�
		FiberManage fiberManage = FiberManage.getInstance();
		fiberManage.setFibers(new ArrayList<IFiber>());
		for(IFiber fiber: config.getUmsClientModules() ){
			fiberManage.schedule(fiber);
		}
		fasSystem.addUnMonitoredModules(fiberManage);
		
		//6. ������ʼ��

		//7. ���һ��������ϵͳ����������ģ���Լ��¼�������
		fasSystem.startSystem();
	}
}
