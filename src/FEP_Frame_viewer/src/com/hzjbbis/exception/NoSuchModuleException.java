package com.hzjbbis.exception;

public class NoSuchModuleException extends Exception {
	private static final long serialVersionUID = 200603141603L;
	public NoSuchModuleException(String mid){
		super("ģ��id=["+mid+"]û�ж���");
	}
}
