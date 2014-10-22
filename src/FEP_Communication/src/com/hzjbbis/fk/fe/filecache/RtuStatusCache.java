/**
 * �ն˹����ı����ļ����档
 */
package com.hzjbbis.fk.fe.filecache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;

/**
 * @author bhw
 *
 */
public class RtuStatusCache {
	private static final Logger log = Logger.getLogger(RtuStatusCache.class);
	private static final String filename = "rtu-status.dat";
	private static final Object lock = new Object();
	private static String pathName;
	static {
		//����Ƿ����dataĿ¼
		try{
			File file = new File("data");
			file.mkdirs();
			pathName = file.getAbsolutePath() + File.separatorChar + filename;
			System.out.println("rtu-cache file = "+pathName);
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	
	public static final void save2File(Collection<ComRtu> rtus){
		long time1 = System.currentTimeMillis();
		synchronized(lock){
			try{
				_save(rtus);
			}catch(Exception e){
				log.error("�ն˹������浽�ļ��쳣:"+e.getLocalizedMessage(),e);
			}
		}
		if( log.isDebugEnabled() ){
			long spend = System.currentTimeMillis() - time1;
			if( spend == 0 )
				spend = 1;
			long speed = (rtus.size()*1000)/spend;
			log.debug("�����ն�����="+rtus.size()+",����ʱ��(ms)="+spend+",�ٶ�(/s)="+speed);
		}
	}
	
	private static void _save(Collection<ComRtu> rtus) throws IOException{
		PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(pathName,false),1024*1024));
		for( ComRtu rtu: rtus ){
			printer.print(rtu.getRtua()); printer.print('|');
			printer.print(rtu.getRtuId()); printer.print('|');
			printer.print(rtu.getDeptCode()); printer.print('|');
			printer.print(rtu.getRtuProtocol()); printer.print('|');
			printer.print(rtu.getManufacturer()); printer.print('|');		//5

			printer.print(rtu.getSimNum()); printer.print('|');
			printer.print(rtu.getCommType()); printer.print('|');
			printer.print(rtu.getCommAddress()); printer.print('|');
			printer.print(rtu.getB1CommType()); printer.print('|');
			printer.print(rtu.getB1CommAddress()); printer.print('|');
			printer.print(rtu.getB2CommType()); printer.print('|');
			printer.print(rtu.getB2CommAddress()); printer.print('|');

			printer.print(rtu.getActiveGprs()); printer.print('|');
			printer.print(rtu.getActiveUms()); printer.print('|');
			printer.print(rtu.getUpGprsFlowmeter()); printer.print('|');
			printer.print(rtu.getUpSmsCount()); printer.print('|');			//number 16

			printer.print(rtu.getDownGprsFlowmeter()); printer.print('|');
			printer.print(rtu.getDownSmsCount()); printer.print('|');
			printer.print(rtu.getUpMobile()); printer.print('|');
			
			printer.print(rtu.getLastIoTime()); printer.print('|');
			printer.print(rtu.getLastGprsTime()); printer.print('|');
			printer.print(rtu.getLastSmsTime()); printer.print('|');		// 22
			
			printer.print(rtu.getTaskCount()); printer.print('|');
			printer.print(rtu.getUpGprsCount()); printer.print('|');
			printer.print(rtu.getDownGprsCount()); printer.print('|');
			printer.print(rtu.getRtuIpAddr()); printer.print('|');
			printer.print(rtu.getActiveSubAppId()); printer.print('|');		//27
			
			printer.print(rtu.getHeartbeatCount()); printer.print('|');
			printer.print(rtu.getLastHeartbeat()); printer.print('|');
			printer.print(rtu.getHeartSavePosition()); printer.println();
		}
		printer.flush();
		printer.close();
	}
	
	public static List<ComRtu> loadFromFile(){
		synchronized(lock){
			try{
				return _load();
			}catch(Exception e){
				log.error("���ն˹��������ļ������쳣:"+e.getLocalizedMessage(),e);
			}
			return null;
		}
	}
	
	private static List<ComRtu> _load() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(pathName),1024*1024);
		List<ComRtu> list = new LinkedList<ComRtu>();
		while(true){
			String line = reader.readLine();
			if( null == line )
				break;
			String[] items = line.split("\\|");
			if( items.length<30 ){
				log.warn("�޷�����ComRtu����ʽ����ȷ��line="+line);
				break;
			}
			int i=0;
			String strRtua = items[i++];
			int rtua = (int)Long.parseLong(strRtua,16);
			ComRtu rtu = RtuManage.getInstance().getComRtuInCache(rtua);
			if( null == rtu ){
				rtu = new ComRtu();
				rtu.setLogicAddress(strRtua);
				rtu.setRtua( rtua );
				RtuManage.getInstance().putComRtuToCache(rtu);
			}
			rtu.setRtuId(items[i++]);
			rtu.setDeptCode(items[i++]);
			rtu.setRtuProtocol(items[i++]);
			rtu.setManufacturer(items[i++]);

			rtu.setSimNum(items[i++]);
			rtu.setCommType(items[i++]);
			rtu.setCommAddress(items[i++]);

			rtu.setB1CommType(items[i++]);
			rtu.setB1CommAddress(items[i++]);
			rtu.setB2CommType(items[i++]);
			rtu.setB2CommAddress(items[i++]);

			rtu.setActiveGprs(items[i++]);
			rtu.setActiveUms(items[i++]);

			rtu.setUpGprsFlowmeter(Integer.parseInt(items[i++]));
			rtu.setUpSmsCount(Integer.parseInt(items[i++]));

			rtu.setDownGprsFlowmeter(Integer.parseInt(items[i++]));
			rtu.setDownSmsCount(Integer.parseInt(items[i++]));
			rtu.setUpMobile(items[i++]);
			
			rtu.setLastIoTime(Long.parseLong(items[i++]));
			rtu.setLastGprsTime(Long.parseLong(items[i++]));
			rtu.setLastSmsTime(Long.parseLong(items[i++]));
			
			rtu.setTaskCount(Integer.parseInt(items[i++]));
			rtu.setUpGprsCount(Integer.parseInt(items[i++]));
			rtu.setDownGprsCount(Integer.parseInt(items[i++]));
			rtu.setRtuIpAddr( items[i++] );
			rtu.setActiveSubAppId( items[i++] );
			
			rtu.setHeartbeatCount(Integer.parseInt(items[i++]));
			rtu.setLastHeartbeat(Long.parseLong(items[i++]));
			rtu.setHeartSavePosition(Integer.parseInt(items[i++]));
			list.add(rtu);
		}
		return list;
	}
}
