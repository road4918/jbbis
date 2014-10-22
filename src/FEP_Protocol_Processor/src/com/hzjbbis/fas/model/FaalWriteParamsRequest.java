package com.hzjbbis.fas.model;

import java.util.HashMap;

/**
 * 写对象参数请求
 * @author 张文亮
 */
public class FaalWriteParamsRequest extends FaalRequest {

    private static final long serialVersionUID = 6624139932292640887L;
    
    /** 测量点号 */
    private String tn;
    
    /**key:测量点路号，value:测量点*/
	private HashMap map;
	
	public void setMap(String key,String value){
		if(map==null)
			map = new HashMap();
		map.put(key, value);
	}
	
	public String getValue(String key){
		return (String)map.get(key);
	}
	
    public FaalWriteParamsRequest() {
        super();
        type = FaalRequest.TYPE_WRITE_PARAMS;
    }

    /**
     * @return Returns the tn.
     */
    public String getTn() {
        return tn;
    }
    /**
     * @param tn The tn to set.
     */
    public void setTn(String tn) {
        this.tn = tn;
    }    
}
