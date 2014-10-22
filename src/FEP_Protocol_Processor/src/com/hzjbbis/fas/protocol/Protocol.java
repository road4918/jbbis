package com.hzjbbis.fas.protocol;

/**
 * 规约类型定义
 * @author 张文亮
 */
public abstract class Protocol {

    /** 浙江规约 */
    public static final String ZJ = "01";
    /** 国网(2004)规约 */
    public static final String G04 = "02";
    /** 国网(1996)规约 */
    public static final String G96 = "03";
    /** 有线终端规约--扩充的浙江规约，这样是方便不同的流程处理 */
    public static final String NET = "04";
    
    public static final String HGCS = "05";
    
    public static final String HGOM = "06";
    
    /**浙江配变规约*/
    public static final String ZJPB = "07";
}
