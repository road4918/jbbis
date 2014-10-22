package com.hzjbbis.fas.protocol.zj.viewer;


/**
 *@filename	FrameC21.java
 *@auther	yangdh
 *@date		2006-12-26
 *@version	1.0
 *TODO
 */
public class FrameC21 extends AbstractFrame{
	public static final String FUNC_NAME="��¼";
	public FrameC21(){
		//
	}
	
	public FrameC21(byte[] frame){
		super(frame);
	}
	
	public FrameC21(String data){
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
