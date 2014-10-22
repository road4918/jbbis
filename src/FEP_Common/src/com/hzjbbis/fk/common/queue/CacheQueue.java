/**
 * �������ȼ��Ļ�����С�
 */
package com.hzjbbis.fk.common.queue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fk.message.MessageLoader;

/**
 * @author bhw
 * 2008��06��03 23��03
 */
public class CacheQueue {
	//��������
	private static String s_cachePath;
	static{
		String workDir = System.getProperty("user.dir");
		s_cachePath = workDir + File.separator + "cache";
		File f = new File(s_cachePath);
		if( !f.exists() )
			f.mkdir();
	}

	//����������
	private int maxSize = 10000;		//������maxSize��д�ļ���д�ļ���Ϣ��maxSize-minSize
	private int minSize = 2000;
	private int maxFileSize = 20;		//�����ļ����Ĵ�С��M���ֽڡ�
	private int fileCount = 100;		//�����ļ��ĸ�����
	private String key="undefine";		//���������������˿�
	private MessageLoader messageLoader;
	private String cachePath;
	
	//��������
	private static final Logger log = Logger.getLogger(CacheQueue.class);
	private final ArrayList<LinkedList<IMessage>> queue = new ArrayList<LinkedList<IMessage>>(IMessage.PRIORITY_MAX+1);
	private final ArrayList<IMessage> tempQueue = new ArrayList<IMessage>(maxSize);
	private final Object fileLock = new Object();
	private long minInterval = 10*1000;		//����������10�롣�������
	private long lastCacheRead = System.currentTimeMillis() - minInterval;
	private long lastCacheWrite = System.currentTimeMillis() - minInterval;
	
	@SuppressWarnings("unchecked")
	public CacheQueue(){
		for(int i=0; i<IMessage.PRIORITY_MAX+1; i++ ){
			LinkedList<IMessage> list = new LinkedList<IMessage>();
			queue.add(list);
		}
		try{
			Class<MessageLoader> clz = (Class<MessageLoader>)Class.forName("com.hzjbbis.fk.message.zj.MessageLoader4Zj");
			messageLoader = (MessageLoader)clz.newInstance();
		}catch(ClassNotFoundException notFoundExp){
			log.error(notFoundExp.getLocalizedMessage(),notFoundExp);
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
		cachePath = s_cachePath;
	}

	/**
	 * ����Ϣ�������ȼ����С�
	 * @param message
	 */
	public void offer(IMessage message){
		if( message.getPriority()< IMessage.PRIORITY_LOW || message.getPriority()>IMessage.PRIORITY_MAX )
			message.setPriority(IMessage.PRIORITY_NORMAL);
		synchronized(queue){
			if( size()>= maxSize ){
				//�����������ļ���
				asyncSaveQueue();
			}
			queue.get(message.getPriority()).add(message);
			queue.notifyAll();
		}
	}
	
	/**
	 * �����ȼ�������ȡ����Ϣ�����Ϊ�գ�����null����������
	 * @return
	 */
	public IMessage poll(){
		synchronized(queue){
			while(true){
				//���ȴӶ�����ȡ��Ϣ
				for(int i=IMessage.PRIORITY_MAX; i>=0; i-- ){
					LinkedList<IMessage> list = queue.get(i);
					if( list.size()>0 )
						return list.remove();
				}
				//�����ϣ��ٴӶ���ȡ�����ǵ��ļ����ʵ�Ч�ʡ��ڿ���ʱ�̣�ֱ�Ӱ��ļ��ϴ���ǰ�û�
				if( _loadFromFile() )
					continue;
				return null;
			}
		}
	}
	
	/**
	 * peek message from queue if possible.
	 * @return
	 */
	public IMessage peek(){
		synchronized(queue){
			while(true){
				//���ȴӶ�����ȡ��Ϣ
				for(int i=IMessage.PRIORITY_MAX; i>=0; i-- ){
					LinkedList<IMessage> list = queue.get(i);
					if( list.size()>0 )
						return list.getFirst();
				}
				//�����ϣ��ٴӶ���ȡ�����ǵ��ļ����ʵ�Ч�ʡ��ڿ���ʱ�̣�ֱ�Ӱ��ļ��ϴ���ǰ�û�
				if( _loadFromFile() )
					continue;
				return null;
			}
		}
	}
	
	/**
	 * �����ȼ�������ȡ����Ϣ��
	 * @return
	 */
	public IMessage take(){
		synchronized(queue){
			while(true){
				//���ȴӶ�����ȡ��Ϣ
				for(int i=IMessage.PRIORITY_MAX; i>=0; i-- ){
					LinkedList<IMessage> list = queue.get(i);
					if( list.size()>0 )
						return list.remove();
				}
				//�����ϣ��ٴӶ���ȡ�����ǵ��ļ����ʵ�Ч�ʡ��ڿ���ʱ�̣�ֱ�Ӱ��ļ��ϴ���ǰ�û�
				// _loadFromFile()����true������Ѿ���������Ϣ��
				if( _loadFromFile() )
					continue;
				try{
					queue.wait();
				}catch(InterruptedException e){
					return null;
				}
			}
		}
	}
	
	public int size(){
		int size = 0;
		for(int i=IMessage.PRIORITY_MAX; i>=0; i-- ){
			LinkedList<IMessage> list = queue.get(i);
			size += list.size();
		}
		return size;
	}
	
	//��������Ҫ������ʱ����ϵͳ�رյ��¶����ͷţ����������ݻ��浽�ļ���
	public void dispose(){
		log.info("cacheQueue disposed. key="+key);
		synchronized(tempQueue){
			for(int i=0; i<=IMessage.PRIORITY_MAX; i++ ){
				LinkedList<IMessage> ar = queue.get(i);
				tempQueue.addAll(ar);
				ar.clear();
			}
			lastCacheWrite = System.currentTimeMillis();
			__saveToFile();
		}
	}
	
	/**
	 * �첽������Ϣ���С�
	 */
	public void asyncSaveQueue(){
		int count = maxSize-minSize;
		synchronized(tempQueue){
			long now = System.currentTimeMillis();
			if( now - lastCacheWrite < this.minInterval )
				return;
			//���ȼ��͵Ĳ���ȫ��д���档
			for(int i=0; i<=IMessage.PRIORITY_MAX; i++ ){
				LinkedList<IMessage> ar = queue.get(i);
				tempQueue.addAll(ar);
				count -= ar.size();
				ar.clear();
				if( count<= 0 )
					break;
			}
			//��ʼ�첽д��Ϣ�������ļ���
			lastCacheWrite = System.currentTimeMillis();
			CacheFileWriteThread t = new CacheFileWriteThread();
			t.start();
		}
	}
	
	private void __saveToFile(){
		synchronized(fileLock){
			PrintWriter out;
			String filename = _findWriteCacheFileName();
			try
			{
				out = new PrintWriter(new BufferedWriter(new FileWriter(filename,true)));
				_saveMessages(out);
				out.close();
				out = null;
			}
			catch(Exception exp)
			{
				StringBuffer sb = new StringBuffer();
				sb.append("��Ϣ���б����쳣,filename=").append(filename);
				sb.append(",�쳣ԭ��").append(exp.getLocalizedMessage());
				log.error(sb.toString(),exp);
				return ;
			}
		}
	}
	
	private boolean _loadFromFile(){
		long now = System.currentTimeMillis();
		if( now-this.lastCacheRead < this.minInterval )
			return false;
		this.lastCacheRead = System.currentTimeMillis();
		
		synchronized(fileLock){
			RandomAccessFile raf = null;
			String filename = _findReadCacheFileName();
			if( null == filename )
				return false;
			if( log.isDebugEnabled() )
				log.debug("begin read cache file(��ʼ���ػ����ļ�):"+filename);
			try{
				raf = new RandomAccessFile(filename,"rwd");
				String serial;
				int count =0;
				int maxCount = this.maxSize - this.minSize;
				
				while( null != (serial=raf.readLine()) ){
					if( serial.length()<26 ){
						log.warn("��������󣺶�ȡ��Ч���ݣ�"+serial);
						continue;
					}
					//ע�⣬Ŀǰֻ֧���㽭��Լ��ʽ��Ϣ
					IMessage msg = messageLoader.loadMessage(serial);
					if( null == msg )
						continue;
					this.offer(msg);		//�ӻ�����سɹ�
					count++;
					if( count>= maxCount )
						break;
				}
				if( count>0 && log.isInfoEnabled() )
					log.info("���δӻ����ļ�װ����Ϣ�����file="+filename+",count="+count);
				
				//�������������̫�࣬����Ҫ��ʣ����Ϣ�Ƶ��ļ�ͷ��
				long readPos = raf.getFilePointer();
				long writePos = 0;
				int n = 0;
				long remaining = raf.length() - readPos;
	
				byte buffer[] = new byte[512*1024];
				while( remaining>0 ){
					raf.seek(readPos);
					n = raf.read(buffer);
					if( n<=0 )
						break;
					raf.seek(writePos);
					raf.write(buffer,0,n);
					readPos += n;
					writePos += n;
					remaining -= n;
				}
				raf.setLength(writePos);
				raf.close();
				raf = null;
				return count>0;
			}catch(Exception exp){
				StringBuffer sb = new StringBuffer();
				sb.append("�ӻ���װ�ص���Ϣ�����쳣,filename=").append(filename);
				sb.append(",ԭ��").append(exp.getLocalizedMessage());
				log.error(sb.toString(),exp);
				if( null != raf ){
					try{
						raf.close();
						raf = null;
					}
					catch(Exception e){}
				}
			}
		}
		return false;
	}
	
	private void _saveMessages(PrintWriter out){
		synchronized(tempQueue){
			String strMsg = null;
			for( IMessage msg : tempQueue ){
				strMsg = messageLoader.serializeMessage(msg);
				if( null != strMsg && strMsg.length()>0  )
					out.println(strMsg);
			}
			tempQueue.clear();
		}
	}
	
	/**
	 * Ϊ��д���棬��ȡһ�������ļ����ơ��ļ����Ƹ�ʽ�� cache-port(key)-(i).txt
	 * @return filename
	 */
	private String _findWriteCacheFileName(){
		String fname0 = "cache-"+key+"-";
		File f = new File(cachePath);
		File [] list = f.listFiles();
		if( null == list )
			list = new File[0];
		
		File file, oldestFile = null;
		Date oldDate = new Date(0),curDate = new Date(0);
		boolean found;
		for(int i=0; i<fileCount; i++ ){
			String cname = fname0+i+".txt";
			found = false;
			for(int j=0;j<list.length; j++){
				if( !list[j].isFile() ) continue;
				if( list[j].getName().indexOf(fname0)<0 )
					continue;
				
				if( null == oldestFile ){
					oldestFile = list[j];
					oldDate.setTime(oldestFile.lastModified());
				}
				else{
					curDate.setTime(list[j].lastModified());
					if( curDate.before(oldDate)){
						oldDate.setTime(curDate.getTime());
						oldestFile = list[j];
					}
				}
				if( cname.equals(list[j].getName()) ){
					found = true;
					file = new File(cachePath+File.separator+cname);
					if( file.length()>= maxFileSize*1024*1024 )
						continue;
					else
						return file.getPath();
				}
			}
			if( !found )
				return cachePath+File.separator + cname;
		}
		//
		if( null != oldestFile ){
			String opath = oldestFile.getPath();
			oldestFile.delete();
			return opath;
		}
		return cachePath+File.separator + fname0+"exp.txt";
	}
	
	/**
	 * Ϊ�˶����棬��ȡһ�������ļ����ơ��ļ����Ƹ�ʽ�� cache-port(key)-(i).txt
	 * @return filename
	 */
	public String _findReadCacheFileName(){
		String fname0 = "cache-"+key+"-";
		File f = new File(cachePath);
		File [] list = f.listFiles();
		if( null == list ){
			log.warn(f.getPath()+":�б����null==list");
			return null;
		}
		
		File file;
		for(int j=0;j<list.length; j++){
			file = list[j];
			if( !file.isFile() || file.length()<=0 ) continue;
			
			String s = file.getName();
			if( s.indexOf(fname0) == 0 )
				return file.getPath();
		}
		if( log.isDebugEnabled() )
			log.debug(f.getPath()+":Ŀ¼���޻����ļ���");
		return null;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		if( maxSize<= 100 )
			maxSize = 100;
		else if( maxSize> 200000 )
			maxSize = 200000;
		this.maxSize = maxSize;
		_adjustMinSize();
	}
	
	private void _adjustMinSize(){
		if( minSize <=(maxSize>>4) || minSize>(maxSize>>2) )
			minSize = maxSize>>4;
	}
	
	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
		_adjustMinSize();
	}

	public int getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(int maxFileSize) {
		if( maxFileSize<=0 || maxFileSize> 100 )
			maxFileSize = 100;		//���100M
		this.maxFileSize = maxFileSize;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public MessageLoader getMessageLoader() {
		return messageLoader;
	}

	public void setMessageLoader(MessageLoader messageLoader) {
		this.messageLoader = messageLoader;
	}
	
	/**
	 * �첽д�����ļ�.������Դ�� tempQueue
	 * @author hbao
	 *
	 */
	class CacheFileWriteThread extends Thread {
		public CacheFileWriteThread(){
			super("cacheWrite-"+key);
		}
		@Override
		public void run() {
			__saveToFile();
		}
	}

	public void setCachePath(String cachePath) {
		this.cachePath = cachePath;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}
}
