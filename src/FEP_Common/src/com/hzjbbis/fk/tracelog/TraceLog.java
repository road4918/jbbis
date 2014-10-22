/**
 * ���ڼ�¼������Ϣ����ֹLog4j����־��Ϣ��̫��
 */
package com.hzjbbis.fk.tracelog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author bhw
 *
 */
public class TraceLog {
	private static final Map<String,TraceLog> logMap = new HashMap<String,TraceLog>();
	private static final String PROP_MAX_FILE_SIZE = "traceLog.maxFileSize";
	private static final String PROP_FILE_COUNT = "traceLog.fileCount";
	private static final String PROP_TRACE_ENABLED = "traceLog.enabled";
	private static int MAX_FILE_SIZE = 50;	//��λM	ϵͳ����ΪtraceLog.maxFileSize
	private static int FILE_COUNT = 1;	//�ļ����� ϵͳ����ΪtraceLog.fileCount
	private static boolean TRACE_ENABLED = false;
	private static String rootPath;		//��־�ļ��ĸ�Ŀ¼
	private static String defaultKey = "trace";
	//Ϊ֧�ֶ�̬���أ���¼�ļ������ļ��޸�ʱ��
	private static PropFileMonitor monitor = null;
	private static String propFilePath;
	private static Properties g_props = null;
	private static long propFileLastModified = 0;
	
	private boolean enabled = false;
	private String key = defaultKey;	//��־�ļ������ļ��� �Զ��Ӻ�׺: -i.log
	private String filePath ;			//�ļ�·�������ڼ���ļ���С��
	private PrintStream out;
	
	static{
		//����������Ϣ
		try{
			File f = new File("traceLog.properties");
			if( ! f.exists() )
				f = new File("bin"+ File.separatorChar + "traceLog.properties");
			if( !f.exists() )
				f = new File("config"+ File.separatorChar + "traceLog.properties");
			if( f.exists() ){
				propFilePath = f.getCanonicalPath();
				propFileLastModified = f.lastModified();
				g_props = new Properties();
				g_props.load(new FileInputStream(f));
				String strMaxFileSize = g_props.getProperty(PROP_MAX_FILE_SIZE);
				if( null == strMaxFileSize )
					strMaxFileSize = System.getProperty(PROP_MAX_FILE_SIZE);
				if( null != strMaxFileSize )
					strMaxFileSize = strMaxFileSize.trim();
				if( null != strMaxFileSize && strMaxFileSize.length()>0 ){
					if( strMaxFileSize.substring(strMaxFileSize.length()-1).toUpperCase().equals("M"))
						strMaxFileSize = strMaxFileSize.substring(0, strMaxFileSize.length()-1);
					MAX_FILE_SIZE = Integer.parseInt(strMaxFileSize);
					if( MAX_FILE_SIZE<=0 || MAX_FILE_SIZE> 50000 )
						MAX_FILE_SIZE = 50;
				}
				String strFileCount = g_props.getProperty(PROP_FILE_COUNT);
				if( null == strFileCount )
					strFileCount = System.getProperty(PROP_FILE_COUNT);
				if( null != strFileCount )
					strFileCount = strFileCount.trim();
				if( null != strFileCount && strFileCount.length()>0 ){
					FILE_COUNT = Integer.parseInt(strFileCount);
					if( FILE_COUNT<=0 && FILE_COUNT> 1000 )
						FILE_COUNT = 1;
				}
				String strEnabled = g_props.getProperty(PROP_TRACE_ENABLED);
				if( null == strEnabled )
					strEnabled = System.getProperty(PROP_TRACE_ENABLED);
				if( null != strEnabled && strEnabled.length()>0 ){
					try{
						TRACE_ENABLED = Boolean.parseBoolean(strEnabled);
					}catch(Exception e1){
						try{
							int iEnabled = Integer.parseInt(strEnabled);
							if( iEnabled != 0 )
								TRACE_ENABLED = true;
							else
								TRACE_ENABLED = false;
						}catch(Exception e2){							
						}
					}
				}
				
				//�����߳̽��м�������ļ��Ƿ��޸�
				monitor = new PropFileMonitor();
				monitor.start();
			}
		}catch(Exception e){
			
		}
		//����Ƿ����dataĿ¼
		try{
			File file = new File("trace");
			file.mkdirs();
			rootPath = file.getAbsolutePath() + File.separatorChar;
		}catch(Exception exp){
		}
	}
	
	public static TraceLog getTracer(){
		return getTracer(defaultKey);
	}
	
	public static TraceLog getTracer(String myKey){
		if( null == myKey )
			myKey = defaultKey;
		TraceLog log = logMap.get(myKey);
		if( null == log ){
			log = new TraceLog(myKey);
			logMap.put(log.key, log);
			if( null != g_props ){
				String suffix = "."+PROP_TRACE_ENABLED;
				String propKey = null;
				Iterator<Object> iter2 = g_props.keySet().iterator();
				while(iter2.hasNext()){
					String pkey = (String)iter2.next();
					int kindex = pkey.indexOf(suffix);
					if( kindex > 0 ){
						String prefix = pkey.substring(0, kindex);
						if( prefix.equals(log.key) || log.key.indexOf(prefix) ==0 ){
							propKey = pkey;
							break;
						}
					}
				}
				if( null != propKey ){
					String strEnabled = g_props.getProperty(propKey);
					if( null != strEnabled && strEnabled.length()>0 ){
						try{
							log.enabled = Boolean.parseBoolean(strEnabled);
						}catch(Exception e1){}
					}
				}
				else
					log.enabled = TRACE_ENABLED;
			}
		}
		return log;
	}
	
	public static TraceLog getTracer(Class<? extends Object> clz){
		return getTracer(clz.getCanonicalName());
	}

	private TraceLog(String fName){
		key = fName;
	}
	
	private synchronized void createPrintStream(){
		filePath = makeFilePath();
		try{
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(filePath,true)));
		}catch(Exception e){
			e.printStackTrace(System.err);
			System.err.println();
		}
	}
	
	private String makeFilePath(){
		String name = key;
		int index = name.lastIndexOf(File.separatorChar);
		if( index>=0 )
			name = name.substring(index+1);
		index = name.lastIndexOf(".");
		if( index>=0 )
			name = name.substring(index+1);
		
		File f;
		String fpath, fpathMark = null;
		long lastModified = 0;
		int flen;
		for( int i=1; i<= FILE_COUNT ; i++ ){
			fpath = rootPath + name + "-" + i + ".log";
			f = new File(fpath);
			flen = (int)(f.length() >>> 20);		//��λM
			if( flen< MAX_FILE_SIZE )
				return fpath;
			//������Ҫ�滻���ϵ���־�ļ�
			if( 0 ==  lastModified || ( f.lastModified()-lastModified < 0 ) ){
				lastModified = f.lastModified();
				fpathMark = fpath;
			}
		}
		f = new File(fpathMark);
		f.delete();
		return fpathMark;
	}

	public void trace(String info){
		if( enabled )
			write2File(info);
	}

	public void trace(String info,Exception e){
		if( ! enabled )
			return;
		synchronized(this){
			write2File(info);
			if( null != out && null != e ){
				e.printStackTrace(out);
				out.println();
			}
		}
	}

	public void trace(Object obj){
		if( ! enabled )
			return;
		write2File(obj.toString());
	}

	private void checkOutput(){
		if( null == out )
			createPrintStream();
		File f = new File(filePath);
		if( (int)(f.length()>>>20) >= MAX_FILE_SIZE )
			createPrintStream();
	}

	private synchronized void write2File(String info){
		checkOutput();
		if( null == out )
			return;
		StringBuilder sb = new StringBuilder(512);
		Calendar ca = Calendar.getInstance();
		int field = ca.get(Calendar.MONTH);
		if( field< 10 )
			sb.append("0");
		sb.append(field).append("-");
		field = ca.get(Calendar.DAY_OF_MONTH);
		if( field< 10 )
			sb.append("0");
		sb.append(field).append(" ");
		field = ca.get(Calendar.HOUR_OF_DAY);
		if( field< 10 )
			sb.append("0");
		sb.append(field).append(":");
		field = ca.get( Calendar.MINUTE );
		if( field< 10 )
			sb.append("0");
		sb.append(field).append(":");
		field = ca.get( Calendar.SECOND );
		if( field< 10 )
			sb.append("0");
		sb.append(field).append(",");
		field = ca.get( Calendar.MILLISECOND );
		if( field< 10 )
			sb.append("00");
		else if( field< 100 )
			sb.append("0");
		sb.append(field).append(" ");
		
		//��ʼ�����־��Ϣ
		Thread t = Thread.currentThread();
		sb.append("[").append(t.getName()).append("] ");
		StackTraceElement[] ste = t.getStackTrace();
//		for(int i=0; i< ste.length; i++)
//			System.out.println(ste[i]);
		if( null != ste && ste.length>3 ){
			sb.append(ste[4].getFileName()).append(":").append(ste[4].getLineNumber());
		}
		sb.append(" - ").append(info);
		out.println(sb.toString());
		out.flush();
	}
	
	static class PropFileMonitor extends Thread{
		public PropFileMonitor(){
			super("TraceLogCfgMonitor");
			setDaemon(true);
			start();
		}

		public void run(){
			while(true){
				try{
					Thread.sleep(2000);
					File f = new File(propFilePath);
					if( f.lastModified() - propFileLastModified > 0 ){
						propFileLastModified = f.lastModified();
						Properties props = new Properties();
						props.load(new FileInputStream(f));
						String strMaxFileSize = props.getProperty(PROP_MAX_FILE_SIZE);
						if( null != strMaxFileSize )
							strMaxFileSize = strMaxFileSize.trim();
						if( null != strMaxFileSize && strMaxFileSize.length()>0 ){
							if( strMaxFileSize.substring(strMaxFileSize.length()-1).toUpperCase().equals("M"))
								strMaxFileSize = strMaxFileSize.substring(0, strMaxFileSize.length()-1);
							MAX_FILE_SIZE = Integer.parseInt(strMaxFileSize);
							if( MAX_FILE_SIZE<=0 || MAX_FILE_SIZE> 50000 )
								MAX_FILE_SIZE = 50;
						}
						String strFileCount = props.getProperty(PROP_FILE_COUNT);
						if( null != strFileCount )
							strFileCount = strFileCount.trim();
						if( null != strFileCount && strFileCount.length()>0 ){
							FILE_COUNT = Integer.parseInt(strFileCount);
							if( FILE_COUNT<=0 && FILE_COUNT> 1000 )
								FILE_COUNT = 1;
						}
						String strEnabled = props.getProperty(PROP_TRACE_ENABLED);
						if( null != strEnabled && strEnabled.length()>0 ){
							try{
								TRACE_ENABLED = Boolean.parseBoolean(strEnabled);
							}catch(Exception e1){
								try{
									int iEnabled = Integer.parseInt(strEnabled);
									if( iEnabled != 0 )
										TRACE_ENABLED = true;
									else
										TRACE_ENABLED = false;
								}catch(Exception e2){
									
								}
							}
						}
						Iterator<Map.Entry<String,TraceLog>> iter = logMap.entrySet().iterator();
						String suffix = "."+PROP_TRACE_ENABLED;
						while(iter.hasNext()){
							Map.Entry<String,TraceLog> entry = iter.next();
							
							String propKey = null;
							Iterator<Object> iter2 = props.keySet().iterator();
							while(iter2.hasNext()){
								String pkey = (String)iter2.next();
								int kindex = pkey.indexOf(suffix);
								if( kindex > 0 ){
									String prefix = pkey.substring(0, kindex);
									if( prefix.equals(entry.getValue().key) || entry.getValue().key.indexOf(prefix) ==0 ){
										propKey = pkey;
										break;
									}
								}
							}
							if( null != propKey ){
								strEnabled = props.getProperty(propKey);
								if( null != strEnabled && strEnabled.length()>0 ){
									try{
										entry.getValue().enabled = Boolean.parseBoolean(strEnabled);
									}catch(Exception e1){}
								}
							}
							else
								entry.getValue().enabled = TRACE_ENABLED;
							
							if( !entry.getValue().enabled ){
								synchronized(entry.getValue()){
									if( null != entry.getValue().out )
										entry.getValue().out.close();
									entry.getValue().out = null;
								}
							}
						}
						g_props = props;
					}
				}catch(Exception e){}
			}
		}
	}

	public final boolean isEnabled() {
		return enabled;
	}
}
