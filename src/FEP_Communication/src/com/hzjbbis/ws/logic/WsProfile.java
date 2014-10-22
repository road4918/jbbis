package com.hzjbbis.ws.logic;

import javax.jws.WebService;

@WebService
public interface WsProfile {
	String allProfile();
	String modulesProfile();
	ModuleSimpleProfile[] getAllModuleProfile();
}
