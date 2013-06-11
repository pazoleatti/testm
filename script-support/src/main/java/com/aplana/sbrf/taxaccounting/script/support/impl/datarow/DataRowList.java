package com.aplana.sbrf.taxaccounting.script.support.impl.datarow;

import java.util.AbstractList;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

/**
 * TODO: [sgoryachkin] Эта реализация List через AbstractList в корне не верна. 
 * Она скорее всего будет работать очень медленно
 * Нужно правильно реализовать List реализуя собственный итератор.
 * 
 * 
 * @author sgoryachkin
 *
 */
public class DataRowList extends AbstractList<DataRow<Cell>> {
	
	private DataRowDao dao;
	private FormData fd;
	private DataRowRange range;
	private DataRowFilter filter;
	private Logger log;
	
	
	public DataRowList(DataRowDao dao, FormData fd, DataRowRange range,
			DataRowFilter filter, Logger log) {
		super();
		this.dao = dao;
		this.fd = fd;
		this.range = range;
		this.filter = filter;
		this.log = log;
	}
	

	@Override
	public DataRow<Cell> get(int index) {
		return dao.getRow(fd, index, filter, range);
	}

	@Override
	public int size() {
		return dao.getSize(fd, filter, range);
	}


}
