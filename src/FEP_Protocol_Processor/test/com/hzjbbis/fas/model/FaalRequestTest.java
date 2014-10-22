package com.hzjbbis.fas.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

public class FaalRequestTest extends TestCase {

    public void testCreateRequest() {
        FaalRequest request = new FaalReadCurrentDataRequest();
        assertNotNull(request);
        request = new FaalReadTaskDataRequest();
        assertNotNull(request);
        request = new FaalWriteParamsRequest();
        assertNotNull(request);
        request = new FaalRealTimeWriteParamsRequest();
        assertNotNull(request);
    }
    
    public void testSerialization() throws Exception {
        FaalRealTimeWriteParamsRequest request = FaalRequestFixture.newFaalRealTimeWriteParamsRequest();        
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baout);
        out.writeObject(request);
        byte[] buf = baout.toByteArray();
        assertNotNull(buf);
        
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf));
        FaalRealTimeWriteParamsRequest newReq = (FaalRealTimeWriteParamsRequest) in.readObject();
        assertNotNull(newReq);
        assertEquals(newReq.getClass(), FaalRealTimeWriteParamsRequest.class);
        assertEquals(newReq.getRtuIds().get(2), "3333");
        assertEquals(newReq.getCmdIds().get(0), "111");
        assertNotNull(newReq.getCmdTime());
        assertEquals(newReq.getTimeout(), 120);
        assertEquals(newReq.getTn(), "0");
        assertEquals(((FaalRequestParam) newReq.getParams().get(0)).getValue(), "100.99");
    }
}
