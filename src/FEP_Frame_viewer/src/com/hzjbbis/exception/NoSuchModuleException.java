package com.hzjbbis.exception;

public class NoSuchModuleException extends Exception {
	private static final long serialVersionUID = 200603141603L;
	public NoSuchModuleException(String mid){
		super("模块id=["+mid+"]没有定义");
	}
}
