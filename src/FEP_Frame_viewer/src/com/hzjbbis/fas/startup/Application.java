/**
 * 浙江电力负控系统－通讯系统启动模块
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
