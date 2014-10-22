package test.hzjbbis.fk.fe.wsclient;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.ws.logic.ModuleSimpleProfile;
import com.hzjbbis.ws.logic.WsFEManage;
import com.hzjbbis.ws.logic.WsHeartbeatQuery;
import com.hzjbbis.ws.logic.WsProfile;

public class WSClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        ClassPathXmlApplicationContext context 
        	= new ClassPathXmlApplicationContext(new String[] {"test/hzjbbis/fk/fe/wsclient/client-beans.xml"});

        long t1 = System.currentTimeMillis();
        
        WsHeartbeatQuery heartQuery = (WsHeartbeatQuery)context.getBean("heartQuery");
        int sum = heartQuery.totalRtuWithHeartByA1((byte)0x92);
        long t3 = System.currentTimeMillis();
        System.out.println("heartbeat rtus = "+ sum);
        System.out.println("times taken: "+ (t3-t1));
        int hCount = heartQuery.heartCount(0x92010001);
        System.out.println("heartbeat count="+ hCount );

        WsProfile client = (WsProfile)context.getBean("profile");
        String response = client.allProfile();
        System.out.println("profile: " + response);
        long t2 = System.currentTimeMillis();
        System.out.println("times taken: "+(t2-t3));
        ModuleSimpleProfile[] list = client.getAllModuleProfile();
    	System.out.println("模块profile:");
        for( ModuleSimpleProfile mp : list ){
        	System.out.println(mp);
        }
        
        //开始增加client
        WsFEManage manage = (WsFEManage)context.getBean("feManage");
        manage.addGprsGateChannel("127.0.0.1", 2007, "ZhouShan");
        list = client.getAllModuleProfile();
    	System.out.println("模块profile===========================");
        for( ModuleSimpleProfile mp : list ){
        	System.out.print(mp);
        }
        System.out.println();
        System.out.println("终端的心跳状况：");
        int rtua = (int)Long.parseLong("91010486",16);
        String result = heartQuery.queryHeartbeatInfo(rtua);
        System.out.println(result);
        System.exit(0);
	}

}
