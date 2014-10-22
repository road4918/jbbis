package test.jbbis.db.initrtu;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.rtu.jdbc.JdbcRtuRefreshDao;
import com.hzjbbis.fk.model.BizRtu;

public class TestRefreshRtuDao {
	private static final Logger log = Logger.getLogger(TestRefreshRtuDao.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-db-batch.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		JdbcRtuRefreshDao dao = (JdbcRtuRefreshDao)context.getBean("dao.refreshRtu");
		BizRtu brtu = dao.getRtu("1280");
		log.debug("bizrtu="+brtu);
	}

}
