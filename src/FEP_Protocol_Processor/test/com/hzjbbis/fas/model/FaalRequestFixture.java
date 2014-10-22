package com.hzjbbis.fas.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FaalRequestFixture {

    public static FaalRequest newFaalRequest() {
        FaalRequest request = new FaalRealTimeWriteParamsRequest();
        request.setRtuIds(new String[]{"T0001", "T0002"});
        request.addParam("8041", null);
        request.addParam("8042", null);
        return request;
    }
    
    public static FaalRealTimeWriteParamsRequest newFaalRealTimeWriteParamsRequest() {
        FaalRealTimeWriteParamsRequest request = new FaalRealTimeWriteParamsRequest();
        request.setRtuIds(new String[]{"1111", "2222", "3333"});
        List cmdIds = new ArrayList(3);
        cmdIds.add("111");
        cmdIds.add("222");
        cmdIds.add("333");
        request.setCmdIds(cmdIds);
        request.setCmdTime(Calendar.getInstance());
        request.setTimeout(120);
        request.setTn("0");
        request.addParam("8010", "100.99");
        
        return request;
    }
    
    public static FaalReadCurrentDataRequest newReadCurrentDataRequest() {
        FaalReadCurrentDataRequest request = new FaalReadCurrentDataRequest();
        request.setRtuIds(new String[]{"33022000"});
        request.setTn(new String[]{"1"});
        
        return request;
    }
    
    public static FaalWriteParamsRequest newWriteParamsRequest() {
        FaalWriteParamsRequest request = new FaalWriteParamsRequest();
        request.setRtuIds(new String[]{"33022000"});
        request.setTn("1");
        
        return request;
    }

}
