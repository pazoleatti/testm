package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormTemplateDaoTest.xml"})
public class ColumnDaoTest {

	@Autowired
	private ColumnDao columnDao;

	private static final int FORM_ID_FOR_TEST = 1;
	private static final int NUMBER_OF_COLUMNS = 3;
	private static final int FIST_COLUMN  = 0;
	private static final int SECOND_COLUMN = 1;
	private static final int THIRD_COLUMN  = 2;

	@Test
	public void getFormColumns(){
		//Given FORM_ID_FOR_TEST, NUMBER_OF_STYLES

		//When
		List<Column> listOfColumnsInDb = columnDao.getFormColumns(FORM_ID_FOR_TEST);

		//Then
		Assert.assertEquals(NUMBER_OF_COLUMNS, listOfColumnsInDb.size());

		Assert.assertEquals(Integer.valueOf(1), listOfColumnsInDb.get(FIST_COLUMN).getId());
		Assert.assertEquals("Строковый столбец", listOfColumnsInDb.get(FIST_COLUMN).getName());
		Assert.assertEquals(1, listOfColumnsInDb.get(FIST_COLUMN).getOrder());
		Assert.assertEquals("stringColumn", listOfColumnsInDb.get(FIST_COLUMN).getAlias());
		Assert.assertEquals(500, ((StringColumn)listOfColumnsInDb.get(FIST_COLUMN)).getMaxLength());

		Assert.assertEquals(Integer.valueOf(2), listOfColumnsInDb.get(SECOND_COLUMN).getId());
		Assert.assertEquals("Числовой столбец", listOfColumnsInDb.get(SECOND_COLUMN).getName());
		Assert.assertEquals(2, listOfColumnsInDb.get(SECOND_COLUMN).getOrder());
		Assert.assertEquals("numericColumn", listOfColumnsInDb.get(SECOND_COLUMN).getAlias());

		Assert.assertEquals(Integer.valueOf(3), listOfColumnsInDb.get(THIRD_COLUMN).getId());
		Assert.assertEquals("Дата-столбец", listOfColumnsInDb.get(THIRD_COLUMN).getName());
		Assert.assertEquals(3, listOfColumnsInDb.get(THIRD_COLUMN).getOrder());
		Assert.assertEquals("dateColumn", listOfColumnsInDb.get(THIRD_COLUMN).getAlias());
	}

	@Test
	public void saveFormColumns(){
		//TODO: переделать тест, чтобы не падал! Займусь после того как сделаю:
		// SBRFACCTAX-963	SBRFACCTAX-881 Реализовать поддержку стилей на уровне DAO для данных формы (CELL_STYLE)
		// SBRFACCTAX-962	SBRFACCTAX-911 Добавить в StringColumn поле maxLength
	/*
		//Given FORM_ID_FOR_TEST, listOfStylesInDb
		List<Column> columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);
		//Создадим один новый стиль, которого нету в БД
		StringColumn newColumn = new StringColumn();
		newColumn.setAlias("newColumn");
		newColumn.setName("Новый столбец");
		newColumn.setOrder(4);
		newColumn.setMaxLength(10);
		columnList.add(newColumn);

		FormTemplate formTemplate = new FormTemplate();
		formTemplate.setId(FORM_ID_FOR_TEST);
		formTemplate.getColumns().addAll(columnList);

		//When
		columnDao.saveFormColumns(formTemplate);

		//Then
		columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);

		newColumn = (StringColumn) columnList.get(3);

		Assert.assertEquals(4, columnList.size());
		Assert.assertEquals("newColumn", newColumn.getAlias());
		Assert.assertEquals("Новый столбец", newColumn.getName());
		Assert.assertEquals(4, newColumn.getOrder());
	*/
	}
}
