/**
 * �㽭��������ϵͳ��ͨѶϵͳ����ģ��
 */
package com.hzjbbis.fas.startup;


/**
 * @author bhw
 *
 */
public class Application {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		initialize();
	}
	
	public static void initialize(){
		ClassLoaderUtil.initializeClassPath();
	}
	
}
