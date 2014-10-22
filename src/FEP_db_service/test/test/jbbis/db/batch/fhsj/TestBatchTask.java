package test.jbbis.db.batch.fhsj;

import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.batch.dao.IBatchDao;

public class TestBatchTask {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-db-batch.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		IBatchDao dao = (IBatchDao)context.getBean("bp.batchDao.insertTaskDlsj03");
		int maxSize = 3000;
		for(int i=0; i<maxSize; i++){
			TaskDLSJ dlsj = new TaskDLSJ();
			dlsj.setBQBJ(i % 2);
			dlsj.setCT(i%9999);
			dlsj.setFXYGZ("1.1");
			dlsj.setFXYGZ1("1.1");
			dlsj.setFXYGZ2("1.2");
			dlsj.setFXYGZ3("1.3");
			dlsj.setFXYGZ4("1.4");
//			dlsj.setFXZDXL("fxzdxl");
			dlsj.setPT(i%9999);
			dlsj.setSJID(String.valueOf(i));
			dlsj.setSJSJ(new Date(System.currentTimeMillis()));
			dao.add(dlsj);
		}
		dao.batchUpdate();
	}

}
