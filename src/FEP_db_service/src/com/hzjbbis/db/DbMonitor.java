/**
 * ���ݿ��ء�
 */
package com.hzjbbis.db;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.Assert;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.tracelog.TraceLog;

/**
 * @author bhw
 *
 */
public class DbMonitor {
	private static final Logger log = Logger.getLogger(DbMonitor.class);
	private static final TraceLog tracer = TraceLog.getTracer();
	private static final ArrayList<DbMonitor> dbMonitors = new ArrayList<DbMonitor>();
	//����������
	private String name = "defaultDbMonitor";
	private String serverIp;
	private int serverPort;
	private DataSource dataSource;
	private String testSql = "select * from dual";
	private int connectTimeout = 2;			//���ݿ����Ӳ��ԣ����ӳ�ʱʱ�䣨�룩
	private int testInterval = 90;			//���Ӳ��Լ�����룩.
	private FasSystem fasSystem = null;		//FasSystem��ע��վ���ݿ⣬�����Ҫ���ô˶����Ա���¡�
	
	//״̬����
	private boolean dbAvailable = false;
	//�ڲ�����
	private boolean initialized = false;
	//������ݿ�ļ�أ���Ҫ��ʱ������ݿ��Ƿ�ָ���
	private static final DbMonitorThread daemonThread = new DbMonitorThread();

	private DbMonitor(){}
	
	public static final DbMonitor createInstance(){
		DbMonitor monitor =  new DbMonitor();
		dbMonitors.add(monitor);
		return monitor;
	}
	
	public static final DbMonitor getMonitor(String name){
		if( null == name || name.length() ==0 )
			name = "defaultDbMonitor";
		for(int i=0; i<dbMonitors.size(); i++)
			if( dbMonitors.get(i).name.equals(name) )
				return dbMonitors.get(i);
		return null;
	}
	
	public static final DbMonitor getMonitor(DataSource ds){
		for(DbMonitor dm : dbMonitors ){
			if( dm.dataSource == ds )
				return dm;
		}
		return null;
	}
	
	public static final DbMonitor getMasterMonitor(){
		return getMonitor((String)null);
	}
	
	public void initialize(){
		this.testDbConnection();
		daemonThread.add(this);
	}
	
	public boolean testSocketConnectable(){
		Socket socket = new Socket();
		try{
			socket.connect(new InetSocketAddress(serverIp,serverPort),connectTimeout*1000);
			socket.close();
			if( null == testSql || testSql.length()<5 )
				setAvailable(true);
		}catch(Exception e){
			log.warn("���ݿ������socket����ʧ�ܣ�ip="+serverIp+",port="+serverPort);
			setAvailable(false);
			return false;
		}
		finally{
			socket = null;
		}
		return true;
	}
	
	public boolean testDbConnection(){
		initialized = true;
		Assert.notNull(dataSource, "dataSource must not be null");
		if( !isAvailable() ){
			if(! testSocketConnectable() )
				return false;
		}
		
		if( null == testSql || testSql.length()<5 )
			return true;
		
		Connection con = null;
		try{
			con = DataSourceUtils.getConnection(dataSource);
			con.createStatement().executeQuery(this.testSql);
			setAvailable(true);
		}catch(Exception e){
			setAvailable(false);
		}
		finally{
			DataSourceUtils.releaseConnection(con, dataSource);
		}
		return isAvailable();
	}
	
	public final boolean isAvailable() {
		if( !initialized )
			this.testDbConnection();
		return dbAvailable;
	}

	public final void setAvailable(boolean available) {
		if( null == fasSystem )
			fasSystem = FasSystem.getFasSystem();
		if( null != fasSystem )
			fasSystem.setDbAvailable(available);
		if( this.dbAvailable != available ){
			tracer.trace(name + " detect DB available is:" + available );
		}
		this.dbAvailable = available;
	}

	public final void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public final void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public final void setTestSql(String testSql) {
		this.testSql = testSql;
	}
	
	public final void setJdbcUrl(String url){
		//url��ʽ��jdbc:oracle:thin:@10.136.34.27:1521:fas
		//jdbc:mysql://192.168.0.185/mysql?autoReconnect=true&useUnicode=true&characterEncoding=utf-8
		int index = url.indexOf("@");
		if( index>0 ){
			String serverAddr = url.substring(index+1);
			index = serverAddr.indexOf(":");
			serverIp = serverAddr.substring(0, index);
			serverAddr = serverAddr.substring(index+1);
			index = serverAddr.indexOf(":");
			if( index>0 )
				serverAddr = serverAddr.substring(0,index);
			else
				serverAddr = serverAddr.trim();
			try{
				serverPort = Integer.parseInt(serverAddr);
			}catch(Exception e){
				System.out.println(e.getLocalizedMessage());
			}
		}
		else{
			index = url.indexOf("//");
			if( index< 0 )
				return;
			String serverAddr = url.substring(index+2);
			index = serverAddr.indexOf("/");
			if( index>0 ){
				serverAddr = serverAddr.substring(0,index);
				index = serverAddr.indexOf(":");
				if( index>0 ){
					String strPort = serverAddr.substring(index+1);
					serverIp = serverAddr.substring(0, index);
					try{
						serverPort = Integer.parseInt(strPort);
					}catch(Exception e){}
				}
				else{
					serverIp = serverAddr;
					serverPort = 3306;	//mysqlĬ�϶˿���3306
				}
			}
		}
	}
	
	public final void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public final void setTestInterval(int testInterval) {
		this.testInterval = testInterval;
	}
	
	static class DbMonitorThread extends Thread{
		private final ArrayList<DbMonitor> monitors = new ArrayList<DbMonitor>();
		public DbMonitorThread(){
			super("DbMonitorDaemonThread");
			this.setDaemon(true);
			this.start();
		}
		
		public void add(DbMonitor monitor){
			monitors.add(monitor);
		}
		
		@Override
		public void run() {
			while(true){
				try{
					if( monitors.size()==0 ){
						Thread.sleep(3*1000);
						continue;
					}
					int interval = 3600;
					for(DbMonitor m: monitors){
						if( m.testInterval> 60 ){
							if( m.testInterval< interval )
								interval = m.testInterval;
						}
						else
							interval = 60;
					}
					Thread.sleep(interval*1000);
					for(DbMonitor m: monitors){
						m.testDbConnection();
					}
				}catch(Exception e){
					log.warn("dbMonitor test exception:"+e.getLocalizedMessage(),e);
				}
			}
		}
	}

	public final void setFasSystem(FasSystem fasSystem) {
		this.fasSystem = fasSystem;
	}
	
}
