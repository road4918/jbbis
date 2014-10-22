// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi 
// Source File Name:   FasSystem.java

package com.hzjbbis.fk;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;

import com.hzjbbis.fk.common.events.GlobalEventHandler;
import com.hzjbbis.fk.common.events.event.MemoryProfileEvent;
import com.hzjbbis.fk.common.simpletimer.TimerScheduler;
import com.hzjbbis.fk.common.spi.IClientModule;
import com.hzjbbis.fk.common.spi.IEventHook;
import com.hzjbbis.fk.common.spi.IModule;
import com.hzjbbis.fk.common.spi.IProfile;
import com.hzjbbis.fk.common.spi.socket.IServerSideChannel;
import com.hzjbbis.fk.common.spi.socket.ISocketServer;
import com.hzjbbis.fk.message.gate.MessageGate;
import com.hzjbbis.fk.tracelog.TraceLog;

public class FasSystem
{

    private static FasSystem fasSystem = null;
    public static final String name = "fasSystem";
    private static final Logger log = Logger.getLogger(FasSystem.class);
    //����������
    private List<IModule> modules, unMonitoredModules;
    private List<IEventHook> eventHooks;
    private boolean testMode = false;
    //Ϊ����������ϵͳ��֧��ȫ�ֶ����塣���ֶ����ڶ���󣬻��Զ���ɹ�����
    private List<Object> globalObjects;
    private MemoryProfileEvent memProfile = new MemoryProfileEvent();
    private List<IProfile> profileObjects;
    private boolean dbAvailable = false;
    //֧�ֶ�����ء�ҵ��������profile�ɼ���
    private Map<String,String> systemsProfile = new HashMap<String,String>();
    private int waitCount = -1;
    private String osProfile = null;
    
    //ϵͳ�ر���Ҫ��������顣
    private final List<Runnable> shutdownHooks = new ArrayList<Runnable>();

    //��ʼ��ʱ���������õ�����
    private ApplicationContext applicationContext;
    private boolean running = false;
    
    public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public static FasSystem getFasSystem()
    {
        if(null == fasSystem)
            fasSystem = new FasSystem();
        return fasSystem;
    }

    public FasSystem()
    {
        fasSystem = this;
    }

    public void initialize(){
    	Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				log.info("Run shutdownHooks");
				for(Runnable runIt : shutdownHooks ){
					try{
						runIt.run();
					}catch(Exception e){
						log.warn("shutdownHook exception:"+e.getLocalizedMessage(),e);
					}
				}
			}
    	});
    }

    public void showProfile(){
    	if( ! running )
    		return;
    	TraceLog.getTracer().trace(memProfile.profile());
    	for( IModule module: modules ){
    		try{
    			TraceLog.getTracer().trace(module.profile());
    		}catch(Exception e){}
    	}

    	for( IEventHook hook: eventHooks ){
    		try{
    			TraceLog.getTracer().trace(hook.profile());
    		}catch(Exception e) { }
    	}
    }

    public List<IModule> getModules()
    {
        return modules;
    }

    public void setModules(List<IModule> modules)
    {
        this.modules = modules;
    }

    public List<IEventHook> getEventHooks()
    {
        return eventHooks;
    }

    public void setEventHooks(List<IEventHook> eventHooks)
    {
        this.eventHooks = eventHooks;
    }

    public void addModule(IModule module)
    {
        if(null == modules)
            modules = new ArrayList<IModule>();
        modules.remove(module);			//��ֹ�ظ���ӡ�
        modules.add(module);
    }

    public void addEventHook(IEventHook hook)
    {
        if(null == eventHooks)
            eventHooks = new ArrayList<IEventHook>();
        eventHooks.remove(hook);
        eventHooks.add(hook);
    }

    public boolean startModule(String name)
    {
        if(null == modules)
            return false;
        for(IModule module : modules ){
            if(module.getName().equalsIgnoreCase(name))
                return module.start();
        }
        return false;
    }

    public boolean stopModule(String name)
    {
        if(null == modules)
            return false;
        for(IModule module : modules ){
            if(module.getName().equalsIgnoreCase(name)){
                module.stop();
                return true;
            }
        }
        return false;
    }

    public void startSystem()
    {
        if(null == modules || null == eventHooks)
        {
            log.fatal("ϵͳû�������κ�ģ������¼�����ģ�顣�����̣�");
            stopSystem();
           return;
        }
        if( null != this.unMonitoredModules ){
        	for(IModule module : this.unMonitoredModules ){
        		try{
        			module.start();
        		}
        		catch(Exception e){
        			log.warn("unMonitoredModules start exception:"+e.getLocalizedMessage(),e);
        		}
        	}
        }
        if( null != this.eventHooks ){
            for( IEventHook hook: eventHooks ){
            	try{
            		hook.start();
            	}catch(Exception e){
        			log.warn("eventHooks start exception:"+e.getLocalizedMessage(),e);
            	}
            }
        }
        
        if( null != this.modules ){
            for(IModule module: modules ){
            	try{
            		module.start();
            	}catch(Exception e){
        			log.warn("modules start exception:"+e.getLocalizedMessage(),e);
            	}
            }
        }
        running = true;
        log.info("startSystem successfully!");
    }

    public void stopSystem()
    {
    	//shutdown quartz
		try{
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.shutdown();
		}catch(Exception e){
			log.warn("Quartz default schedule shutdown error:"+e.getLocalizedMessage(),e);
		}
		
    	//1. �رո�������ģ��
    	log.info("stopSystem() is called.");
    	if( null != modules ){
            for(IModule module: modules ){
            	try{
            		module.stop();
            	}catch(Exception e){
            		log.warn("modules stop exception:"+e.getLocalizedMessage(),e);
            	}
            }
    	}
    	
    	if( null != eventHooks ){
            for( IEventHook hook: eventHooks ){
            	try{
            		hook.stop();
            	}catch(Exception e){
            		log.warn("event hook stop exception: "+e.getLocalizedMessage(),e);
            	}
            }
    	}
    	
    	if( null != this.unMonitoredModules ){
    		for( IModule module: this.unMonitoredModules ){
    			try{
    				module.stop();
    			}catch(Exception e){
    				log.warn("unMonitoredModules stop exception: "+e.getLocalizedMessage(),e );
    			}
    		}
    	}
    	
    	//2. Ϊ�˸�����ģ���ͷ���Դ��ʱ�䣬��ͣ1�롣
        try
        {
            Thread.sleep(1000L);
        }
        catch(Exception e) { }
        //3. �ر�Deamo�߳�
        try{
	        TimerScheduler.getScheduler().destroy();
	        GlobalEventHandler.getInstance().destroy();
        }catch(Exception e){
        	log.warn("shutdown timerScheduler or global event handler exception:"+e.getLocalizedMessage(),e);
        }
        
        running = false;
        log.info("System stopped successfully !");
     }

    public String getProfile(String type)
    {
        if("module".equalsIgnoreCase(type))
            return getModuleProfile();
        if("eventhook".equalsIgnoreCase(type))
            return getEventHookProfile();
        if( "gather".equalsIgnoreCase(type))
        	return gatherSystemsProfile();
        else
            return getProfile();
    }

    public String getProfile()
    {
        StringBuffer sb = new StringBuffer(2048);
        sb.append("<?xml version=\"1.0\" encoding=\"");
        sb.append(Charset.defaultCharset().name()).append("\"?>\r\n");
        sb.append("<root>");
        if( null != this.osProfile ){
        	sb.append("\r\n    ").append(this.osProfile);
        }
        else{
            sb.append("\r\n    ").append(memProfile.profile());
        }
        if( null != this.profileObjects ){
        	for(IProfile profile: this.profileObjects ){
        		sb.append("\r\n    ").append(profile.profile());
        	}
        }
        for( IModule mod: modules ){
        	sb.append("\r\n    ").append(mod.profile());
        }
        for( IEventHook hook: eventHooks)
        	sb.append("\r\n    ").append(hook.profile());
        sb.append("\r\n    <dbAvailable>").append(dbAvailable).append("</dbAvailable>");
        sb.append("\r\n").append("</root>");
        return sb.toString();
    }

    public String getModuleProfile()
    {
        StringBuffer sb = new StringBuffer(2048);
        sb.append("<root>");
        for( IModule mod: modules ){
        	sb.append("\r\n    ").append(mod.profile());
        }
        sb.append("\r\n    ").append(memProfile.profile());
        sb.append("\r\n").append("</root>");
        return sb.toString();
    }

    public String getEventHookProfile()
    {
        StringBuffer sb = new StringBuffer(2048);
        sb.append("<root>");
        for( IEventHook hook: eventHooks)
        	sb.append("\r\n    ").append(hook.profile());
        sb.append("\r\n").append("</root>");
        return sb.toString();
    }

	public List<Object> getGlobalObjects() {
		return globalObjects;
	}

	public void setGlobalObjects(List<Object> globalObjects) {
		this.globalObjects = globalObjects;
	}
	
	public ApplicationContext getApplicationContext(){
		return applicationContext;
	}
	
	public void setApplicationContext(ApplicationContext context){
		applicationContext = context;
	}

	public List<IModule> getUnMonitoredModules() {
		return unMonitoredModules;
	}

	public void setUnMonitoredModules(List<IModule> unMonitoredModules) {
		if( null != this.unMonitoredModules ){
			this.unMonitoredModules.addAll(unMonitoredModules);
		}
		else
			this.unMonitoredModules = unMonitoredModules;
	}
	
	public void addUnMonitoredModules(IModule module) {
		if( null == this.unMonitoredModules ){
			this.unMonitoredModules = new ArrayList<IModule>();
		}
		this.unMonitoredModules.add(module);
	}
	
	/**
	 * �ɼ����ϵͳ��profile������GPRS���ء����ҵ���������Լ�ͨ��ǰ�û���
	 * ֻ��ͨ��ǰ�û�֧�ָù��ܡ�
	 * @return
	 */
	public synchronized String gatherSystemsProfile(){
		if( waitCount != -1 )
			return null;
		synchronized(systemsProfile){
			systemsProfile.clear();
			waitCount = 0;
		}
        for( IModule mod: modules ){
        	if( mod.getModuleType().equals(IModule.MODULE_TYPE_GPRS_CLIENT)
        			&& mod instanceof IClientModule ){
        		try{
        			IClientModule gprsClient = (IClientModule)mod;
        			if( gprsClient.sendMessage(MessageGate.createMoniteProfileRequest()) )
        				waitCount ++;
        		}catch(Exception e){
        			log.warn(e.getLocalizedMessage(),e);
        		}
        	}
        	else if( mod.getModuleType().equals(IModule.MODULE_TYPE_BP)
        			&& mod instanceof ISocketServer ){
        		for( IServerSideChannel client : ((ISocketServer)mod).getClients() ){
        			if( client.send(MessageGate.createMoniteProfileRequest()))
        				waitCount ++;
        		}
        	}
        }
        //�ȴ�������ء����10��
        int sleepCount = 10*10;
        while( systemsProfile.size()< waitCount && sleepCount>0 ){
        	try{
        		Thread.sleep(100);
        		sleepCount --;
        	}catch(Exception e){ break; }
        }
        addFrontEndProfile(getProfile());
        StringBuffer sb = new StringBuffer(1024*30);
        sb.append("<?xml version=\"1.0\" encoding=\"");
        sb.append(Charset.defaultCharset().name());  sb.append("\"?>\r\n");
        sb.append("<multi-system>\r\n");
        synchronized(systemsProfile){
        	Iterator<String> iter = systemsProfile.values().iterator();
        	while(iter.hasNext()){
        		String prof = iter.next();
        		sb.append(prof);
        	}
        }
        sb.append("</multi-system>");
        waitCount = -1;
        return sb.toString();
	}
	
	private void addSystemProfile(String sysType, String sysId, String profile){
		if( waitCount == -1 )
			return;
		int pos0 = profile.indexOf("<root>\r\n");
		if( pos0<0 )
			return;
		pos0 += 8;
		int pos1 = profile.indexOf("</root>");
		if( pos1<0 )
			return;
		profile = profile.substring(pos0, pos1);
		StringBuffer sb = new StringBuffer(1024*10);
		sb.append("    <system type=\"").append(sysType).append("\" id=\"").append(sysId).append("\">\r\n");
		String[] lines = profile.split("\r\n");
		for(String line : lines ){
			if( line.length()>7 )
				sb.append("      ").append(line).append("\r\n");
		}
		sb.append("    </system>\r\n");
		synchronized(systemsProfile){
			systemsProfile.put(sysId, sb.toString());
		}
	}
	
	public void addGprsGateProfile(String systemId,String profile){
		addSystemProfile("gprsGate",systemId,profile);
	}
	
	public void addBizProcessorProfile(String systemId, String profile){
		addSystemProfile("bp",systemId,profile);
	}
	
	private void addFrontEndProfile(String profile){
		addSystemProfile("frontEnd","fe",profile);
	}

	public synchronized final void setOsProfile(String osProfile) {
		this.osProfile = osProfile;
	}

	public final void setProfileObjects(List<IProfile> profileObjects) {
		this.profileObjects = profileObjects;
	}
	
	public final void addProfileObject(IProfile profileObject){
		if( null == this.profileObjects ){
			this.profileObjects = new ArrayList<IProfile>();
		}
		this.profileObjects.add(profileObject);
	}

	public final boolean isDbAvailable() {
		return dbAvailable;
	}

	public final void setDbAvailable(boolean dbAvailable) {
		this.dbAvailable = dbAvailable;
	}
	
	public final void addShutdownHook( Runnable runIt){
		shutdownHooks.remove(runIt);
		shutdownHooks.add(runIt);
	}
}
