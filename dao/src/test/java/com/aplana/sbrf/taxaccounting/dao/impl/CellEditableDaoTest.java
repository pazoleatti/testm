package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CellEditableDao;
import com.aplana.sbrf.taxaccounting.model.*;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CellEditableDaoTest.xml"})
public class CellEditableDaoTest {

	@Autowired
	private CellEditableDao cellEditableDao;

	@Test
	@Transactional
	public void getCellEditableTest(){
		Map<Long, DataRow> rowIdMap = new HashMap<Long, DataRow>();

		List<Column> columns = new ArrayList<Column>();
		for (int i = 1; i < 3; i++) {
			Column column = new StringColumn();
			column.setId(i);
			column.setAlias("alias " + i);
			columns.add(column);
			for (long l = 1; l < 3; l ++) {
				DataRow row = new DataRow("" + l ,columns, null);
				rowIdMap.put(l, row);
			}
		}

		cellEditableDao.fillCellEditable(1l, rowIdMap);

		Assert.assertEquals(true, rowIdMap.get(1l).getCell("alias " + 1).isEditable());
		Assert.assertEquals(false, rowIdMap.get(1l).getCell("alias " + 2).isEditable());
		Assert.assertEquals(true, rowIdMap.get(2l).getCell("alias " + 1).isEditable());
		Assert.assertEquals(true, rowIdMap.get(2l).getCell("alias " + 2).isEditable());

		rowIdMap.get(1l).getCell("alias " + 1).setEditable(false);
		rowIdMap.get(1l).getCell("alias " + 2).setEditable(true);
		rowIdMap.get(2l).getCell("alias " + 1).setEditable(false);
	}
}
