package com.hzjbbis.fas.protocol.zj.viewer;
/**
 * @filename	FrameC04.java
 * @auther 		yangdh
 * @date		2006-8-2 15:10:24
 * @version		1.0
 * TODO
 */
public class FrameC04 extends AbstractFrame{
	public static final String FUNC_NAME="�ն˱����־";
	public FrameC04(){
		//
	}
	
	public FrameC04(byte[] frame){
		super(frame);
	}
	
	public FrameC04(String data){
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
