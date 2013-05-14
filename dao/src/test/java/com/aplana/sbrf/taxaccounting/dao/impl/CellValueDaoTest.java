package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.cell.CellValueDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CellValueDaoTest.xml"})
public class CellValueDaoTest {

	@Autowired
	private CellValueDao cellValueDao;

	@Test
	@Transactional
	public void getCellValueTest(){
		Map<Long, DataRow<Cell>> rowIdMap = new HashMap<Long, DataRow<Cell>>();

		List<Column> columns = new ArrayList<Column>();
		Column stringColumn = new StringColumn();
		stringColumn.setId(1);
		stringColumn.setAlias("alias 1");
		columns.add(stringColumn);
		Column numericColumn = new NumericColumn();
		numericColumn.setId(2);
		numericColumn.setAlias("alias 2");
		columns.add(numericColumn);
		Column dateColumn = new DateColumn();
		dateColumn.setId(3);
		dateColumn.setAlias("alias 3");
		columns.add(dateColumn);

		rowIdMap.put(1l, new DataRow<Cell>(null, FormDataUtils.createCells(columns, null)));
		rowIdMap.put(2l, new DataRow<Cell>(null, FormDataUtils.createCells(columns, null)));
		cellValueDao.fillCellValue(1l, rowIdMap);

		Assert.assertEquals("string cell", rowIdMap.get(1l).getCell("alias 1").getValue());
		Assert.assertEquals(getDate(2013, 11, 31), rowIdMap.get(1l).getCell("alias 3").getValue());
		Assert.assertEquals(null, rowIdMap.get(1l).getCell("alias 2").getValue());

		Assert.assertEquals(new BigDecimal(123), rowIdMap.get(2l).getCell("alias 2").getValue());
		Assert.assertEquals(null, rowIdMap.get(2l).getCell("alias 1").getValue());
	}

	@Test
	@Transactional
	public void saveAndGetCellValueTest(){
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

		DataRow<Cell> editCellRow = new DataRow<Cell>("alias 3", FormDataUtils.createCells(columns, null));
		editCellRow.getCell("alias 3").setValue(getDate(2013, 11, 31));
		editCellRow.getCell("alias 4").setValue("string cell");

		rowIdMap.put(3l, editCellRow);
		rowIdMap.put(4l, new DataRow<Cell>("alias 4" , FormDataUtils.createCells(columns, null)));

		cellValueDao.saveCellValue(rowIdMap);

		cellValueDao.fillCellValue(1l, rowIdMap);

		Assert.assertEquals(getDate(2013, 11, 31), rowIdMap.get(3l).getCell("alias 3").getValue());
		Assert.assertEquals("string cell", rowIdMap.get(3l).getCell("alias 4").getValue());
		Assert.assertEquals(null, rowIdMap.get(4l).getCell("alias 3").getValue());
		Assert.assertEquals(null, rowIdMap.get(4l).getCell("alias 4").getValue());
	}

	private Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
