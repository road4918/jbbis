package com.hzjbbis.fas.protocol.conf;

import com.hzjbbis.fk.message.zj.MessageZj;
import com.hzjbbis.util.CastorUtil;

import junit.framework.TestCase;

public class ProtocolProviderConfigTest extends TestCase {

    private ProtocolProviderConfig config;
    
    protected void setUp() throws Exception {
        super.setUp();
        config = (ProtocolProviderConfig) CastorUtil.unmarshal(
                "/com/hzjbbis/fas/protocol/conf/protocol-provider-config-mapping.xml",
                "/com/hzjbbis/fas/protocol/conf/protocol-provider-config.xml");
    }

    /*
     * Test method for 'com.hzjbbis.fas.protocol.conf.ProtocolProviderConfig.getProtocolHandlerConfig(String)'
     */
    public void testGetProtocolHandlerConfig() {
        ProtocolHandlerConfig handler = config.getProtocolHandlerConfig(MessageZj.class.getName());
        assertNotNull(handler);
        assertEquals(MessageZj.class.getName(), handler.getMessageType());
    }

    /*
     * Test method for 'com.hzjbbis.fas.protocol.conf.ProtocolProviderConfig.getHandlers()'
     */
    public void testGetHandlers() {
        assertNotNull(config.getHandlers());
    }

}
