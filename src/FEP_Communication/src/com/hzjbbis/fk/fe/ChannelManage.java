/**
 * �ն����ӵ����ص��ⲿͨ�Ŷ˿ڣ�ͨ��ǰ�û����ӵ������ڲ��˿ڡ�
 * �ն��ʲ����ͨ�Ų���Ϊ���ⲿIP���ⲿ�˿�
 * ����ͨ��������˵�����ص�Ψһ�Ա�ʶΪ���ڲ�ip���ڲ��˿ڡ�
 * ���ڶ������У�ͨ����Ψһ��ʶΪ��appid
 * 
 */
package com.hzjbbis.fk.fe;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.clientmod.ClientModule;
import com.hzjbbis.fk.common.spi.socket.IChannel;
import com.hzjbbis.fk.common.spi.socket.abstra.BaseClientChannel;
import com.hzjbbis.fk.fe.ums.UmsModule;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 *
 */
public class ChannelManage {
	private static final Logger log = Logger.getLogger(ChannelManage.class);
	//����������
	private int rtuHeartbeatInterval = 15*60;	//15�������������
	private int rtuTransferInterval = 60;		//��������1���Ӽ���ж�
	//�ڲ�����
	private long hbInterval = rtuHeartbeatInterval * 1000;
	private long tfInterval = rtuTransferInterval * 1000;
	
	private static ChannelManage cm = new ChannelManage();
	private ChannelManage(){}
	
	public static ChannelManage getInstance(){
		return cm;
	}
	
	//����rtua�ҵ��ն�rtu���󣻸�����Ҫ������activeGprs,activeUms��ȷ��ͨ��
	private final Map<String,BaseClientChannel> mapGates = new HashMap<String,BaseClientChannel>();
	private final Map<String,BaseClientChannel> mapUmsClients = new HashMap<String,BaseClientChannel>();
	private final Map<String,BaseClientChannel> mapGprsClients = new HashMap<String,BaseClientChannel>();
	public boolean testMode = false;	//����ʱ��rtu����û�м��أ���ôchannelѡ���κ�һ��GPRS�������С�
	
	
	public void setRtuHeartbeatInterval(int interval){
		rtuHeartbeatInterval = interval;
		hbInterval = rtuHeartbeatInterval * 1000; 
	}

	public void setRtuTransferInterval(int rtuTransferInterval) {
		this.rtuTransferInterval = rtuTransferInterval;
		tfInterval = rtuTransferInterval * 1000;
	}

	/**
	 * ��ӣ�GPRS���صĿͻ�������
	 * @param gprsClient
	 */
	public void addGprsClient(ClientModule gprsClient ){
		mapGates.put(gprsClient.getSocket().getPeerAddr(), gprsClient.getSocket());
		mapGprsClients.put(gprsClient.getSocket().getPeerAddr(), gprsClient.getSocket());
	}
	
	//����ҪΪUMS�������ӽ���IClientChannelӳ���ϵ��....
	public void addUmsClient(UmsModule umsClient){
		mapGates.put(umsClient.getPeerAddr(), umsClient);
		mapUmsClients.put(umsClient.getPeerAddr(), umsClient);
	}
	
	/**
	 * ������ͨ�û����ŷ�������0X28������Ҫ���ѡ��һ����Ч�Ķ���ͨ�����С�
	 * @return
	 */
	public BaseClientChannel getActiveUmsChannel(){
		for( BaseClientChannel channel: mapUmsClients.values() )
			if( channel.isActive() )
				return channel;
		return null;
	}
	
	public BaseClientChannel getActiveGprsChannel(){
		for( BaseClientChannel channel: mapGprsClients.values() )
			if( channel.isActive() )
				return channel;
		return null;
	}
	/**
	 * ����վ��������ʱ����Ҫ����RTUAȡRtu����Ȼ������ͨ��������ߣ���ȡactiveChannel����ip:port����appid
	 * ����������ָ��APPID���е������ֱ�ӵ��� ������
	 * @param key: GPRSͨ��Ϊip:port������UMSƽ̨Ϊappid
	 * @return
	 */
	public IChannel getChannel(String key){
		return mapGates.get(key);
	}
	
	/**
	 * �����ʲ����ͨ�ŷ�ʽ��ת��Ϊ����ʹ�õ�ͨ�ŷ�ʽ
	 * 		01:����; 02:GPRS;  03:DTMF;  04:Ethernet;
	 * 		05:����; 06:RS232; 07:CSD;   08:Radio; 	09:CDMA;
	 * @param commType
	 * @return��1 ���ţ� 2 GPRS/CDMA/Ethernet; 0 ������ʽ��ͨ��ǰ�û�����Ҫ֧��
	 */
	private static int communicationType(String commType){
		if( null == commType )
			return -1;
		if( commType.equals("02") || commType.equals("09") || commType.equals("04") )
			return 2;
		else if( commType.equals("01") )
			return 1;
		else
			return 0;
	}
	
	/**
	 * �ն�û��ָ������ͨ������£�����RTUA������ѡ��ͨ��
	 * ���ȼ���GRPSͨ��->������ͨ��->��1����ͨ��->��2����ͨ��
	 * @param rtua
	 * @return
	 */
	public IChannel getChannel(int rtua){
		IChannel channel = getGPRSChannel(rtua);
		if( null != channel )
			return channel;
		channel = getUmsChannel(null,rtua);
		if( null != channel )
			return channel;
		//����Ҳ������ն���ЧUMSͨ��������ѡ��ǰ��ЧUMSͨ��
		//�Ա��ն��л��᷵�طǷ�����Ӧ��
		return getActiveUmsChannel();
	}
	
	/**
	 * �����վ�������У�ֻ��֧��GPRSͨ����
	 * @param rtua
	 * @return
	 */
	public IChannel getGPRSChannel(int rtua){
		//����rtua����RTU����
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		if( null == rtu ){
			log.warn("�ն�����û�м��ء�rtua="+HexDump.toHex(rtua));
			return null;
		}
		
		//1. �����ն���������ʱ�䣬ȡ��ǰ���õ�ͨ����
		//������⣺�ն˲���Ϊ���ţ�����ʵ����GPRSͨ����
		long timeSpan = System.currentTimeMillis() - rtu.getLastGprsTime();
		if( timeSpan< (hbInterval*2) && null != rtu.getActiveGprs()){
			long lastReq = rtu.getLastReqTime();
			//long tspan = Math.max(System.currentTimeMillis() - lastReq, lastReq-rtu.getLastGprsTime() ) ;			
			long tspan = Math.abs(System.currentTimeMillis() - lastReq) ;
			if( tspan> tfInterval && rtu.getLastGprsTime() < lastReq  )
				return null;		//��UMSͨ��
			IChannel channel = getChannel(rtu.getActiveGprs());
			return channel;
		}
		return null;
	}
	
	/**
	 * �ն˿���ָ������ͨ�������û��ָ��������RTUA������ѡ��ͨ��
	 * ���ȼ���������ͨ��->��1����ͨ��->��2����ͨ��
	 * @param rtua
	 * @return
	 */
	public IChannel getUmsChannel(String appid, int rtua ){
		IChannel channel = null;
		if( null != appid && appid.length()>0 ){
			channel = getChannel(appid);
			if( null != channel )
				return channel;
		}
		if( 0 == rtua )
			return channel;
		
		//����rtua����RTU����
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		//1. �߶���ͨ��������ʹ�õ�ǰ�����ͨ��
		String activeUms = rtu.getActiveUms();
		if( null != activeUms && activeUms.length()>2 )
			return getChannel(activeUms);
		
		//2. ����ն���ͨ����GPRS�����ȼ���ӦGPRS����ͨ�����
		int cType = communicationType(rtu.getCommType());
		if( cType == 1 ){
			//955983501����Ӧ�ú��벻ȡ
			activeUms = rtu.getCommAddress().substring(0, 9);
			channel = getChannel(activeUms);
			if( null != channel )
				return channel;
		}
		
		//3. �ӱ�1ͨ��ȡ
		cType = communicationType(rtu.getB1CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB1CommAddress().substring(0, 9);
			channel = getChannel(activeUms);
			if( null != channel )
				return channel;
		}
		
		//4. B1ͨ��ʧ�ܣ����B2ͨ��
		cType = communicationType(rtu.getB2CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB2CommAddress().substring(0, 9);
			channel = getChannel(activeUms);
			if( null != channel )
				return channel;
		}
		
		//��ʧ�ܡ���ô�ն˲����߶���ͨ��
		log.warn("�ն˶���ͨ�����ò���ȷ��RTUA="+rtu.getLogicAddress());
		//����Ҳ������ն���ЧUMSͨ��������ѡ��ǰ��ЧUMSͨ��
		//�Ա��ն��л��᷵�طǷ�����Ӧ��
		return null;
	}
	
	public static final String getUmsAppId(int rtua){
		//����rtua����RTU����
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		//1. ����ն���ͨ����GPRS�����ȼ���ӦGPRS����ͨ�����
		int cType = communicationType(rtu.getCommType());
		if( cType <= 0 ){
			log.error("�ն���ͨ������GPRS/CDMA�����߶��š�RTUA��"+rtu.getLogicAddress());
			return null;
		}
		
		//2. �߶���ͨ��������ʹ�õ�ǰ�����ͨ��
		String activeUms = rtu.getActiveUms();
		if( null != activeUms && activeUms.length()>2 )
			return activeUms;
		
		//3. ��ǰ��Ч����ͨ��û�����ã��������ͨ���Ƕ��ţ���Ҫ����ͨ��ȡ
		if( cType == 1 ){
			//955983501����Ӧ�ú��벻ȡ
			activeUms = rtu.getCommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		
		//4. GPRS�ն�ת����ͨ��������Լ������ն���ͨ��ʧ��������ӱ�1ͨ��ȡ
		cType = communicationType(rtu.getB1CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB1CommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		//6. B1ͨ��ʧ�ܣ����B2ͨ��
		cType = communicationType(rtu.getB2CommType() );
		if( cType == 1 ){
			activeUms = rtu.getB2CommAddress();
			if( null != activeUms && activeUms.length()>2 )
				return activeUms;
		}
		//��ʧ�ܡ���ô�ն˲����߶���ͨ��
		log.warn("�ն˶���ͨ�����ò���ȷ��RTUA="+rtu.getLogicAddress());
		return null;
	}
}
