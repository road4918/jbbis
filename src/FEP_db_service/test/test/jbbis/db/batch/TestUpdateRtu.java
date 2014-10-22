package test.jbbis.db.batch;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.db.batch.AsyncService;

public class TestUpdateRtu {

	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-db-batch.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		AsyncService service = (AsyncService)context.getBean("asyncService");
		service.start();
		int maxSize = 3000;
		for(int i=0; i<maxSize; i++){
			ComRtu rtu = new ComRtu();
			rtu.setActiveGprs("192.168.0.2:"+i);
			rtu.setActiveUms("95598"+i);
			rtu.setB1CommAddress(rtu.getActiveUms());
			rtu.setB1CommType("01");
			rtu.setCommAddress(rtu.getActiveGprs());
			rtu.setCommType("02");
			rtu.setCurGprsFlowmeter(1000+i);
			rtu.setCurSmsCounter(i+1);
			rtu.setDeptCode("0312");
			rtu.setLastIoTime(System.currentTimeMillis());
			rtu.setLogicAddress("92010001");
			rtu.setManufacturer("18");
			rtu.setRtua(92010001+i);
			rtu.setSimNum(String.valueOf( 13812300001L+i ) );
			rtu.setRtuId("zdjh0001");
			
			service.addRtu(rtu);
		}
	}

}
