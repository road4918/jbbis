/**
 * �����ն˻����У����¼���ʧ�ܵ��նˣ��������б��Ļ��浽�ļ���
 */
package com.hzjbbis.fk.fe.filecache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.message.zj.MessageZj;

/**
 * @author bhw
 *
 */
public class UndocRtuMsgCache {
	private static final Logger log = Logger.getLogger(UndocRtuMsgCache.class);
	private static String path;			//δ�鵵���ն˱��Ļ����ļ�·����
	private static int maxCount = 10;	//���10���ļ�����ѭ������
	private static int maxSizeM = 100;	//ÿ���ļ����100M��
	//�����ļ����ơ�������������+".1"��".2"
	private static final String fileName = "undocRtu.msg";
	private static final Object fLock = new Object();
	private static final int maxMessageSize = 100;
	private static final ArrayList<MessageZj> msgPool = new ArrayList<MessageZj>(maxMessageSize);

	static{
		//����Ƿ����dataĿ¼
		try{
			File file = new File("data");
			file.mkdirs();
			path = file.getAbsolutePath();
			System.out.println("undocumented rtu message file path= "+path);
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	
	public static void addMessage(MessageZj msg){
		synchronized(fLock){
			if( msgPool.size() == maxMessageSize )
				flush();
			msgPool.add(msg);
		}
	}
	
	public static void flush(){
		if( msgPool.size()==0 )
			return;
		synchronized( fLock ){
			try{
				_save2File();
				msgPool.clear();
			}catch(Exception e){
				log.error("Undocument RTU message save to cache exception:"+e.getLocalizedMessage(),e);
			}
		}
	}
	
	private static void _save2File() throws IOException{
		String nextPath = getNextFilePath();
		PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(nextPath,true),1024*1024));
		for(int i=0;i<msgPool.size(); i++){
			MessageZj msg = msgPool.get(i);
			if( null == msg )
				continue;
			printer.println(msg.getRawPacketString());
		}
		printer.flush();
		printer.close();
	}
	/**
	 * ������һ����Ч�Ļ����ļ�·�����ơ�
	 * @return
	 */
	private static String getNextFilePath(){
		String npath = path + File.separatorChar + fileName;
		int stdNameLen = fileName.length();
		File f = new File(npath);
		if( !f.exists() )
			return npath;
		if( f.length()>= (maxSizeM<<20 ) ){
			//��ǰ�ļ��Ѿ����ˣ���Ҫ�������ơ�
			f = new File(path);
			File[] allFiles = new File[maxCount+1];
			int maxIndex = -1;
			File[] files = f.listFiles();
			for(int i=0; i<files.length; i++){
				if( ! files[i].isFile() )
					continue;
				String fn =  files[i].getName();
				if( !fn.startsWith(fileName) )
					continue;
				String pfix = fn.substring(stdNameLen+1);
				if( pfix.length()<=0 )
					continue;
				int appendInt = Integer.parseInt(pfix);
				if( appendInt>= allFiles.length )
					continue;
				allFiles[appendInt] = files[i];
				maxIndex = i;
			}
			for( int i=maxIndex; i>=0; i-- ){
				if( i >= maxCount ){
					allFiles[i].delete();
					continue;
				}
				npath = path + File.separatorChar + fileName+"."+(i+1);
				allFiles[i].renameTo(new File(npath));
			}
		}
		return path + File.separatorChar + fileName;
	}
	
	public static void setPath(String pstr){
		try{
			File file = new File(pstr);
			if( !file.isDirectory() ){
				log.error("δ�鵵�ն�������Ϣ����Ŀ¼�����ڣ�"+pstr);
				return;
			}
			path = file.getAbsolutePath();
			System.out.println("undocumented rtu message file path= "+path);
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	public static void setMaxCount(int mc){
		if( mc>0 )
			maxCount = mc;
	}
	public static void setMaxSizeM(int sizeM){
		if( sizeM>0 )
			maxSizeM = sizeM;
	}
}
