package com.hzjbbis.fas.protocol.zj.viewer;
/**
 * @filename	FrameC09.java
 * @auther 		yangdh
 * @date		2006-8-2 15:14:27
 * @version		1.0
 * TODO
 */
public class FrameC09 extends AbstractFrame{
	public static final String FUNC_NAME="�쳣�澯";
	public FrameC09(){
		//
	}
	
	public FrameC09(byte[] frame){
		super(frame);
	}
	
	public FrameC09(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("��������--").append(FUNC_NAME);
			sb.append("\n");
			if(direction>0){	//�ն�Ӧ��
				descRtuReply(sb);
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			if(fexp>0){				
				buffer.append("�쳣Ӧ��--").append(errCode(frame[11]));				
			}else{				
				buffer.append("�������͸澯����--").append(frame[11] & 0xFF).append("\n");
				buffer.append("�澯�����Ĳ������--").append(frame[12] & 0xFF).append("    ");
				buffer.append("�澯����ʱ��---");
				buffer.append("20").append(Util.ByteToHex(frame[13])).append("-").append(Util.ByteToHex(frame[14]))
						.append("-").append(Util.ByteToHex(frame[15])).append(" ").append(Util.ByteToHex(frame[16]))
						.append(":").append(Util.ByteToHex(frame[17])).append(":00    ");
				buffer.append("�澯����--").append(Util.ByteToHex(frame[19])).append(Util.ByteToHex(frame[18])).append("\n");				
				buffer.append("�澯����--").append(Util.BytesToHex(frame,20,length-9));
			}
		}catch(Exception e){
			//
		}
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("�澯�����Ĳ������--").append(Util.ByteToHex(frame[11])).append("    ");
			buffer.append("�澯����--").append(Util.ByteToHex(frame[13])).append(Util.ByteToHex(frame[12])).append("    ");
			buffer.append("�澯����ʱ��---");
			buffer.append("20").append(Util.ByteToHex(frame[14])).append("-").append(Util.ByteToHex(frame[15]))
					.append("-").append(Util.ByteToHex(frame[16])).append(" ").append(Util.ByteToHex(frame[17]))
					.append(":").append(Util.ByteToHex(frame[18])).append(":00\n");
			buffer.append("�����ٲ�澯����--").append(frame[19] & 0xFF).append("\n");
		}catch(Exception e){
			//
		}
	}
}
