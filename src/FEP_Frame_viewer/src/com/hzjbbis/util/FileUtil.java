package com.hzjbbis.util;

import java.io.File;
import java.io.IOException;

/**
 * �ļ�����������
 * @author ������
 */
public class FileUtil {

    /**
     * ����Ŀ¼�������Ŀ¼�����ڣ����������и�Ŀ¼
     * @param path ·����
     * @return Ŀ¼����
     */
    public static File mkdirs(String path) {
        File dir = new File(path);
        if (dir.isFile()) {
            throw new IllegalArgumentException(path + " is not a directory");
        }
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return dir;
    }
    
    /**
     * ���ļ�������ļ������ڣ��򴴽�֮
     * @param path �ļ�����Ŀ¼
     * @param fileName �ļ���
     * @return �ļ�����
     */
    public static File openFile(String path, String fileName) {
        File dir = mkdirs(path);
        File file = new File(dir, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException ex) {
                throw new RuntimeException("Error to open file: " + fileName, ex);
            }
        }
        
        return file;
    }
    
    /**
     * ɾ���ļ�
     * @param path �ļ�����·��
     * @param fileName �ļ���
     */
    public static void deleteFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    /**
     * ȡ��Ŀ¼�ľ���·��������������·�������·��������û��ĵ�ǰĿ¼��Ϊ�丸Ŀ¼ 
     * @param path ·�����������Ǿ���·�������·��
     * @return ����·����
     */
    public static String getAbsolutePath(String path) {
        File f = new File(path);
        return f.getAbsolutePath();
    }
    
    /**
     * ȡ���ļ��ľ���·����
     * @param path �ļ��Ĵ��·��
     * @param fileName �ļ���
     * @return �ļ��ľ���·����
     */
    public static String getAbsolutePath(String path, String fileName) {
        File dir = mkdirs(getAbsolutePath(path));
        File file = new File(dir, fileName);
        return file.getAbsolutePath();
    }
}
