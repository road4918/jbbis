package com.hzjbbis.db.heartbeat;

import java.sql.SQLException;
import java.util.List;

public interface HeartBeatDao {
//	��ʼ��
	public boolean doInit(int weekNo, int weekNum) throws SQLException;
//	��ȡ��־��Ϣ
	public  List<HeartBeatLog> getLogResult(int weekNO) throws SQLException;
//	������־
	public void updateLogResult(boolean b,long curTime,int id,int nInitWeekNo) throws SQLException;
	
	public void insertLogResult(int nInitWeekNo, long curTime) throws SQLException;
	
	public int[] batchUpdate(List<HeartBeat> orHeartBeats,int columnIndex);
	public void batchInsert(List<HeartBeat> insertHeartBeats,int columnIndex);

}
