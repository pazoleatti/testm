package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.dao.cell.CellStyleDao;
import com.aplana.sbrf.taxaccounting.model.*;
import junit.framework.Assert;
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
@ContextConfiguration({"CellStyleDaoTest.xml"})
public class CellStyleDaoTest {

	@Autowired
	private CellStyleDao cellStyleDao;

	@Autowired
	protected FormStyleDao formStyleDao;

	@Test
	@Transactional
	public void getCellStyleTest(){
		Map<Long, DataRow> rowIdMap = new HashMap<Long, DataRow>();

		List<Column> columns = new ArrayList<Column>();
		for (int i = 1; i < 3; i++) {
			Column column = new StringColumn();
			column.setId(i);
			column.setAlias("alias " + i);
			columns.add(column);
		}

		List<FormStyle> styles = formStyleDao.getFormStyles(1);
		rowIdMap.put(1l, new DataRow(null ,columns, styles));
		rowIdMap.put(2l, new DataRow(null ,columns, styles));
		cellStyleDao.fillCellStyle(1l, rowIdMap, styles);

		Assert.assertEquals("alias 1", rowIdMap.get(1l).getCell("alias 1").getStyle().getAlias());
		Assert.assertEquals(Color.BLUE, rowIdMap.get(1l).getCell("alias 1").getStyle().getFontColor());
		Assert.assertEquals(Color.GREEN, rowIdMap.get(1l).getCell("alias 1").getStyle().getBackColor());
		Assert.assertEquals(true, rowIdMap.get(1l).getCell("alias 1").getStyle().isItalic());
		Assert.assertEquals(false, rowIdMap.get(1l).getCell("alias 1").getStyle().isBold());
		Assert.assertEquals(null, rowIdMap.get(1l).getCell("alias 2").getStyle());

		Assert.assertEquals("alias 2", rowIdMap.get(2l).getCell("alias 1").getStyle().getAlias());
		Assert.assertEquals(null, rowIdMap.get(2l).getCell("alias 2").getStyle());
	}

	@Test
	@Transactional
	public void saveAndGetCellStyleTest(){
		Map<Long, DataRow> rowIdMap = new HashMap<Long, DataRow>();
		List<FormStyle> styles = formStyleDao.getFormStyles(1);

		List<Column> columns = new ArrayList<Column>();
		Column dateColumn = new DateColumn();
		dateColumn.setId(3);
		dateColumn.setAlias("alias 3");
		columns.add(dateColumn);
		Column stringColumn = new StringColumn();
		stringColumn.setId(4);
		stringColumn.setAlias("alias 4");
		columns.add(stringColumn);

		DataRow editCellRow = new DataRow("alias 3" ,columns, styles);
		editCellRow.getCell("alias 3").setStyleAlias("alias 1");
		editCellRow.getCell("alias 4").setStyleAlias("alias 3");

		rowIdMap.put(3l, editCellRow);
		rowIdMap.put(4l, new DataRow("alias 4" ,columns, styles));

		cellStyleDao.saveCellStyle(rowIdMap);

		cellStyleDao.fillCellStyle(1l, rowIdMap, styles);

		Assert.assertEquals("alias 1", rowIdMap.get(3l).getCell("alias 3").getStyle().getAlias());
		Assert.assertEquals(Color.BLUE, rowIdMap.get(3l).getCell("alias 3").getStyle().getFontColor());
		Assert.assertEquals(Color.GREEN, rowIdMap.get(3l).getCell("alias 3").getStyle().getBackColor());
		Assert.assertEquals(true, rowIdMap.get(3l).getCell("alias 3").getStyle().isItalic());
		Assert.assertEquals(false, rowIdMap.get(3l).getCell("alias 3").getStyle().isBold());
		Assert.assertEquals("alias 3", rowIdMap.get(3l).getCell("alias 4").getStyle().getAlias());

		Assert.assertEquals(null, rowIdMap.get(4l).getCell("alias 3").getStyle());
		Assert.assertEquals(null, rowIdMap.get(4l).getCell("alias 4").getStyle());
	}
}
