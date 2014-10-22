/**
 * 终端刷新操作数据库接口
 */
package com.hzjbbis.db.rtu;

import java.util.List;

import com.hzjbbis.fk.model.BizRtu;
import com.hzjbbis.fk.model.MeasuredPoint;
import com.hzjbbis.fk.model.RtuTask;
import com.hzjbbis.fk.model.TaskTemplate;
import com.hzjbbis.fk.model.TaskTemplateItem;

/**
 * @author bhw
 *
 */
public interface RtuRefreshDao {
	/**
	 * 根据终端局号重新加载终端对象。
	 * Use Case：主站主动通知前置机刷新终端信息。
	 * @param zdjh
	 * @return
	 */
	BizRtu getRtu(String zdjh);
	
	/**
	 * 根据终端RTUA重新加载终端对象。
	 * Use Case: 任务解析失败，根据RTUA加载对象。
	 * @param rtua
	 * @return
	 */
	BizRtu getRtu(int rtua);
	
	/**
	 * 根据终端局号加载该终端的测量点信息列表。
	 * @param zdjh：终端局号
	 * @return
	 */
	List<MeasuredPoint> getMeasurePoints(String zdjh);
	
	/**
	 * 刷新终端任务列表。
	 * @param zdjh： 终端局号。
	 * @return
	 */
	List<RtuTask> getRtuTasks(String zdjh);
	
	/**
	 * 刷新任务模板信息。
	 * Use Case：主站修改终端任务模板，在批量更新该模板的终端任务后，通知前置机刷新模板信息。
	 * @param templID
	 * @return
	 */
	TaskTemplate getTaskTemplate(String templID);
	
	/**
	 * 刷新某个任务模板的所有参数项。
	 * Use Case: 主站刷新任务模板后，需要更新该模板的参数项列表。
	 * @param templID
	 * @return
	 */
	List<TaskTemplateItem> getTaskTemplateItems(String templID);
	
	
}
