/**
 * �ն��������ػ��档
 */
package com.hzjbbis.fk.fe.filecache;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 * ��ʽ: rtua(4)+ upGprsFlowmeter + upSmsCount + downGprsFlowmeter + downSmsCount + upGprsCount + downGprsCount
 * 		lastGprsTime(8) + lastSmsTime(8) + taskCount(2) + heartbeatCount(2)
 * 
 * line1 4*7=28 bytes; line2 8*2+ 2 + 2 = 20;   total 48 bytes
 */
public class RtuCommFlowCache {
	private static final Logger log = Logger.getLogger(RtuCommFlowCache.class);
	private static final int STOPPED = 0;
	private static final int RUNNING = 1;
	private static final int STOPPING = 2;
	private static RtuCommFlowCache instance;
	private static String filePath;
	private static final int ONE_RTU_CACHE_LEN = 48;	//�ɻ�����Ϣ����ÿ���ն˵Ļ����ֽ�����
	
	static {
		//����Ƿ����dataĿ¼
		try{
			File file = new File("data");
			file.mkdirs();
			filePath = file.getAbsolutePath() + File.separatorChar + "rtu-flow.data";
			instance = new RtuCommFlowCache();
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}

	//������
	private int batchSize = 1000;
	//�ȴ�������ն�
	private Map<Integer,ComRtu> rtuMap = new HashMap<Integer,ComRtu>(1024);
	private int _state = STOPPED;
	//�ڴ�ӳ��
	private MappedByteBuffer buffer = null;
	
	private RtuCommFlowCache(){
		new RtuCommFlowCacheThread();
	}

	public static final RtuCommFlowCache getInstance(){
		return instance;
	}
	
	/**
	 * ���ն�ͨ�Ų�����ʼ��֮�󣬽���������ʼ����
	 */
	public void initOnStartup(){
		File f = new File(filePath);
		if( !f.exists() || f.length()==0 )
			return;
		synchronized(instance){
			try{
				RandomAccessFile raf = new RandomAccessFile(f,"rw");
				buffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0	, f.length() );
				raf.close();
			}catch(Exception e){
				log.error("heartbeat file exception:"+e.getLocalizedMessage(),e);
			}
		}
		int count = buffer.capacity() / ONE_RTU_CACHE_LEN;
		
		//��Ҫ���rtuObj�������ݵ�ʵ�ʳ�����ONE_RTU_CACHE_LEN���бȽϣ��ṩ�����޸ı������ơ�
		boolean checkDataLength = false;
		int pos = 0, rtua=-1;
		int failedCount = 0, successCount = 0;			
		//failedCount-successCount >100,��Ȼ�ļ���ʽ����ȷ���������޸����ն˱���Ĳ�����ʽ��
		for(int i=0; i<count; i++){
			buffer.position(pos);
			rtua = buffer.getInt();		//rtua(4 bytes)
			ComRtu rtuObj = RtuManage.getInstance().getComRtuInCache(rtua);
			if( null != rtuObj ){
				rtuObj.setFlowSavePosition(pos);
				loadRtuFlowData(rtuObj);
				successCount ++;
			}
			else
				failedCount ++;
			if( failedCount - successCount > 100 ){
				log.fatal("loading rtu flows, but failedCount - successCount > 100. RTU save format error.");
				System.exit(-1);
			}
			if( ! checkDataLength ){
				int dataLen = buffer.position() - pos;
				if( dataLen != ONE_RTU_CACHE_LEN ){
					log.fatal("�ն˶����ȡ���ݳ�����ÿ�ն˻��泤�Ȳ�һ��. ONE_RTU_CACHE_LEN��"+ONE_RTU_CACHE_LEN+", readLen="+dataLen);
					System.exit(-1);
				}
			}
			pos += ONE_RTU_CACHE_LEN;
		}
	}
	
	private void loadRtuFlowData(ComRtu rtuObj){
		rtuObj.setUpGprsFlowmeter(buffer.getInt());	//upGprsFlowmeter
		rtuObj.setUpSmsCount(buffer.getInt());		//upSmsCount
		rtuObj.setDownGprsFlowmeter(buffer.getInt());//downGprsFlowmeter
		rtuObj.setDownSmsCount(buffer.getInt());	//downSmsCount
		rtuObj.setUpGprsCount(buffer.getInt());		//upGprsCount
		rtuObj.setDownGprsCount(buffer.getInt());	//downGprsCount
		//lastGprsTime(8) + lastSmsTime(8) + taskCount(2) + heartbeatCount(2)
		rtuObj.setLastGprsTime(buffer.getLong());
		rtuObj.setLastSmsTime(buffer.getLong());
		rtuObj.setTaskCount(buffer.getShort());
		rtuObj.setHeartbeatCount(buffer.getShort());
	}
	
	private void storeRtuFlowData(ComRtu rtu){
		//upGprsFlowmeter + upSmsCount + downGprsFlowmeter + downSmsCount
		buffer.putInt(rtu.getUpGprsFlowmeter());
		buffer.putInt(rtu.getUpSmsCount());
		buffer.putInt(rtu.getDownGprsFlowmeter());
		buffer.putInt(rtu.getDownSmsCount());
		//upGprsCount + downGprsCount
		buffer.putInt(rtu.getUpGprsCount());
		buffer.putInt(rtu.getDownGprsCount());
		
		//lastGprsTime(8) + lastSmsTime(8) + taskCount(2) + heartbeatCount(2)
		buffer.putLong(rtu.getLastGprsTime());
		buffer.putLong(rtu.getLastSmsTime());
		buffer.putShort((short)rtu.getTaskCount());
		buffer.putShort((short)rtu.getHeartbeatCount());
	}
	
	/**
	 * ���ն˵������������ʱ����ñ�������
	 * �����仯�Ĵ����������1��GateMessageEventHandler; ��2��SmsMessageEventhandler
	 * @param rtu
	 */
	public void addRtu(ComRtu rtu){
		synchronized(rtuMap){
			rtuMap.put(rtu.getRtua(), rtu);
			if( rtuMap.size()>= batchSize )
				rtuMap.notifyAll();
		}
	}
	
	//��ϵͳ�˳�����Ҫ���滺�档
	public void dispose(){
		if( _state != RUNNING )
			return;
		_state = STOPPING;
		if( rtuMap.size()>0 ){
			rtuMap.notifyAll();
			int cnt = 20;
			//�ȴ�������ϡ�
			while( cnt-->0 && _state != STOPPED ){
				try{
					Thread.sleep(100);
				}catch(Exception e){}
			}
		}
		log.info("RtuCommFlowCache disposed, state="+_state);
	}
	
	/**
	 * ���ն˲������������ļ�����Ҫ���ô˺���������չ�����ļ���
	 * @param rtus
	 */
	private void addNewRtu(final Collection<ComRtu> rtus){
		if( null == rtus || rtus.size() ==0 )
			return;
		//�����ļ�
		int count = rtus.size();
		int expandLength = count * ONE_RTU_CACHE_LEN;
		
		File f = new File(filePath);
		synchronized(instance){
			try{
				RandomAccessFile raf = new RandomAccessFile(f,"rw");
				int pos0 = (int) raf.length();
				raf.setLength( pos0 + expandLength );
				for(ComRtu rtu: rtus ){
					raf.seek(pos0);
					rtu.setFlowSavePosition(pos0);
					raf.writeInt(rtu.getRtua());
					pos0 += ONE_RTU_CACHE_LEN;
				}
				buffer = null;
				buffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0	, raf.length() );
				raf.close();
			}catch(Exception e){
				log.warn("addNewRtu exception:" + e.getLocalizedMessage() , e);
			}
		}
	}
	
	private void doSave2Cache(){
		ArrayList<ComRtu> list = null;
		//1. �ѵȴ�������ն�ת�Ƶ���ʱ�б��С�
		if( rtuMap.size() == 0 )
			return;
		synchronized(rtuMap){
			list = new ArrayList<ComRtu>(rtuMap.values());
			rtuMap.clear();
		}
		//2. ���б����ҳ����в��ڻ����ļ��е��նˣ��Ա����󻺴��ļ����֮��
		ArrayList<ComRtu> newRtuList = new ArrayList<ComRtu>();
		Iterator<ComRtu> iter = list.iterator();
		while(iter.hasNext() ){
			ComRtu rtu = iter.next();
			if( rtu.getFlowSavePosition()<0 )
				newRtuList.add(rtu);
		}
		addNewRtu(newRtuList);
		newRtuList = null;
		
		//3. ��ŵ������ļ��С�
		synchronized(instance){
			iter = list.iterator();
			while(iter.hasNext()){
				ComRtu rtu = iter.next();
				buffer.position(rtu.getFlowSavePosition());
				int rtua = buffer.getInt();
				if( rtua != rtu.getRtua() ){
					log.warn("�ն�RTUA��λ��һ�£�"+HexDump.toHex(rtua));
					continue;
				}
				storeRtuFlowData(rtu);
			}
			buffer.force();
		}
		list = null;
	}
	
	class RtuCommFlowCacheThread extends Thread{
		public RtuCommFlowCacheThread(){
			super("rtuCommFlowCacheThread");
			setDaemon(true);
			start();
		}
		
		public void run(){
			_state = RUNNING;
			while(true){
				try{
					synchronized(rtuMap){
						rtuMap.wait( 1000*2 );
						if( rtuMap.size() == 0 ){
							continue;
						}
					}
					long t1 = System.currentTimeMillis();
					doSave2Cache();
					if( log.isInfoEnabled() ){
						long t2 = System.currentTimeMillis();
						log.info("save rtu params takes "+(t2-t1)+" milliseconds");
					}
					if( _state == STOPPING )
						break;
				}catch(Exception e){
					log.warn(e.getLocalizedMessage(),e);
				}
			}
			_state = STOPPED;
		}
	}
}
