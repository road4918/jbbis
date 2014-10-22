/**
 * 浙江电力负控系统－通讯系统启动模块
 */
package com.hzjbbis.fas.startup;

import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.fk.message.IMessage;
import com.hzjbbis.fas.model.FaalReadCurrentDataRequest;
import com.hzjbbis.fas.protocol.handler.ProtocolHandler;
import com.hzjbbis.fas.protocol.handler.ProtocolHandlerFactory;


/**
 * @author bhw
 *
 */
public class Application {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		initialize();
		IMessage msg = new MessageZj();
		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();        
        ProtocolHandler handler = factory.getProtocolHandler(MessageZj.class);
        //IMessage[] responses = handler.process(msg);  
        FaalReadCurrentDataRequest request= new FaalReadCurrentDataRequest();
        IMessage[] messages = handler.createMessage(request);
	}
	
	public static void initialize(){
		ClassLoaderUtil.initializeClassPath();
	}
	
}
