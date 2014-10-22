package com.hzjbbis.fas.protocol.conf;

import com.hzjbbis.util.CastorUtil;

import junit.framework.TestCase;

public class ProtocolDataConfigTest extends TestCase {

    private ProtocolDataConfig config;
    
    protected void setUp() throws Exception {
        super.setUp();
        config = (ProtocolDataConfig) CastorUtil.unmarshal(
                "com/hzjbbis/fas/protocol/zj/conf/protocol-data-config-mapping.xml",
                "com/hzjbbis/fas/protocol/zj/conf/protocol-data-config.xml");
    }

    public void testGetDataItems() {
        assertNotNull(config.getDataItems());
    }
}
