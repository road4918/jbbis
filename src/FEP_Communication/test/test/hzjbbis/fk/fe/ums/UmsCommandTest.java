package test.hzjbbis.fk.fe.ums;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hzjbbis.fk.fe.ums.protocol.UmsCommands;

public class UmsCommandTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-monitor.xml",
				"classpath*:applicationContext-db-batch.xml",
				"classpath*:applicationContext-ums.xml",
				"classpath*:applicationContext-fec.xml"};
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		UmsCommands umsCmd = (UmsCommands)context.getBean("ums.protocol.3401");
		umsCmd.sendMessage(null, "13867468483", "��վ��ַ��ͨѶʱ����Ψһʶ��ͨѶ����վ�˶�����Ӧ�÷����������̷���ģ�顢ǰ�û��ȣ���", "0", "3401",null,"955983401");
		if( null == umsCmd )
			return;
//		String reqStr = umsCmd.sendRtuMessage("13812345678", "6899053806C11668811000010000000000000030805520211205047616");
//		System.out.println("req="+reqStr);
	}

}
