package com.hzjbbis.fas.model;

import java.util.HashMap;

/**
 * д�����������
 * @author ������
 */
public class FaalWriteParamsRequest extends FaalRequest {

    private static final long serialVersionUID = 6624139932292640887L;
    
    /** ������� */
    private String tn;
    
    /**key:������·�ţ�value:������*/
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
