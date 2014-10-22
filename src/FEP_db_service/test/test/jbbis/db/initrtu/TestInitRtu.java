package test.jbbis.db.initrtu;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.initrtu.InitRtu;
import com.hzjbbis.fk.model.ComRtu;

public class TestInitRtu {
	private static final Logger log = Logger.getLogger(TestInitRtu.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-db-batch.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		InitRtu initRtu = (InitRtu)context.getBean("initRtu");
		long time1 = System.currentTimeMillis();
		List<ComRtu> comRtuList = initRtu.loadComRtu();
		long time2 = System.currentTimeMillis();
		long timeConsume = time2-time1;
		long speed = comRtuList.size()*1000 / timeConsume ;
		long guestw = 18*10000 / speed ;
		log.info("加载终端通信参数，time take="+timeConsume+" ms, total count="+comRtuList.size()+",speed="+speed+"/sec");
		log.info("18万终端预计加载时间="+guestw+" seconds");
	}

}
