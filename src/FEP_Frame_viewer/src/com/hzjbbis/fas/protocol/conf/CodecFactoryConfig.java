package com.hzjbbis.fas.protocol.conf;

import java.util.List;

/**
 * ����/��������������
 * @author ������
 */
public class CodecFactoryConfig {

    /** ����/������������ */
    private String factoryClass;
    /** Э���������ӳ���ļ���Դ·�� */
    private String dataConfigMapping;
    /** Э��������������ļ���Դ·�� */
    private String dataConfigResource;
    /** ����/�����������б� */
    private List codecs;
    
    /**
     * @return Returns the factoryClass.
     */
    public String getFactoryClass() {
        return factoryClass;
    }
    /**
     * @param factoryClass The factoryClass to set.
     */
    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }
    /**
     * @return Returns the dataConfigMapping.
     */
    public String getDataConfigMapping() {
        return dataConfigMapping;
    }
    /**
     * @param dataConfigMapping The dataConfigMapping to set.
     */
    public void setDataConfigMapping(String dataConfigMapping) {
        this.dataConfigMapping = dataConfigMapping;
    }
    /**
     * @return Returns the dataConfigResource.
     */
    public String getDataConfigResource() {
        return dataConfigResource;
    }
    /**
     * @param dataConfigResource The dataConfigResource to set.
     */
    public void setDataConfigResource(String dataConfigResource) {
        this.dataConfigResource = dataConfigResource;
    }
    /**
     * @return Returns the codecs.
     */
    public List getCodecs() {
        return codecs;
    }
    /**
     * @param codecs The codecs to set.
     */
    public void setCodecs(List codecs) {
        this.codecs = codecs;
    }
}
