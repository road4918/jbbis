package com.hzjbbis.fas.protocol.conf;

/**
 * ����/����������
 * @author ������
 */
public class CodecConfig {

    /** ������ */
    private int funCode;
    /** ������ʵ���� */
    private String encoderClass;
    /** ������ʵ���� */
    private String decoderClass;
    
    /**
     * @return Returns the funCode.
     */
    public int getFunCode() {
        return funCode;
    }
    /**
     * @param funCode The funCode to set.
     */
    public void setFunCode(int funCode) {
        this.funCode = funCode;
    }
    /**
     * @return Returns the encoderClass.
     */
    public String getEncoderClass() {
        return encoderClass;
    }
    /**
     * @param encoderClass The encoderClass to set.
     */
    public void setEncoderClass(String encoderClass) {
        this.encoderClass = encoderClass;
    }
    /**
     * @return Returns the decoderClass.
     */
    public String getDecoderClass() {
        return decoderClass;
    }
    /**
     * @param decoderClass The decoderClass to set.
     */
    public void setDecoderClass(String decoderClass) {
        this.decoderClass = decoderClass;
    }    
}
