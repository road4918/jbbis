package com.hzjbbis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

/**
 * ���������ļ��������б��������ļ��м��������б��Ȱ�����·�������ļ�������ļ�
 * �����ڣ����������·���в�����Դ�ļ���������մ���Դ�ļ��м��������б���������
 * ��Ϊֻ����������ֵ����д�ص��ļ��С����������б�Ϊ��д�ģ���������ֵʱ����ͬʱ
 * д�������ļ���
 * @author ������
 */
public class FileBasedProperties extends Properties {

    private static final long serialVersionUID = 3788399848195322525L;
    
    /** �����ļ� */
    private String filepath;
    /** �Ƿ�ֻ�� */
    private boolean readOnly;
    
    /**
     * ����һ�������б�
     * @param file �����ļ�
     */
    public FileBasedProperties(String file) {
        super();
        filepath = getAbsolutePath(file);
        if( null == filepath )
        	throw new RuntimeException("�����ļ������ڣ�"+file);
        loadFromFile();
    }
    
    private String getAbsolutePath(String name){
    	File file = new File(name);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	int index = name.lastIndexOf(File.separator);
    	if( index>=0 )
    		name = name.substring(index,name.length());
    	else
    		name = File.separator + name;
    	
    	//�ӵ�ǰ����Ŀ¼����config/cfg/configurationĿ¼�µ������ļ�
    	String workDir = System.getProperty("user.dir");
    	String cfgpath = workDir + File.separator + "config" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = workDir + File.separator + "configuration" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = workDir + File.separator + "cfg" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	
    	//�ӵ�ǰ��ĸ�Ŀ¼����
    	String classRoot = PathUtil.getRootPath(FileBasedProperties.class);
    	String path = classRoot + name;
    	file = new File(path);
    	if( file.exists())
    		return file.getAbsolutePath();
    	file = new File(classRoot);
    	
    	//��jar�ĸ�Ŀ¼�ϼ�Ŀ¼
    	classRoot = file.getParentFile().getAbsolutePath();
    	path = classRoot + name;
    	file = new File(path);
    	if( file.exists())
    		return file.getAbsolutePath();
    
    	//�ϼ�Ŀ¼��������Ŀ¼
    	cfgpath = classRoot + File.separator + "config" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = classRoot + File.separator + "configuration" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = classRoot + File.separator + "cfg" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
   		return null;
    }

    /**
     * ����һ�������б�
     * @param file �����ļ�
     * @param defaults ����ȱʡֵ
     */
    public FileBasedProperties(String file, Properties defaults) {
        super(defaults);
        filepath = getAbsolutePath(file);
        if( null == filepath )
        	throw new RuntimeException("�����ļ������ڣ�"+file);
        loadFromFile();
    }

    /**
     * �ж������ļ��Ƿ���ֻ����
     * @return Returns true - ֻ����false - ��д.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * ��ȡ������������ֵ
     * @param key ������
     * @param defaultValue ȱʡֵ
     * @return �������͵�����ֵ�����δָ������ֵ���򷵻�ȱʡֵ
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        value = value.trim();
        if (value.length() == 0) {
            return defaultValue;
        }
        
        return Boolean.valueOf(value).booleanValue();
    }
    
    /**
     * ��ȡ��������ֵ
     * @param key ������
     * @param defaultValue ȱʡֵ
     * @return ���ε�����ֵ�����δָ������ֵ���򷵻�ȱʡֵ
     */
    public int getInt(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        value = value.trim();
        if (value.length() == 0) {
            return defaultValue;
        }
        
        return Integer.parseInt(value);
    }

    /**
     * ��ȡ�������͵�����ֵ
     * @param key ������
     * @return �������͵�����ֵ�����δָ������ֵ���򷵻� null
     */
    public Calendar getDate(String key) {
        String value = getProperty(key);
        return CalendarUtil.parse(value);
    }

    /**
     * ��ȡ�������͵�����ֵ
     * @param key ������
     * @param defaultValue ȱʡֵ
     * @return �������͵�����ֵ�����δָ������ֵ���򷵻�ȱʡֵ
     */
    public Calendar getDate(String key, Calendar defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return CalendarUtil.parse(value, defaultValue);
    }
    
    /**
     * ��ȡ��ʾ�ļ��򻺳�����С�����ԡ�����ֵ���Բ�ָ����λ��Ҳ����ָ����λ��KB/K��MB/M �� GB/G��
     * @param key ������
     * @param defaultValue ȱʡֵ
     * @return �����ε�����ֵ�����δָ������ֵ���򷵻�ȱʡֵ
     */
    public long getSize(String key, long defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }        
        
        String s = value.trim().toUpperCase();
        if (s.length() == 0) {
            return defaultValue;
        }
        
        long multiplier = 1;
        int index;

        if ((index = s.indexOf("KB")) != -1 || (index = s.indexOf("K")) != -1) {
            multiplier = 1024;
            s = s.substring(0, index);
        }
        else if ((index = s.indexOf("MB")) != -1 || (index = s.indexOf("M")) != -1) {
            multiplier = 1024 * 1024;
            s = s.substring(0, index);
        }
        else if ((index = s.indexOf("GB")) != -1 || (index = s.indexOf("G")) != -1) {
            multiplier = 1024 * 1024 * 1024;
            s = s.substring(0, index);
        }
        
        return Long.parseLong(s) * multiplier;
    }
    
    /* (non-Javadoc)
     * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
     */
    public synchronized Object setProperty(String key, String value) {
        Object oldValue = super.setProperty(key, value);
        if (!readOnly) {
            OutputStream out = null;
            try {
                File f = new File(filepath);
                if (!f.exists()) {
                    f.createNewFile();
                }
                out = new FileOutputStream(f);
                super.store(out, null);
            }
            catch (FileNotFoundException ex) {
                throw new RuntimeException("Can't store property: " + key, ex);
            }
            catch (IOException ex) {
                throw new RuntimeException("Can't store property: " + key, ex);
            }
            finally {
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException e) {
                        // ����
                    }
                }
            }
        }
        
        return oldValue;
    }

    /**
     * �������ļ��м��������б�
     * @param file �����ļ�
     */
    public void loadFromFile() {
        File f = new File(filepath);
        InputStream in = null;
        if (f.exists()) {
            readOnly = false;
            try {
                in = new FileInputStream(f);
            }
            catch (FileNotFoundException e) {
                // ����
            }
        }
        else {
            in = FileBasedProperties.class.getResourceAsStream(filepath);
            if (in != null) {
                readOnly = true;
            }
        }
        
        if (in != null) {
            try {
                super.load(in);
            }
            catch (IOException ex) {
                throw new RuntimeException("Can't load properties from file: " + filepath, ex);
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException ex) {
                    // ����
                }
            }
        }
    }
}
