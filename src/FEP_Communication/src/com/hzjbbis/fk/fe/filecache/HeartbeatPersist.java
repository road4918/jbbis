/**
 * �ն�������Ϣ���浽�����ļ���
 * ÿ��һ���ļ���ÿ���ն�ÿ�����288����������λ���棬��36�ֽ�)��
 * 30���նˣ��ļ�Ϊ12M��С��ѭ�����1���£���ռ�����Ϊ 360M��
 * ��Ÿ�ʽ��
 * ÿ���նˣ�rtua(4�ֽ�)+ 36 �ֽ�
 * ÿ���ļ����ƣ�heartbeat-i.data	i��ʾ�·ݵ�����.ÿ���賿����
 * 
 * �㷨��
 * 1���ն˱���Ķ�λ������ComRtu��heartSavePositionֱ�Ӷ�λ
 * 2����ʼ��ʱ����Ҫ�Ӵ��̼����ļ������¶�λ��Ϣ��
 * 	 ���heartSavePosition �� -1�����ʾ���նˣ���Ҫ�����µ�λ�á�
 * 3��36�ֽ�˳��: byte1 byte2 ... ÿ���ֽ����λ��ʾ�ȷ����������¼�(little ending)��
 * 4���������ĵ�ʱ�䣬���Ƶ�5����ʱ�䣬Ȼ���ڶ�λ���ֽں�λ�������ļ���
 */
package com.hzjbbis.fk.fe.filecache;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;
import com.hzjbbis.fk.utils.HexDump;

/**
 * @author bhw
 *
 */
public class HeartbeatPersist {
	private static final Logger log = Logger.getLogger(HeartbeatPersist.class);
	private static HeartbeatPersist instance = null;
	private static final int STOPPED = 0;
	private static final int RUNNING = 1;
	private static final int STOPPING = 2;
	private static final int ONE_RTU_CACHE_LEN = 40; 
	private static final byte[] EMPTY_HEARTS = new byte[ONE_RTU_CACHE_LEN-4];
	private static String rootPath;
	
	static {
		//����Ƿ����dataĿ¼
		try{
			File file = new File("data");
			file.mkdirs();
			rootPath = file.getAbsolutePath() + File.separatorChar ;
			instance = new HeartbeatPersist();
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	//������
	private int batchSize = 1000;
	//�ڴ�ӳ��
	private String filePath = null;
	private MappedByteBuffer buffer = null;
	private final LinkedList<HeartbeatInfo> heartList = new LinkedList<HeartbeatInfo>();
	
	//ÿ�ն�ʱ����
	private TimerTask runEveryDay = null;
	//ϵͳֹͣʱ����Ҫִ��������
	private int _state = 0;
	
	private HeartbeatPersist(){
		new HeartbeatHandleThread();
		runEveryDay = new TimerTask(){
			@Override
			public void run() {
				initPerDay();
				TimerScheduler.getScheduler().schedulerOnce(runEveryDay, nextDay() );
			}
		};
		TimerScheduler.getScheduler().schedulerOnce(runEveryDay, nextDay() );
		for( int i=0; i<EMPTY_HEARTS.length; i++ ){
			EMPTY_HEARTS[i] = 0;
		}
	}
	
	public static final HeartbeatPersist getInstance(){
		return instance;
	}
	
	private Date nextDay(){
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(System.currentTimeMillis());
		int year,month,date;
		year = cl.get(Calendar.YEAR);
		month = cl.get(Calendar.MONTH);
		date = cl.get(Calendar.DAY_OF_MONTH);
		cl.clear();
		cl.set(year, month, date, 0, 0, 10);
		cl.add(Calendar.DAY_OF_MONTH, 1);
		return cl.getTime();
	}

	public void handleHeartbeat(int rtua){
		HeartbeatInfo hi = new HeartbeatInfo();
		hi.rtua = rtua;
		synchronized(heartList){
			heartList.addLast(hi);
			if( heartList.size()>= batchSize )
				heartList.notifyAll();
		}
	}
	
	public void handleHeartbeat(int rtua, long time){
		HeartbeatInfo hi = new HeartbeatInfo();
		hi.rtua = rtua;
		hi.time = time;
		synchronized(heartList){
			heartList.addLast(hi);
			if( heartList.size()>= batchSize )
				heartList.notifyAll();
		}
	}
	
	/**
	 * must be called after Rtu loaded from db or cache.
	 */
	public void initOnStartup(){
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(System.currentTimeMillis());
		int dayOfMonth = cl.get(Calendar.DAY_OF_MONTH);
		filePath = rootPath + "heartbeat-"+dayOfMonth+".data";
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
		if( null == buffer )
			return;
		int count = buffer.capacity() / ONE_RTU_CACHE_LEN;
		int pos = 0, rtua=-1;
		for(int i=0; i<count; i++){
			rtua = buffer.getInt(pos);
			ComRtu rtuObj = RtuManage.getInstance().getComRtuInCache(rtua);
			if( null != rtuObj )
				rtuObj.setHeartSavePosition(pos);
			pos += ONE_RTU_CACHE_LEN;
		}
	}
	
	public void dispose(){
		if( _state != RUNNING )
			return;
		_state = STOPPING ;
		synchronized(heartList){
			if( heartList.size()>0 ){
				heartList.notifyAll();
				int cnt = 10;
				while( cnt-->0 && _state != STOPPED ){
					try{
						Thread.sleep(100);
					}catch(Exception e){}
				}
			}
		}
	}
	
	/**
	 * ÿ��00:00:10����һ�Ρ�
	 */
	public void initPerDay(){
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(System.currentTimeMillis());
		int dayOfMonth = cl.get(Calendar.DAY_OF_MONTH);
		filePath = rootPath + "heartbeat-"+dayOfMonth+".data";
		File f = new File(filePath);
		if( f.exists() ){
			f.delete();
			return;
		}
		synchronized(instance){
			try{
				RandomAccessFile raf = new RandomAccessFile(f,"rw");
				List<ComRtu> rtus = new ArrayList<ComRtu>(RtuManage.getInstance().getAllComRtu());
				int maxPos = 0;
				for(ComRtu rtu: rtus ){
					if( rtu.getHeartSavePosition()> maxPos )
						maxPos = rtu.getHeartSavePosition();
				}
				raf.setLength(maxPos + ONE_RTU_CACHE_LEN);
				if( null != buffer ){
					buffer.force();
					buffer = null;
				}
				buffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0	, raf.length() );
				raf.close();
				for(ComRtu rtu : rtus ){
					if( rtu.getHeartSavePosition()>=0 ){
						buffer.position(rtu.getHeartSavePosition());
						buffer.putInt(rtu.getRtua());
						buffer.put(EMPTY_HEARTS);
					}
				}
				buffer.force();
			}catch(Exception e){
				log.error("heartbeat file exception:"+e.getLocalizedMessage(),e);
			}
		}
	}
	
	private String heartbeatInfo2String(byte[] heartBits){
		StringBuilder sb = new StringBuilder();
		try{
			//����ʱ����
			int minute = 0, hour = 0, totalMinutes = 0;
			for(int i=0; i<heartBits.length; i++ ){
				byte b = heartBits[i];
				int sum = 0;
				for( int j=7; j>=0; j-- ){
					int result = j!=0 ? ((1 << j) & b) : b & 1;
					if( result != 0 ){
						sum = totalMinutes + 5*(7-j);
						hour = sum / 60;
						minute = sum % 60;
						sb.append(hour).append(":").append(minute).append("; ");
					}
				}
				totalMinutes += 40;			//ÿ���ֽڴ���40����
			}
		}catch(Exception e){
			log.warn(e.getLocalizedMessage(),e);
			sb.append("exception:").append(e.getLocalizedMessage());
		}
		return sb.toString();
	}
	
	public String queryHeartbeatInfo(int rtua){
		ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
		if( null == rtu || rtu.getHeartSavePosition()<0 )
			return "no rtu";
		int pos = rtu.getHeartSavePosition()+4;
		byte[] heartBits = new byte[ONE_RTU_CACHE_LEN-4];
		buffer.position(pos);
		buffer.get(heartBits);
		return heartbeatInfo2String(heartBits);
	}
	
	public String queryHeartbeatInfo(int rtua, int dayOfMonth){
		filePath = rootPath + "heartbeat-"+dayOfMonth+".data";
		File f = new File(filePath);
		if( !f.exists() || f.length() == 0 ){
			return "file not exist:"+filePath;
		}
		String result = "rtu not found in that day";
		try{
			RandomAccessFile raf = new RandomAccessFile(f,"rw");
			MappedByteBuffer buf = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0	, raf.length() );

			int end = buf.capacity()-ONE_RTU_CACHE_LEN;
			for(int pos=0; pos<end; pos += ONE_RTU_CACHE_LEN ){
				if( buf.getInt(pos) == rtua ){
					buf.position(pos+4);
					byte[] heartBits = new byte[ONE_RTU_CACHE_LEN-4];
					buf.get(heartBits);
					result = heartbeatInfo2String(heartBits);
					break;
				}
			}
			
			buf = null;
			raf.close();
		}catch(Exception e){
			result = "heartbeat file exception:"+e.getLocalizedMessage();
			log.error(result,e);
		}
		return result;
	}
	
	/**
	 * ����rtu.getHeartSavePosition()<0 ,��ʾ���ն�û�м�¼���ļ��У���Ҫ�����ļ���
	 * @param hinfos
	 */
	private void addNewRtu2File(final Collection<ComRtu> rtus ){
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
					rtu.setHeartSavePosition(pos0);
					raf.writeInt(rtu.getRtua());
					raf.write(EMPTY_HEARTS);
					pos0 += ONE_RTU_CACHE_LEN;
				}
				buffer = null;
				buffer = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0	, raf.length() );
				raf.close();
			}catch(Exception e){
				log.warn("addMoreRtu2File exception:" + e.getLocalizedMessage() , e);
			}
		}
	}
	
	private void processHeartInfoList(){
		Map<Integer,ComRtu> uniqRtus = new HashMap<Integer,ComRtu>();
		ArrayList<HeartbeatInfo> list = null;
		if( heartList.size() == 0 )
			return;
		synchronized(heartList){
			list = new ArrayList<HeartbeatInfo>(heartList);
			heartList.clear();
		}
		
		Iterator<HeartbeatInfo> iter = list.iterator();
		while( iter.hasNext() ){
			HeartbeatInfo hi = iter.next();
			ComRtu rtu = RtuManage.getInstance().getComRtuInCache(hi.rtua);
			if( null == rtu ){
				iter.remove();
				continue;
			}
			if( rtu.getHeartSavePosition()<0 ){
				uniqRtus.put(rtu.getRtua(), rtu);
			}
		}
		addNewRtu2File(uniqRtus.values());
		uniqRtus = null;
		
		iter = list.iterator();
		synchronized(instance){
			while( iter.hasNext() ){
				HeartbeatInfo hi = iter.next();
				ComRtu rtu = RtuManage.getInstance().getComRtuInCache(hi.rtua);
				int rtua = buffer.getInt(rtu.getHeartSavePosition());
				if( rtua != rtu.getRtua() ){
					log.warn("�ն�RTUA��λ��һ�£�"+HexDump.toHex(rtua));
					continue;
				}
				//������ʱ�䣬���㵽λ
				Calendar cl = Calendar.getInstance();
				cl.setTimeInMillis(hi.time);
				int hour = cl.get(Calendar.HOUR_OF_DAY);
				int minute = cl.get(Calendar.MINUTE);
				int quotient = minute / 5;
				int bitPos = (hour * 12) + quotient ;
				int delta = minute % 5;
				if( delta>=4 )
					bitPos++;
				quotient = bitPos / 8 ;		//�ڼ����ֽ�
				delta = 7 - (bitPos % 8) ;	//����ڼ�λ
				byte b = 1;
				if( delta>0 )
					b = (byte)(b << delta);
				int pos = rtu.getHeartSavePosition() + 4 + quotient;
				buffer.put(pos, (byte)(buffer.get(pos)|b) );
			}
			buffer.force();
		}
		list = null;
	}
	
	class HeartbeatInfo{
		public int rtua = 0;
		public long time = System.currentTimeMillis();
	}
	
	class HeartbeatHandleThread extends Thread {
		public HeartbeatHandleThread(){
			super("HeartbeatHandle");
			this.setDaemon(true);
			start();
		}
		
		@Override
		public void run() {
			_state = RUNNING;
			while(true){
				try{
					synchronized(heartList){
						heartList.wait(1000*60);
						if( heartList.size()==0 )
							continue;
					}
					long t1 = System.currentTimeMillis();
					processHeartInfoList();
					if( log.isDebugEnabled() ){
						long t2 = System.currentTimeMillis();
						log.debug("save heartbeat takes "+(t2-t1)+" milliseconds");
					}
					if( _state == STOPPING )
						break;
				}catch(Exception e){
					log.error(e.getLocalizedMessage(),e);
				}
			}
			_state = STOPPED;
		}
	}

	public final void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
