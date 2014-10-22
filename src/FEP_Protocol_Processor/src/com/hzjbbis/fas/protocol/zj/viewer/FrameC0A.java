package com.hzjbbis.fas.protocol.zj.viewer;
/**
 * @filename	FrameC0A.java
 * @auther 		yangdh
 * @date		2006-8-2 15:21:15
 * @version		1.0
 * TODO
 */
public class FrameC0A extends AbstractFrame{
	public static final String FUNC_NAME="�澯ȷ��";
	public FrameC0A(){
		//
	}
	
	public FrameC0A(byte[] frame){
		super(frame);
	}
	
	public FrameC0A(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("��������--").append(FUNC_NAME);
			sb.append("\n");
			sb.append("����--").append(Util.BytesToHex(frame,11,length));			
			return sb.toString();
		}
		return null;
	}
}
