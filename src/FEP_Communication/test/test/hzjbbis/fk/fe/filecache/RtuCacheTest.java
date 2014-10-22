package test.hzjbbis.fk.fe.filecache;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.fe.filecache.RtuStatusCache;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.utils.HexDump;


public class RtuCacheTest {
	private static final Logger log = Logger.getLogger(RtuCacheTest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int maxSize = 100000;
		List<ComRtu> list = new LinkedList<ComRtu>();
		long t1 = System.currentTimeMillis();
		for(int i=0; i<maxSize; i++){
			ComRtu rtu = new ComRtu();
			rtu.setActiveGprs("192.168.0.2:"+i);
			rtu.setActiveUms("95598"+i);
			rtu.setB1CommAddress(rtu.getActiveUms());
			rtu.setB1CommType("01");
			rtu.setCommAddress(rtu.getActiveGprs());
			rtu.setCommType("02");
			rtu.addUpGprsFlowmeter(1000+i);
			rtu.setUpSmsCount(i+1);
			rtu.setDeptCode("0312");
			rtu.setLastIoTime(System.currentTimeMillis());
			rtu.setManufacturer("18");
			int rtua = 92010001+i;
			rtu.setRtua(rtua);
			rtu.setLogicAddress(HexDump.toHex(rtua));
			rtu.setSimNum(String.valueOf( 13812300001L+i ) );
			rtu.setRtuId("zdjh0001");
			rtu.setLastIoTime(System.currentTimeMillis());
			
			list.add(rtu);
		}
		long t2 = System.currentTimeMillis();
		RtuStatusCache.save2File(list);
		long t3 = System.currentTimeMillis();
		
		Collection<ComRtu> c = RtuStatusCache.loadFromFile();
		long t4 = System.currentTimeMillis();
		for( ComRtu rtu: c ){
			log.info("rtua="+rtu.getLogicAddress());
		}
		long t5 = System.currentTimeMillis();
		log.info("对象生成毫秒="+(t2-t1)+",保存="+(t3-t2)+",加载="+(t4-t3)+",打印="+(t5-t4));
	}

}
