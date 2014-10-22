/**
 * ��װ����GPRS���ؿͻ���ͨ��ģ�顣
 * �������б�����Ҫ���д�����澯�Զ�Ӧ�𡢷���ʧ��ת����ͨ�����ն�ͨ����������ͳ�ơ�д������־�ȣ�
 * ��Ҫ���յ�GPRS�����¼����뵽���ȼ����н��д���
 * ��װ���ж��ſͻ���ͨ��ģ�顣
 * 
 * ���Ҫ�㣺�������б��ģ����������ݽ����̴߳������ã����������¼��������ʱ������Ҫ���������¼������̡߳�
 */
package com.hzjbbis.fk.fe;

import java.util.ArrayList;
import java.util.List;

import com.hzjbbis.fk.clientmod.ClientModule;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.abstra.BaseModule;
import com.hzjbbis.fk.fe.ums.UmsModule;

/**
 * @author bhw
 * 
 */
public class GateClientManage extends BaseModule{
	//���������ԣ�ͨ��SPRING���á�
	private List<ClientModule> gprsGateClients = new ArrayList<ClientModule>();
	//����ҪSMSͨ������
	private List<UmsModule> umsClients = new ArrayList<UmsModule>();
	
	private static final GateClientManage instance = new GateClientManage();
	
	public static final GateClientManage getInstance(){
		return instance;
	}
	
	private GateClientManage(){}
	
	public void setGprsGateClients(List<ClientModule>gprsGates){
		gprsGateClients = gprsGates;
		for( ClientModule gate: gprsGateClients ){
			gate.init();
			ChannelManage.getInstance().addGprsClient(gate);
		}
	}

	public final void setUmsClients(List<UmsModule> clients) {
		this.umsClients = clients;
		for( UmsModule ums: umsClients ){
			ChannelManage.getInstance().addUmsClient(ums);
		}
	}

	@Override
	public boolean start() {
		return false;
	}

	@Override
	public void stop() {
	}

	public String getModuleType() {
		return IModule.MODULE_TYPE_CONTAINER;
	}

	public final List<ClientModule> getGprsGateClients() {
		return gprsGateClients;
	}

	public final List<UmsModule> getUmsClients() {
		return umsClients;
	}
	
}
