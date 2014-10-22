package com.hzjbbis.fk.gate;

import com.hzjbbis.fk.utils.ClassLoaderUtil;

public class AppInitClassLoader {
	public static void main(String[] args) {
		ClassLoaderUtil.initializeClassPath();
		Gate.main(args);
	}

}
