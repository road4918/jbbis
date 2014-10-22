/**
 * 业务处理器系统对象。
 */
package com.hzjbbis.fk.bp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.DbMonitor;
import com.hzjbbis.db.bizprocess.MasterDbService;
import com.hzjbbis.db.managertu.ManageRtu;
import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.bp.feclient.FEClientManage;
import com.hzjbbis.fk.bp.processor.BPLatterProcessor;
import com.hzjbbis.fk.clientmod.ClientModule;

/**
 * @author bhw
 */
public class BizProcessor {
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-db-batch.xml",
				"classpath*:applicationContext-monitor.xml",
				"classpath*:applicationContext-bp.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		
		ManageRtu manageRtu = (ManageRtu)context.getBean("manageRtu");
		if( DbMonitor.getMasterMonitor().isAvailable() ){
			manageRtu.loadBizRtu();
		}
		
		MasterDbService master = (MasterDbService)context.getBean("master.dbservice");
		BPLatterProcessor.getInstance().setMasterDbService(master);
		BPLatterProcessor.getInstance().start();
		
		FasSystem fas = (FasSystem)context.getBean("fasSystem");
		fas.startSystem();
		
		ClientModule client = (ClientModule)context.getBean("bp.com.client");
		FEClientManage feClientManage = (FEClientManage)context.getBean("bp.feClientManage");
		feClientManage.setClient(client);
	}
}
