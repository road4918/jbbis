package test.jbbis.db.initrtu;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.initrtu.dao.BizRtuDao;
import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuAlertCode;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.TaskDbConfig;
import com.hzjbbis.fk.model.TaskTemplate;

public class TestInitBizRtu {
	private static final Logger log = Logger.getLogger(TestInitBizRtu.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-db-batch.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		BizRtuDao dao = (BizRtuDao)context.getBean("dao.initBizRtu");
		long time1 = System.currentTimeMillis();
		List<BizRtu> comRtuList = dao.loadBizRtu();
		long time2 = System.currentTimeMillis();
		long timeConsume = time2-time1+1;
		long speed = comRtuList.size()*1000 / timeConsume ;
		log.info("加载终端,time take="+timeConsume+" ms, total count="+comRtuList.size()+",speed="+speed+"/sec");
		
		List<MeasuredPoint> mpoints = dao.loadMeasuredPoints();
		long time3 = System.currentTimeMillis();
		timeConsume = time3-time2+1;
		speed = mpoints.size()*1000 / timeConsume ;
		log.info("加载测量点,time take="+timeConsume+" ms, total count="+mpoints.size()+",speed="+speed+"/sec");
		
		List<RtuAlertCode> alertCodes = dao.loadRtuAlertCodes();
		long time4 = System.currentTimeMillis();
		timeConsume = time4-time3+1;
		speed = alertCodes.size()*1000 / timeConsume ;
		log.info("加载告警编码,time take="+timeConsume+" ms, total count="+alertCodes.size()+",speed="+speed+"/sec");
		
		List<RtuTask> rtuTasks = dao.loadRtuTasks();
		long time5 = System.currentTimeMillis();
		timeConsume = time5-time4+1;
		speed = rtuTasks.size()*1000 / timeConsume ;
		log.info("加载终端任务,time take="+timeConsume+" ms, total count="+rtuTasks.size()+",speed="+speed+"/sec");
		
		List<TaskDbConfig> taskDbConfig = dao.loadTaskDbConfig();
		long time6 = System.currentTimeMillis();
		timeConsume = time6-time5+1;
		speed = taskDbConfig.size()*1000 / timeConsume ;
		log.info("加载终端任务数据库配置,time take="+timeConsume+" ms, total count="+rtuTasks.size()+",speed="+speed+"/sec");
		
		List<TaskTemplate> taskTemplates = dao.loadTaskTemplate();
		long time7 = System.currentTimeMillis();
		timeConsume = time7-time6+1;
		speed = taskTemplates.size()*1000 / timeConsume ;
		log.info("加载任务模板,time take="+timeConsume+" ms, total count="+taskTemplates.size()+",speed="+speed+"/sec");
		
		speed = comRtuList.size()*1000 / (System.currentTimeMillis()-time1) ;
		long guestw = 18*10000 / speed ;
		log.info("18万终端预计加载时间="+guestw+" seconds");
	}

}
