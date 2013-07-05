package com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CellValueDaoTest.xml"})
public class CellValueDaoTest {

	@Autowired
	private CellValueDao cellValueDao;
	
	@Autowired 
	private DataRowDao dataRowDao;
	
	@Autowired 
	private FormDataDao formDataDao;

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

		List<DataRow<Cell>> rows = dataRowDao.getRows(formDataDao.get(1l), null, null);
		rowIdMap.clear();
		for (DataRow<Cell> dataRow : rows) {
			rowIdMap.put(dataRow.getId(), dataRow);
		}

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
