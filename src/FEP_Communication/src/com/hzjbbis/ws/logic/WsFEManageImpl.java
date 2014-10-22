package com.hzjbbis.ws.logic;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.fe.config.ApplicationPropertiesConfig;
import com.hzjbbis.fk.fe.filecache.MisparamRtuManage;
import com.hzjbbis.fk.fe.filecache.RtuStatusCache;
import com.hzjbbis.fk.model.ComRtu;
import com.hzjbbis.fk.model.RtuManage;

@WebService(endpointInterface = "com.hzjbbis.ws.logic.WsFEManage")
public class WsFEManageImpl implements WsFEManage {
	private static final Logger log = Logger.getLogger(WsFEManageImpl.class);

	public boolean addGprsGateChannel(String ip, int port, String gateName) {
		return ApplicationPropertiesConfig.getInstance().addGprsGate(ip, port, gateName);
	}

	public boolean addUmsChannel(String appid, String password) {
		return ApplicationPropertiesConfig.getInstance().addUmsClient(appid, password);
	}

	public void startModule(String name) {
		FasSystem.getFasSystem().startModule(name);
	}

	public void stopModule(String name) {
		FasSystem.getFasSystem().stopModule(name);
	}

	public void updateFlow() {
		MisparamRtuManage instance = MisparamRtuManage.getInstance();
		RtuManage rm = RtuManage.getInstance();
		List<ComRtu> allRtu = null;
		synchronized(rm ){
			allRtu = new ArrayList<ComRtu>(rm.getAllComRtu());
		}
		try{
			RtuStatusCache.save2File(allRtu);
			instance.saveRtuStatus2Db(allRtu);
		}catch(Exception e){
			log.warn("更新流量异常:"+e.getLocalizedMessage(),e);
		}
	}

}
