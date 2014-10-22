/**
 * ���ն˶����ͨ�Ų��������ļ����档
 * �ն�ͨ�Ų����ı仯Ƶ�ʺ�С���ն˵ĸ����仯Ƶ�ʸ�С�����ն������ı仯��Ƶ��������ն�ͨ�Ų���
 * �������ֿ����棬�Ա����ϵͳ��IO���ܡ�
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
 * ͨ�Ų�����ʽ��rtua(4 bytes) + manufacturer(1 byte) + mobile(8 bytes) + activeGprs(21 bytes) + activeUms(8 bytes) + activeSubAppId(3 bytes)
 * activeGprs ip:port,   255.255.255.255:65535 maxlen = 21
 * total bytes= 45
 * ���津��������activeGprs activeUms activeSubAppId �����仯ʱ����Ҫд�����ļ���
 */
public class RtuParamsCache {
	private static final Logger log = Logger.getLogger(RtuParamsCache.class);
	private static final int STOPPED = 0;
	private static final int RUNNING = 1;
	private static final int STOPPING = 2;
	private static RtuParamsCache instance = null;
	private static String filePath;
	private static final int ONE_RTU_CACHE_LEN = 45;	//�ɻ�����Ϣ����ÿ���ն˵Ļ����ֽ�����
	
	static {
		//����Ƿ����dataĿ¼
		try{
			File file = new File("data");
			file.mkdirs();
			filePath = file.getAbsolutePath() + File.separatorChar + "rtu-params.data";
			instance = new RtuParamsCache();
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
	
	private RtuParamsCache(){
		new RtuParamCacheThread();
	}
	
	public static final RtuParamsCache getInstance(){
		return instance;
	}
	
	public void initOnStartup(boolean create ){
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
		int pos = 0, rtua=-1;
		int failedCount = 0, successCount = 0;			
		//failedCount-successCount >100,��Ȼ�ļ���ʽ����ȷ���������޸����ն˱���Ĳ�����ʽ��
		byte[] activeGprsBytes = new byte[21];
		byte[] subAppIdBytes = new byte[3];
		for(int i=0; i<count; i++){
			buffer.position(pos);
			rtua = buffer.getInt();		//rtua(4 bytes)
			ComRtu rtuObj = null;
			if( create ){
				rtuObj = new ComRtu();
				rtuObj.setRtua(rtua);
				rtuObj.setLogicAddress(HexDump.toHex(rtua));
				RtuManage.getInstance().putComRtuToCache(rtuObj);
			}
			else
				rtuObj = RtuManage.getInstance().getComRtuInCache(rtua);
			if( null != rtuObj ){
				rtuObj.setRtuSavePosition(pos);

				byte b = buffer.get();								//manufacturer(1 byte)	
				if( 0 ==  b )
					rtuObj.setManufacturer( null );
				else
					rtuObj.setManufacturer(HexDump.toHex(b) );
				
				long mobile = buffer.getLong();						//mobile(8 bytes)
				if( 0 != mobile )
					rtuObj.setSimNum(String.valueOf(mobile));
				else
					rtuObj.setSimNum(null);
				
				buffer.get(activeGprsBytes);						//activeGprs(21 bytes)
				int gprsLen = activeGprsBytes.length;
				for(int j=0; j<activeGprsBytes.length; j++){
					if( 0 == activeGprsBytes[j] ){
						gprsLen = j;
						break;
					}
				}
				if( gprsLen>0 )
					rtuObj.setActiveGprs(new String(activeGprsBytes,0,gprsLen));
				else
					rtuObj.setActiveGprs(null);
				
				mobile = buffer.getLong();							//activeUms(8 bytes)
				if( 0 != mobile )
					rtuObj.setActiveUms(String.valueOf(mobile));
				else
					rtuObj.setActiveUms(null);
				
				buffer.get(subAppIdBytes);								//activeSubAppId(3 bytes)
				int subAppIdLen = subAppIdBytes.length;
				for(int j=0; j<subAppIdBytes.length; j++ ){
					if( 0 == subAppIdBytes[j] ){
						subAppIdLen = j;
						break;
					}
				}
				if( subAppIdLen>0 )
					rtuObj.setActiveSubAppId(new String(subAppIdBytes,0,subAppIdLen));
				else
					rtuObj.setActiveSubAppId("");
				successCount++;
			}
			else{
				failedCount++;
			}
			double rate = failedCount* 1.0 / (failedCount+successCount) ;
			if( rate > 0.99 ){
				log.warn("loading rtu params, but failedCount/total > 99%. RTU save format may be error.");
//				System.exit(-1);
			}
			pos += ONE_RTU_CACHE_LEN;
		}
	}
	
	/**
	 * ���ն˵�ͨ�Ų����������ʱ����ñ�������
	 * ͨ�Ų�������1��mobile���������п��ܵ��±��������2��activeGprs ��GPRS���п��ܵ��±����;
	 * 		   ��3��activeUms��UMS���п��ܵ��±������4��activeSubAppId��ͬ3��
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
			log.info("RtuParamsCache disposed, state="+_state);
		}
	}
	
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
					rtu.setRtuSavePosition(pos0);
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
			if( rtu.getRtuSavePosition()<0 )
				newRtuList.add(rtu);
		}
		addNewRtu(newRtuList);
		newRtuList = null;
		
		//3. ��ŵ������ļ��С�
		synchronized(instance){
			iter = list.iterator();
			while(iter.hasNext()){
				ComRtu rtu = iter.next();
				buffer.position(rtu.getRtuSavePosition());
				int rtua = buffer.getInt();
				if( rtua != rtu.getRtua() ){
					log.warn("�ն�RTUA��λ��һ�£�"+HexDump.toHex(rtua));
					continue;
				}
				
				String str = rtu.getManufacturer();
				if( null == str || str.length() == 0 )
					str = "0";
				byte manuf = 0;
				try{
					manuf = Byte.parseByte(str, 16);
				}catch(Exception e){
					log.warn("manufacturer error:"+str,e);
				}
				buffer.put(manuf);								//manufacturer(1 byte)
				
				str = rtu.getSimNum();							//mobile(8 bytes)
				if( null == str || str.length() == 0 )
					str = "0";
				long longVal = 0;
				try{
					longVal = Long.parseLong(str);
				}catch(Exception e){
					log.warn("simNum error:"+str,e);
				}
				buffer.putLong( longVal );
				
				byte[] activeGprsBytes = new byte[21];
				int endPos = 0;
				str = rtu.getActiveGprs();
				if( null != str && str.length()>0 ){
					byte[] srcObj = str.getBytes(); 
					endPos = srcObj.length;
					if( endPos > 21 ){
						log.error("activeGprs>21 bytes. which is:"+str);
						endPos = 21;
					}
					for(int j=0; j<endPos; j++ )
						activeGprsBytes[j] = srcObj[j];
				}
				for(int j=endPos; j<21; j++ )
					activeGprsBytes[j] = 0;
				buffer.put(activeGprsBytes);
				
				longVal = 0;
				str = rtu.getActiveUms();
				if( null == str || str.length() == 0 )
					str = "0";
				try{
					longVal = Long.parseLong(str);
				}catch(Exception e){
					log.warn("activeUms error:"+str,e);
				}
				buffer.putLong( longVal );							//activeUms(8 bytes)
				
				byte[] subAppIdBytes = new byte[3];					//activeSubAppId(3 bytes)
				str = rtu.getActiveSubAppId();
				endPos = 0;
				if( null != str && str.length()>0 ){
					byte[] srcObj = str.getBytes(); 
					endPos = srcObj.length;
					if( endPos > 3 ){
						log.error("activeSubAppId>3 bytes. which is:"+str);
						endPos = 3;
					}
					for(int j=0; j<endPos; j++ )
						subAppIdBytes[j] = srcObj[j];
				}
				for(int j=endPos; j<subAppIdBytes.length; j++ )
					subAppIdBytes[j] = 0;
				buffer.put(subAppIdBytes);
			}
			buffer.force();
		}
		list = null;
	}
	
	class RtuParamCacheThread extends Thread{
		public RtuParamCacheThread(){
			super("RtuParamCacheThread");
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
