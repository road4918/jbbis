package com.hzjbbis.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.hzjbbis.exception.CastorException;

/**
 * Castor ��������
 * @author ������
 */
public class CastorUtil {
    
    /**
     * �� xml �ļ��н���һ������
     * @param mappingResource ӳ���ļ�
     * @param dataResource �����ļ�
     * @return Java ����
     */
    public static Object unmarshal(String mappingResource, String dataResource) {
        if (!mappingResource.startsWith("/")) {
            mappingResource = "/" + mappingResource;
        }
        if (!dataResource.startsWith("/")) {
            dataResource = "/" + dataResource;
        }
        
        try {
            Mapping mapping = new Mapping();
            InputSource in = new InputSource(CastorUtil.class.getResourceAsStream(mappingResource));
            try {
                mapping.loadMapping(in);
            }
            finally {
                in.getByteStream().close();
            }
            
            Unmarshaller unmarshaller = new Unmarshaller(mapping);
            Reader reader = new BufferedReader(new InputStreamReader(CastorUtil.class.getResourceAsStream(dataResource)));
            try {
                return unmarshaller.unmarshal(reader);
            }
            finally {
                reader.close();
            }
        }
        catch (Exception ex) {
            String msg = "Error to unmarshal from xml ["
                + "mappingResource: " + mappingResource
                + ", dataResource: " + dataResource + "]";
            throw new CastorException(msg, ex);
        }
    }
}
