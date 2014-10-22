package com.hzjbbis.fas.protocol.handler;

import com.hzjbbis.fk.message.zj.MessageZj;

import junit.framework.TestCase;

public class ProtocolHandlerFactoryTest extends TestCase {

    public void testCreateFactory() {
        ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
        assertNotNull(factory);
        ProtocolHandler handler = factory.getProtocolHandler(MessageZj.class);
        assertNotNull(handler);
    }
}
