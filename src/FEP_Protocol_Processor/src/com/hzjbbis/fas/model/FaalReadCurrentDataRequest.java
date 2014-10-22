package com.hzjbbis.fas.model;

import java.util.HashMap;

/**
 * 读当前数据请求
 * @author 张文亮
 */
public class FaalReadCurrentDataRequest extends FaalRequest {

    private static final long serialVersionUID = -6192523704322578814L;
    
    /** 测量点号数组 */
    private String[] tn;

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
	
    public FaalReadCurrentDataRequest() {
        super();
        type = FaalRequest.TYPE_READ_CURRENT_DATA;
    }
    
    /**
     * @return Returns the tn.
     */
    public String[] getTn() {
        return tn;
    }

    /**
     * @param tn The tn to set.
     */
    public void setTn(String[] tn) {
        this.tn = tn;
    }    
}
