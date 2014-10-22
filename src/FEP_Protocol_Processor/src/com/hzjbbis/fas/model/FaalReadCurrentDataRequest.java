package com.hzjbbis.fas.model;

import java.util.HashMap;

/**
 * ����ǰ��������
 * @author ������
 */
public class FaalReadCurrentDataRequest extends FaalRequest {

    private static final long serialVersionUID = -6192523704322578814L;
    
    /** ����������� */
    private String[] tn;

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
