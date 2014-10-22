package test.hzjbbis.monitor;

import com.hzjbbis.fk.monitor.MonitorDataItem;
import com.hzjbbis.fk.monitor.OsSystemMonitor;

public class CPUTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final OsSystemMonitor cmonitor = OsSystemMonitor.getInstance();
		cmonitor.setAutoMonitor(true);
		cmonitor.initialize();
		
		MonitorDataItem item = cmonitor.getCurrentData();
		System.out.println("item="+item);
		long count = 0;
		for(long i=0; i<10000000; i++ ){
			count += 1 + count*3;
			if( count> 10000000 ){
				count = 0;
			}
		}
		try{
		Thread.sleep(1000*60*10);
		}catch(Exception e){}
	}
	
	

}
