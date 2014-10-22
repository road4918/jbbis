package com.hzjbbis.fas.model;


/**
 *@filename	FaalReadRtuParameterRequest.java
 *@auther	netice
 *@date		2007-3-28
 *@version	1.0
 *TODO
 */
public class FaalReadRtuParameterRequest extends FaalRequest{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 测量点号数组 */
    private String[] tn;
 
    public FaalReadRtuParameterRequest() {
        super();
        type = FaalRequest.TYPE_READ_RTUPARAM;
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
