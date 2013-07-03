package com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell;

import com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell.CellSpanInfoDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CellSpanInfoDaoTest.xml"})
public class CellSpanInfoDaoTest {

	@Autowired
	private CellSpanInfoDao cellSpanInfoDao;

	@Test
	@Transactional
	public void getCellSpanInfoTest(){
		Map<Long, DataRow<Cell>> rowIdMap = new HashMap<Long, DataRow<Cell>>();

		List<Column> columns = new ArrayList<Column>();
		for (int i = 1; i < 3; i++) {
			Column column = new StringColumn();
			column.setId(i);
			column.setAlias("alias " + i);
			columns.add(column);
		}

		rowIdMap.put(1l, new DataRow<Cell>(null, FormDataUtils.createCells(columns, null)));
		rowIdMap.put(2l, new DataRow<Cell>(null, FormDataUtils.createCells(columns, null)));
		cellSpanInfoDao.fillCellSpanInfo(1l, rowIdMap);

		Assert.assertEquals(2, rowIdMap.get(1l).getCell("alias 1").getColSpan());
		Assert.assertEquals(3, rowIdMap.get(1l).getCell("alias 1").getRowSpan());
		Assert.assertEquals(1, rowIdMap.get(1l).getCell("alias 2").getRowSpan());

		Assert.assertEquals(3, rowIdMap.get(2l).getCell("alias 1").getColSpan());
		Assert.assertEquals(2, rowIdMap.get(2l).getCell("alias 1").getRowSpan());
	}

	@Test
	@Transactional
	public void saveAndGetCellSpanInfoTest(){
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
		editCellRow.getCell("alias 3").setColSpan(3);
		editCellRow.getCell("alias 4").setRowSpan(2);

		rowIdMap.put(3l, editCellRow);
		rowIdMap.put(4l, new DataRow<Cell>("alias 4", FormDataUtils.createCells(columns, null)));

		cellSpanInfoDao.saveCellSpanInfo(rowIdMap);

		cellSpanInfoDao.fillCellSpanInfo(1l, rowIdMap);

		Assert.assertEquals(3, rowIdMap.get(3l).getCell("alias 3").getColSpan());
		Assert.assertEquals(1, rowIdMap.get(3l).getCell("alias 3").getRowSpan());
		Assert.assertEquals(2, rowIdMap.get(3l).getCell("alias 4").getRowSpan());
		Assert.assertEquals(1, rowIdMap.get(4l).getCell("alias 4").getRowSpan());
	}
}
