package com.hzjbbis.fk.bp.test;

import com.hzjbbis.fk.message.zj.MessageLoader4Zj;
import com.hzjbbis.fk.message.zj.MessageZj;


public class inittest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageLoader4Zj msgLoader=new MessageLoader4Zj();
		MessageZj msgZj=msgLoader.loadMessage("6894308809810868801500016899999999999968810867F3444444444444E1161C16");
		System.out.print("msgZj"+msgZj);
		/*String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-dbs.xml",
				"classpath*:applicationContext-bp.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		FasSystem fas = (FasSystem)context.getBean("fasSystem");
		fas.initialize();
		fas.startSystem();*/
		//DbService dbService = (DbService)context.getBean("dbService");
		//dbService.initBizinessProcessor();
		//if( null == fas ||dbService==null)
			return;
	}


}
