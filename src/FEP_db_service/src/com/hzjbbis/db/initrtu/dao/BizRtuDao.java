/**
 * 终端RTU的初始化加载、终端的测量点、任务参数加载
 */
package com.hzjbbis.db.initrtu.dao;

import java.util.List;

import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuAlertCode;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.SysConfig;
import com.hzjbbis.fk.model.TaskDbConfig;
import com.hzjbbis.fk.model.TaskTemplate;

/**
 * @author bhw
 *
 */
public interface BizRtuDao {
	/**
	 * 加载业务处理器使用的终端列表。这些终端，只加载基本属性。对于集合类属性，另外加载，然后组装。
	 * @return
	 */
	List<BizRtu> loadBizRtu();
	
	/**
	 * 加载终端任务列表。
	 * Use Case: 初始化Service调用loadRtuTask(),查找BizRtu，如果tasklist 为null，
	 * 			创建list，添加到list中。
	 * @return
	 */
	List<RtuTask> loadRtuTasks();
	
	/**
	 * 加载终端的测量点列表。
	 * @return
	 */
	List<MeasuredPoint> loadMeasuredPoints();
	
	/**
	 * 查找全部告警编码定义
	 * @return
	 */
	List<RtuAlertCode> loadRtuAlertCodes();
	
	/**
	 * 加载任务保存的数据库表配置信息。
	 * @return
	 */
	List<TaskDbConfig> loadTaskDbConfig();
	
	/**
	 * 初始化任务模板
	 * @return
	 */
	List<TaskTemplate> loadTaskTemplate();
	/**
	 * 初始化数据库表系统配置参数值
	 * @return
	 */
	List<SysConfig> loadSysConfig();
}
