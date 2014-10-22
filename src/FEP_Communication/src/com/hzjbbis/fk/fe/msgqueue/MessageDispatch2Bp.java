/**
 * 上行报文（GPRS、UMS等）发送到业务处理器BP。
 * 按照地市码区分，分配client。
 * 关系维护：
 * 		bpClient对象找到所有地市码（允许发送报文）
 * 		按照消息的地市码，找到bpClient；找不到则把地市码分配到bpClient
 */
package com.hzjbbis.fk.fe.msgqueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.hzjbbis.fk.common.spi.IProfile;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.fe.msgqueue.BpBalanceFactor.DistrictFactor;
import com.hzjbbis.fk.tracelog.TraceLog;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 *
 */
public class MessageDispatch2Bp implements IProfile{
	private static final TraceLog tracer = TraceLog.getTracer();
	private static final MessageDispatch2Bp instance = new MessageDispatch2Bp();
	private Map<Byte,BpClient> a12ClientMap = new HashMap<Byte,BpClient>();
	private ArrayList<BpClient> clients = new ArrayList<BpClient>();

	private MessageDispatch2Bp(){}
	
	public static final MessageDispatch2Bp getInstance(){
		return instance;
	}
	
	private BpClient findByChannel(IServerSideChannel bpChannel){
		synchronized(clients){
			for(BpClient c: clients){
				if( c.channel == bpChannel )
					return c;
			}
		}
		return null;
	}
	
	private boolean removeByChannel(IServerSideChannel bpChannel){
		synchronized(clients){
			for(int i=0; i<clients.size(); i++ ){
				if( clients.get(i).channel == bpChannel ){
					clients.remove(i);
					return true;
				}
			}
		}
		return false;
	}

	public void onBpClientConnected(IServerSideChannel bpChannel){
		tracer.trace("client connected:"+bpChannel);
		if( null != findByChannel(bpChannel) )
			return ;
		BpClient client = new BpClient();
		client.channel = bpChannel;
		synchronized(clients){
			clients.add(client);
			divideDistrict();
		}
	}
	
	public void onBpClientClosed(IServerSideChannel bpChannel){
		synchronized(clients){
			if( removeByChannel(bpChannel) ){
				tracer.trace("client closed:"+bpChannel);
				divideDistrict();
			}
		}
	}
	
	/**
	 * 按照地市的终端数量分配到BP.
	 * 只能在onBpClientConnected、onBpClientClosed synchronized(clients)内部调用。
	 */
	private void divideDistrict(){
		synchronized(a12ClientMap){
			//重新分配。先初始化
			for( BpClient client : clients ){
				client.a1Array.clear();
				client.factor = 0;
			}
			//把所有预先加载的终端按照地市数量（权重）进行分配
			BpClient minClient = null;
			for( DistrictFactor df: BpBalanceFactor.getInstance().getAllDistricts() ){
				minClient = getMinFactorClient();
				if( null != minClient ){
					minClient.a1Array.add(df.districtCode);
					minClient.factor += df.rtuCount;
				}
			}
			//更新map
			for( BpClient client : clients ){
				for(int i=0; i<client.a1Array.size(); i++)
					a12ClientMap.put(client.a1Array.get(i),client);
			}
		}
		if( tracer.isEnabled() && !clients.isEmpty() )
			tracer.trace("BP divided by A1. profile="+profile());
	}
	
	private BpClient getMinFactorClient(){
		BpClient minClient = null;
		for( BpClient c : clients ){
			if( null == minClient )
				minClient = c;
			else if( c.factor < minClient.factor )
				minClient = c;
		}
		return minClient;
	}
	
	/**
	 * According to city code, get business processor client channel.
	 * @param a1
	 * @return
	 */
	public IServerSideChannel getBpChannel(byte a1){
		BpClient client = null;
		synchronized(a12ClientMap){
			client = a12ClientMap.get(a1);
			if( null == client ){
				//a1地市码没有分配给bpclient 或者某个bpChannel关闭导致需要重新分配。
				synchronized(clients){
					BpBalanceFactor.getInstance().travelRtus();
					divideDistrict();
					client = a12ClientMap.get(a1);
//					client = getMinFactorClient();
//					if( null != client ){
//						client.a1Array.add(a1);
//						a12ClientMap.put(a1, client);
//					}
				}
			}
		}
		return null != client ?  client.channel : null;
	}
	
	public IServerSideChannel getIdleChannel(){
		synchronized(clients){
			for(BpClient c: clients){
				if(c.channel.sendQueueSize() == 0 && c.channel.getRequestNum() != 0 )
					return c.channel;
			}
		}
		return null;
	}
	
	private class BpClient{
		IServerSideChannel channel = null;
		Vector<Byte> a1Array = new Vector<Byte>();	//zhejiang message head's a1 byte
		int factor = 0;
	}

	public String profile() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\r\n    <bp-citys>");
		synchronized(clients){
			for(BpClient c: clients){
				sb.append("\r\n        <bp addr=\"").append(c.channel.getPeerAddr()).append("\" factor=\"");
				sb.append(c.factor).append("\">");
				boolean first = true;
				for(Byte B: c.a1Array ){
					if( first ){
						sb.append(HexDump.toHex(B.byteValue()));
						first = false;
					}
					else
						sb.append(",").append(HexDump.toHex(B.byteValue()));
				}
				sb.append("</bp>");
			}
		}
		sb.append("\r\n    </bp-citys>");
		return sb.toString();
	}
}
