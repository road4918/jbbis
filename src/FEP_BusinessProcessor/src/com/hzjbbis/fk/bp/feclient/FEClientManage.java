/**
 * ��װ����GPRS���ؿͻ���ͨ��ģ�顣
 * �������б�����Ҫ���д�����澯�Զ�Ӧ�𡢷���ʧ��ת����ͨ�����ն�ͨ����������ͳ�ơ�д������־�ȣ�
 * ��Ҫ���յ�GPRS�����¼����뵽���ȼ����н��д���
 * ��װ���ж��ſͻ���ͨ��ģ�顣
 * 
 * ���Ҫ�㣺�������б��ģ����������ݽ����̴߳������ã����������¼��������ʱ������Ҫ���������¼������̡߳�
 */
package com.hzjbbis.fk.bp.feclient;

import com.hzjbbis.fk.clientmod.ClientModule;

/**
 * @author bhw
 * 
 */
public class FEClientManage {
	//���������ԣ�ͨ��SPRING���á�
	ClientModule client;

	public void setClient(ClientModule feClient){
		client = feClient;
		FEChannelManage.addClient(feClient);
	}
}
