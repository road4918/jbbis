package com.hzjbbis.db.rtu;

import java.util.Collection;

import com.hzjbbis.fk.model.ComRtu;

public interface RtuStatusUpdateDao {
	void update(final Collection<ComRtu> rtus);
}
