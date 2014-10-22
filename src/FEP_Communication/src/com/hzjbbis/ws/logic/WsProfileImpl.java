package com.hzjbbis.ws.logic;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import com.hzjbbis.fk.FasSystem;
import com.hzjbbis.fk.common.spi.IModule;

@WebService(endpointInterface = "com.hzjbbis.ws.logic.WsProfile")
public class WsProfileImpl implements WsProfile {

	public String allProfile() {
		return FasSystem.getFasSystem().gatherSystemsProfile();
	}

	public String modulesProfile() {
		return FasSystem.getFasSystem().getModuleProfile();
	}
	
	public ModuleSimpleProfile[] getAllModuleProfile(){
		List<ModuleSimpleProfile> list = new ArrayList<ModuleSimpleProfile>();
		for(IModule mod: FasSystem.getFasSystem().getModules() ){
			ModuleSimpleProfile mp = new ModuleSimpleProfile();
			mp.setLastReceiveTime(mod.getLastReceiveTime());
			mp.setModuleType(mod.getModuleType());
			mp.setName(mod.getName());
			mp.setPerMinuteReceive(mod.getMsgRecvPerMinute());
			mp.setPerMinuteSend(mod.getMsgSendPerMinute());
			mp.setRunning(mod.isActive());
			mp.setTotalReceive(mod.getTotalRecvMessages());
			mp.setTotalSend(mod.getTotalSendMessages());
			list.add(mp);
		}
		return list.toArray(new ModuleSimpleProfile[list.size()]);
	}
}
