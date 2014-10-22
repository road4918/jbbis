package test.hzjbbis.fk.simulator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.fk.sockclient.async.AsyncSocketPool;
import com.hzjbbis.fk.sockclient.async.eventhandler.AsyncSocketEventHandler;
import com.hzjbbis.fk.sockclient.async.simulator.SimulatorManager;

public class SimulatorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket-client.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		//ģ�����¼�������
		AsyncSocketEventHandler simulatorHandler = (AsyncSocketEventHandler)context.getBean("eventhook.simulator");
		simulatorHandler.start();
		AsyncSocketPool sockPool = (AsyncSocketPool)context.getBean("asyncpool.simulator");
		sockPool.start();
		SimulatorManager.startHeart();	SimulatorManager.startTask();
	}

}
