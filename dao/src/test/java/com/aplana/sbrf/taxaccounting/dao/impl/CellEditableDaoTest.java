package com.aplana.sbrf.taxaccounting.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.cell.CellEditableDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CellEditableDaoTest.xml"})
public class CellEditableDaoTest {

	@Autowired
	private CellEditableDao cellEditableDao;

	@Test
	@Transactional
	public void getCellEditableTest(){
		Map<Long, DataRow<Cell>> rowIdMap = new HashMap<Long, DataRow<Cell>>();

		List<Column> columns = new ArrayList<Column>();
		for (int i = 1; i < 3; i++) {
			Column column = new StringColumn();
			column.setId(i);
			column.setAlias("alias " + i);
			columns.add(column);
		}

		rowIdMap.put(1l, new DataRow<Cell>(null ,FormDataUtils.createCells(columns, null)));
		rowIdMap.put(2l, new DataRow<Cell>(null ,FormDataUtils.createCells(columns, null)));

		cellEditableDao.fillCellEditable(1l, rowIdMap);

		Assert.assertEquals(true, rowIdMap.get(1l).getCell("alias 1").isEditable());
		Assert.assertEquals(false, rowIdMap.get(1l).getCell("alias 2").isEditable());
		Assert.assertEquals(true, rowIdMap.get(2l).getCell("alias 1").isEditable());
		Assert.assertEquals(true, rowIdMap.get(2l).getCell("alias 2").isEditable());
	}

	@Test
	@Transactional
	public void saveAndGetCellEditableTest(){
		Map<Long, DataRow<Cell>> rowIdMap = new HashMap<Long, DataRow<Cell>>();

		List<Column> columns = new ArrayList<Column>();
		Column dateColumn = new DateColumn();
		dateColumn.setId(3);
		dateColumn.setAlias("alias 3");
		columns.add(dateColumn);
		Column stringColumn = new StringColumn();
		stringColumn.setId(4);
		stringColumn.setAlias("alias 4");
		columns.add(stringColumn);

		DataRow<Cell> editCellRow = new DataRow<Cell>("alias 3" ,FormDataUtils.createCells(columns, null));
		editCellRow.getCell("alias 3").setEditable(true);
		editCellRow.getCell("alias 4").setEditable(true);

		rowIdMap.put(3l, editCellRow);
		rowIdMap.put(4l, new DataRow<Cell>("alias 4" ,FormDataUtils.createCells(columns, null)));

		cellEditableDao.saveCellEditable(rowIdMap);

		cellEditableDao.fillCellEditable(1l, rowIdMap);

		Assert.assertEquals(true, rowIdMap.get(3l).getCell("alias " + 3).isEditable());
		Assert.assertEquals(true, rowIdMap.get(3l).getCell("alias " + 4).isEditable());
		Assert.assertEquals(false, rowIdMap.get(4l).getCell("alias " + 3).isEditable());
		Assert.assertEquals(false, rowIdMap.get(4l).getCell("alias " + 4).isEditable());
	}
}
