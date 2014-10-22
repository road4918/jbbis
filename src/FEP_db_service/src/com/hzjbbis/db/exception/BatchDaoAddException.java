/**
 * Throws if BatchDao's queue is full. No object added to BatchDao.
 * Caller should put back object for next time. 
 */
package com.hzjbbis.db.exception;

/**
 * @author bhw
 *
 */
public class BatchDaoAddException extends Exception {
	private static final long serialVersionUID = -3257157300332484834L;

}
