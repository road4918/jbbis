package test.fk.gate;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.fk.FasSystem;

public class GateTest {

	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-monitor.xml",
				"classpath*:applicationContext-gate.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		FasSystem fasSystem = (FasSystem)context.getBean("fasSystem");
		fasSystem.startSystem();
	}

}
