package com.hzjbbis.fas.protocol.zj.viewer;


/**
 *@filename	FrameC24.java
 *@auther	yangdh
 *@date		2007-1-9
 *@version	1.0
 *TODO
 */
public class FrameC24 extends AbstractFrame{
	public static final String FUNC_NAME="����";
	
	public FrameC24(){
		//
	}
	
	public FrameC24(byte[] frame){
		super(frame);
	}
	
	public FrameC24(String data){
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
