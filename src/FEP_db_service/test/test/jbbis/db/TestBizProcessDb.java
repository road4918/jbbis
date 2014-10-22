package test.jbbis.db;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.bizprocess.MasterDbService;

public class TestBizProcessDb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-db-batch.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		MasterDbService msd = (MasterDbService)context.getBean("master.dbservice");
		int seq = msd.getRtuCommandSeq("94300324");
		System.out.println("94300324,seq="+seq);
	}

}
