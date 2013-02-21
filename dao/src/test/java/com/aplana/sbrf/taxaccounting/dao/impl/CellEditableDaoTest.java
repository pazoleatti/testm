package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.CellEditableDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"CellEditableDaoTest.xml"})
public class CellEditableDaoTest {

	@Autowired
	private CellEditableDao cellEditableDao;

	@Autowired
	private FormDataDao formDataDao;

	@Test
	@Transactional
	public void getCellEditableTest(){
		List<CellEditable> records = cellEditableDao.getEditableCells(Long.valueOf(1));

		Assert.assertEquals(3, records.size());
		Assert.assertEquals(Long.valueOf(1), records.get(0).getRowId());
		Assert.assertEquals(Integer.valueOf(1), records.get(0).getColumnId());
		Assert.assertEquals(Long.valueOf(2), records.get(2).getRowId());
		Assert.assertEquals(Integer.valueOf(1), records.get(1).getColumnId());
		Assert.assertEquals(Long.valueOf(2), records.get(2).getRowId());
		Assert.assertEquals(Integer.valueOf(2), records.get(2).getColumnId());
	}

	@Test
	@Transactional
	public void getNotExistedFormCellEditableTest(){
		List<CellEditable> records = cellEditableDao.getEditableCells(Long.valueOf(-1));
		Assert.assertEquals(0, records.size());
	}

	@Test
	@Transactional
	public void saveEditableCells(){
		List<CellEditable> edits =new ArrayList<CellEditable>();
		edits.add(new CellEditable(1l, 2));
		edits.add(new CellEditable(2l, 3));

		cellEditableDao.saveEditableCells(edits);
		List<CellEditable> records = cellEditableDao.getEditableCells(Long.valueOf(1));

		Assert.assertEquals(5, records.size());

		Assert.assertEquals(Long.valueOf(1), records.get(0).getRowId());
		Assert.assertEquals(Integer.valueOf(1), records.get(0).getColumnId());

		Assert.assertEquals(Long.valueOf(1), records.get(1).getRowId());
		Assert.assertEquals(Integer.valueOf(2), records.get(1).getColumnId());

		Assert.assertEquals(Long.valueOf(2), records.get(2).getRowId());
		Assert.assertEquals(Integer.valueOf(1), records.get(2).getColumnId());

		Assert.assertEquals(Long.valueOf(2), records.get(3).getRowId());
		Assert.assertEquals(Integer.valueOf(2), records.get(3).getColumnId());

		Assert.assertEquals(Long.valueOf(2), records.get(4).getRowId());
		Assert.assertEquals(Integer.valueOf(3), records.get(4).getColumnId());
	}
}
