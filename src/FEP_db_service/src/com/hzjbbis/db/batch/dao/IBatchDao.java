/**
 * ֧���������DAO
 */
package com.hzjbbis.db.batch.dao;


/**
 * @author bhw
 *
 */
public interface IBatchDao{
	/**
	 * �����õ�SQL���磺
	 * update t_actor set first_name = :firstName, last_name = :lastName where id = :id
	 * update t_actor set first_name = ?, last_name = ? where id = ?
	 * @param sql
	 */
	void setSql(String sql);
	
	/**
	 * ������������dao��˵��updateʧ�ܣ���Ҫִ�б���sql����insert��
	 * @param sqlAlt
	 */
	void setSqlAlt(String sqlAlt);
	
	/**
	 * �����������棬��һ���������º󣬿�����Ҫִ��һ�丽�ӵ�SQL����merge��䡣
	 * @param aSql
	 */
	void setAdditiveSql(String aSql );
	
	void setAdditiveParameter(Object additiveParameter);
	
	/**
	 * �� POJO ���� null����ʾ������������ʱ���Ƿ񵽴�delay��ʱ�䡣
	 * ���ȴ�������Ķ������ˣ����ܼ�����Dao���Ӷ���return false.
	 * @param pojo
	 */
	boolean add(Object pojo);
	boolean add(Object[] param);
	/**
	 * ��������Ӧ��KEY��
	 * ����͸澯����Ҫ���յ�λ������зֱ��档����ڷŵ�Daoʱ����Ҫ����key��ѡ����ʵ�DAO��
	 * @return
	 */
	int getKey();
	
	/**
	 * ��������Ľ���������ʵ�����д��������������̳߳����Զ�ִ�У�����Ҫ����ֵ��
	 */
	void batchUpdate();
	
	/**
	 * ���ص�ǰDAO����ȴ���������Ķ�������
	 * @return
	 */
	int size();
	
	/**
	 * �����������µ����ֵ��
	 * @param batchSize
	 */
	void setBatchSize(int batchSize);
	
	/**
	 * �������һ�����������ִ��ʱ�䡣
	 * @return
	 */
	long getLastIoTime();
	
	/**
	 * �������¶���ʱ�����ܲ���������������ô�������ζ��
	 * ��������ӳ�ʱ�䣬��������ҲҪ���档
	 * @param delaySec
	 */
	void setDelaySecond(int delaySec);
	
	long getDelayMilliSeconds();
	
	/**
	 * ���delay����ڣ��Ƿ���������Ҫ���档
	 */
	boolean hasDelayData();
}
