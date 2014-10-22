package test.hzjbbis.monitor;

import com.hzjbbis.fk.monitor.client.MockMonitorReplyListener;
import com.hzjbbis.fk.monitor.client.MonitorClient;

public class MockMonitorClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MonitorClient client = new MonitorClient("127.0.0.1",10006);
		client.setReplyListener(new MockMonitorReplyListener());
		client.setBufLength(32768);
		client.connect();
		while( !client.isConnected() ){
			Thread.yield();
			try{
				Thread.sleep(100);
			}catch(Exception e){
			}
		}
		client.cmdGetFile("log\\Adb.zip");
		if( !client.isConnected() )
			return;
		int cnt = 10;
		while(cnt-->0){
			try{
				Thread.sleep(1000*60);
			}catch(Exception e){
				
			}
		}
	}

}
