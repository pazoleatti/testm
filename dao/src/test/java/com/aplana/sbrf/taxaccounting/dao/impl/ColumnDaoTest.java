package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.test.BDUtilsMock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ColumnDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ColumnDaoTest {

    @Autowired
    private ColumnDao columnDao;

    private static final int FORM_ID_FOR_TEST = 1;
    private static final int NUMBER_OF_COLUMNS = 4;
    private static final int FIRST_COLUMN = 0;
    private static final int SECOND_COLUMN = 1;
    private static final int THIRD_COLUMN = 2;
    private static final int FOURTH_COLUMN = 3;

    @Before
    public void init() {
        ReflectionTestUtils.setField(columnDao, "bdUtils", BDUtilsMock.getBDUtils());
    }

    @Test
    public void getFormColumnsTest() {
        List<Column> listOfColumnsInDb = columnDao.getFormColumns(FORM_ID_FOR_TEST);
        assertEquals(NUMBER_OF_COLUMNS, listOfColumnsInDb.size());

        assertEquals(Integer.valueOf(1), listOfColumnsInDb.get(FIRST_COLUMN).getId());
        assertEquals("Строковый столбец", listOfColumnsInDb.get(FIRST_COLUMN).getName());
        assertEquals(1, listOfColumnsInDb.get(FIRST_COLUMN).getOrder());
        assertEquals("stringColumn", listOfColumnsInDb.get(FIRST_COLUMN).getAlias());
        assertEquals(500, ((StringColumn) listOfColumnsInDb.get(FIRST_COLUMN)).getMaxLength());
        assertEquals(true, listOfColumnsInDb.get(FIRST_COLUMN).isChecking());

        assertEquals(Integer.valueOf(2), listOfColumnsInDb.get(SECOND_COLUMN).getId());
        assertEquals("Числовой столбец", listOfColumnsInDb.get(SECOND_COLUMN).getName());
        assertEquals(2, listOfColumnsInDb.get(SECOND_COLUMN).getOrder());
        assertEquals("numericColumn", listOfColumnsInDb.get(SECOND_COLUMN).getAlias());
        assertEquals(false, listOfColumnsInDb.get(SECOND_COLUMN).isChecking());

        assertEquals(Integer.valueOf(3), listOfColumnsInDb.get(THIRD_COLUMN).getId());
        assertEquals("Дата-столбец", listOfColumnsInDb.get(THIRD_COLUMN).getName());
        assertEquals(3, listOfColumnsInDb.get(THIRD_COLUMN).getOrder());
		assertEquals(2, listOfColumnsInDb.get(THIRD_COLUMN).getDataOrder().intValue());
		assertEquals("dateColumn", listOfColumnsInDb.get(THIRD_COLUMN).getAlias());
        assertEquals(false, listOfColumnsInDb.get(SECOND_COLUMN).isChecking());

        assertEquals(Integer.valueOf(4), listOfColumnsInDb.get(FOURTH_COLUMN).getId());
        assertEquals("Автонумеруемая графа", listOfColumnsInDb.get(FOURTH_COLUMN).getName());
        assertEquals(4, listOfColumnsInDb.get(FOURTH_COLUMN).getOrder());
        assertEquals("autoNumerationColumn", listOfColumnsInDb.get(FOURTH_COLUMN).getAlias());
        assertEquals(false, listOfColumnsInDb.get(FOURTH_COLUMN).isChecking());
        if (ColumnType.AUTO.equals(listOfColumnsInDb.get(FOURTH_COLUMN).getColumnType())) {
            assertEquals(1, ((AutoNumerationColumn)listOfColumnsInDb.get(FOURTH_COLUMN)).getNumerationType().getId());
        }
    }

    @Test
	public void calculateNewDataOrderTest() {
		List<Column> oldColumns = new ArrayList<Column>();
		for (int i = 0; i < 5; i++) {
			Column column = new StringColumn();
			column.setDataOrder(i);
			oldColumns.add(column);
		}
		List<Column> newColumns = new ArrayList<Column>();
		for (int i = 0; i < 3; i++) {
			newColumns.add(new StringColumn());
		}
		// 1 случай
		ColumnDaoImpl.calculateNewDataOrder(newColumns, oldColumns);
		assertEquals(5, newColumns.get(0).getDataOrder().intValue());
		assertEquals(6, newColumns.get(1).getDataOrder().intValue());
		assertEquals(7, newColumns.get(2).getDataOrder().intValue());
		// 2 случай
		oldColumns.get(1).setDataOrder(6);
		ColumnDaoImpl.calculateNewDataOrder(newColumns, oldColumns);
		assertEquals(1, newColumns.get(0).getDataOrder().intValue());
		assertEquals(5, newColumns.get(1).getDataOrder().intValue());
		assertEquals(7, newColumns.get(2).getDataOrder().intValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void calculateNewDataOrderTest2() {
		List<Column> oldColumns = new ArrayList<Column>();
		// забиваем до предела
		for (int i = 0; i <= Column.MAX_DATA_ORDER; i++) {
			Column column = new StringColumn();
			column.setDataOrder(i);
			oldColumns.add(column);
		}
		List<Column> newColumns = new ArrayList<Column>();
		newColumns.add(new StringColumn());
		ColumnDaoImpl.calculateNewDataOrder(newColumns, oldColumns);
	}

	private FormTemplate getFormTemplate() {
		List<Column> columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.setId(FORM_ID_FOR_TEST);
		formTemplate.getColumns().addAll(columnList);
		return formTemplate;
	}

	@Test
	public void createFormColumnsTest() {
		FormTemplate formTemplate = getFormTemplate();
		int columnCount = formTemplate.getColumns().size();
		List<Column> newColumns = new ArrayList<Column>();

		StringColumn stringColumn = new StringColumn();
		stringColumn.setName("строка");
		stringColumn.setMaxLength(10);
		stringColumn.setAlias("stringColumn2");
		stringColumn.setOrder(6);
		newColumns.add(stringColumn);

		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setName("число");
		numericColumn.setMaxLength(10);
		numericColumn.setPrecision(2);
		numericColumn.setOrder(5);
		numericColumn.setAlias("numericColumn2");
		newColumns.add(numericColumn);

		ReferenceColumn referenceColumn = new ReferenceColumn();
		referenceColumn.setName("зависимая");
		referenceColumn.setParentAlias("stringColumn");
		referenceColumn.setRefBookAttributeId(5);
		referenceColumn.setAlias("referenceColumn2");
		referenceColumn.setName("Зависимая графа");
		referenceColumn.setOrder(7);
		referenceColumn.setDataOrder(referenceColumn.getOrder() - 1);
		referenceColumn.setChecking(false);
		newColumns.add(referenceColumn);

		ColumnDaoImpl.calculateNewDataOrder(newColumns, formTemplate.getColumns());
		assertEquals(4, stringColumn.getDataOrder().intValue());
		assertEquals(5, numericColumn.getDataOrder().intValue());
		assertEquals(6, referenceColumn.getDataOrder().intValue());

		((ColumnDaoImpl) columnDao).createFormColumns(newColumns, formTemplate);
		List<Column> columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);
		assertEquals("numericColumn2", columnList.get(columnCount).getAlias());
		assertEquals("stringColumn2", columnList.get(columnCount + 1).getAlias());
	}

	@Test
	public void setReferenceParentIdTest() {
		FormTemplate formTemplate = getFormTemplate();
		List<Column> newColumns = new ArrayList<Column>();

		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setName("число");
		numericColumn.setMaxLength(10);
		numericColumn.setPrecision(2);
		numericColumn.setOrder(5);
		numericColumn.setAlias("numericColumn2");
		newColumns.add(numericColumn);

		ReferenceColumn referenceColumn = new ReferenceColumn();
		referenceColumn.setName("зависимая");
		referenceColumn.setParentAlias("stringColumn");
		referenceColumn.setRefBookAttributeId(5);
		referenceColumn.setAlias("referenceColumn2");
		referenceColumn.setName("Зависимая графа");
		referenceColumn.setOrder(6);
		referenceColumn.setDataOrder(referenceColumn.getOrder() - 1);
		referenceColumn.setChecking(false);
		newColumns.add(referenceColumn);

		((ColumnDaoImpl) columnDao).setReferenceParentId(formTemplate, newColumns);
		assertEquals(1, ((ReferenceColumn) newColumns.get(1)).getParentId());
	}

	@Test
	public void deleteFormColumnsTest() {
		FormTemplate formTemplate = getFormTemplate();
		List<String> removeColumns = new ArrayList<String>();
		removeColumns.add(formTemplate.getColumn("numericColumn").getAlias());
		removeColumns.add(formTemplate.getColumn("dateColumn").getAlias());

		((ColumnDaoImpl) columnDao).deleteFormColumns(removeColumns, formTemplate);

		List<Column> columns = columnDao.getFormColumns(formTemplate.getId());
		assertEquals(2, columns.size());
		assertEquals("stringColumn", columns.get(0).getAlias());
		assertEquals("autoNumerationColumn", columns.get(1).getAlias());

		//TODO form_data_row
	}

	@Test
	public void clearTypeChangedColumnsTest() {
		FormTemplate formTemplate = getFormTemplate();
		List<Column> columns = formTemplate.getColumns();
		// меняем тип графы
		columns.remove(formTemplate.getColumn("stringColumn"));
		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setName("число");
		numericColumn.setMaxLength(10);
		numericColumn.setPrecision(2);
		numericColumn.setOrder(5);
		numericColumn.setDataOrder(4);
		numericColumn.setAlias("stringColumn");
		columns.add(numericColumn);

		((ColumnDaoImpl) columnDao).clearTypeChangedColumns(formTemplate);

		//TODO form_data_row
	}

	@Test
	public void deleteColumnDataTest() {
		FormTemplate formTemplate = getFormTemplate();
		List<Integer> dataOrders = new ArrayList<Integer>();
		dataOrders.add(0);
		dataOrders.add(3);
		((ColumnDaoImpl) columnDao).deleteColumnData(formTemplate, dataOrders);

		//TODO form_data_row
	}

	@Test
	public void updateFormColumnsTest() {
		FormTemplate formTemplate = getFormTemplate();
		List<Column> oldColumns = new ArrayList<Column>();
		// меняем тип графы
		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setId(12312);
		numericColumn.setName("число");
		numericColumn.setMaxLength(10);
		numericColumn.setPrecision(2);
		numericColumn.setOrder(1);
		numericColumn.setDataOrder(0);
		numericColumn.setAlias("numericColumn");
		oldColumns.add(numericColumn);

		oldColumns.add(formTemplate.getColumn("stringColumn"));
		((StringColumn) oldColumns.get(1)).setMaxLength(100);

		((ColumnDaoImpl) columnDao).updateFormColumns(oldColumns, formTemplate);

		List<Column> columns = columnDao.getFormColumns(formTemplate.getId());
		assertEquals(ColumnType.NUMBER, columns.get(0).getColumnType());
		assertEquals("numericColumn", columns.get(0).getAlias());
		assertEquals(100, ((StringColumn) columns.get(1)).getMaxLength());
		assertEquals("stringColumn", columns.get(1).getAlias());

		//TODO form_data_row
	}

	@Test
	public void updateFormColumnsTest2() {
		FormTemplate formTemplate = getFormTemplate();
		List<Column> columns = formTemplate.getColumns();
		// меняем тип графы
		columns.remove(formTemplate.getColumn("stringColumn"));
		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setId(43623);
		numericColumn.setName("число");
		numericColumn.setMaxLength(10);
		numericColumn.setPrecision(2);
		numericColumn.setOrder(1);
		numericColumn.setDataOrder(0);
		numericColumn.setAlias("stringColumn");
		columns.add(numericColumn);

		((NumericColumn) formTemplate.getColumn("numericColumn")).setMaxLength(20);

		columnDao.updateFormColumns(formTemplate);

		columns = columnDao.getFormColumns(formTemplate.getId());
		assertEquals(ColumnType.NUMBER, columns.get(0).getColumnType());
		assertEquals("numericColumn", columns.get(0).getAlias());
		assertEquals(1, ((DateColumn) columns.get(1)).getFormatId().intValue());
		assertEquals("dateColumn", columns.get(1).getAlias());

		//TODO form_data_row
	}

	//@Test
    public void saveFormColumns1Test() {
        //Given FORM_ID_FOR_TEST
        List<Column> columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);
        //Создадим column, которого нету в БД
        StringColumn newColumn = new StringColumn();
        newColumn.setAlias("newColumn");
        newColumn.setName("Новый столбец");
        newColumn.setOrder(5);
		newColumn.setDataOrder(newColumn.getOrder() - 1);
        newColumn.setMaxLength(100);
        newColumn.setChecking(false);
        columnList.add(newColumn);

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(FORM_ID_FOR_TEST);
        formTemplate.getColumns().addAll(columnList);

        //When
        columnDao.updateFormColumns(formTemplate);

        //Then
        columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);

        newColumn = (StringColumn) columnList.get(4);

        assertEquals(5, columnList.size());
        assertEquals("newColumn", newColumn.getAlias());
        assertEquals("Новый столбец", newColumn.getName());
        assertEquals(5, newColumn.getOrder());
        assertEquals(100, newColumn.getMaxLength());
        assertEquals(false, newColumn.isChecking());
    }

    @Test
    public void saveFormColumnsWithDeleteTest() {
        //Given FORM_ID_FOR_TEST
        List<Column> columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);
        columnList.remove(0);
        //Создадим column, которого нету в БД
        StringColumn newColumn = new StringColumn();
        newColumn.setAlias("newColumn");
        newColumn.setName("Новый столбец");
        newColumn.setOrder(5);
		newColumn.setDataOrder(newColumn.getOrder() - 1);
        newColumn.setMaxLength(100);
        newColumn.setChecking(false);
        columnList.add(newColumn);

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(FORM_ID_FOR_TEST);
        formTemplate.getColumns().addAll(columnList);

        //When
        columnDao.updateFormColumns(formTemplate);

        //Then
        columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);
        assertEquals(4, columnList.size());
    }

    //@Test
    public void saveFormColumns2Test() {

        List<Column> columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);

        RefBookColumn refBookColumn = new RefBookColumn();
        refBookColumn.setRefBookAttributeId(4L);
        refBookColumn.setAlias("refBookColumn");
        refBookColumn.setName("Справочная графа");
        refBookColumn.setOrder(4);
		refBookColumn.setDataOrder(refBookColumn.getOrder() - 1);
        refBookColumn.setChecking(false);
        columnList.add(refBookColumn);

        ReferenceColumn referenceColumn = new ReferenceColumn();
        referenceColumn.setParentId(1);
        referenceColumn.setRefBookAttributeId(5);
        referenceColumn.setAlias("referenceColumn");
        referenceColumn.setName("Зависимая графа");
        referenceColumn.setOrder(5);
		referenceColumn.setDataOrder(referenceColumn.getOrder() - 1);
        referenceColumn.setChecking(false);
        columnList.add(referenceColumn);

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setId(FORM_ID_FOR_TEST);
        formTemplate.getColumns().addAll(columnList);

        columnDao.updateFormColumns(formTemplate);

        columnList = columnDao.getFormColumns(FORM_ID_FOR_TEST);

        referenceColumn = (ReferenceColumn) columnList.get(5);

        assertEquals(6, columnList.size());
        assertEquals("referenceColumn", referenceColumn.getAlias());
        assertEquals("Зависимая графа", referenceColumn.getName());
        assertEquals(6, referenceColumn.getOrder());
        assertEquals(false, referenceColumn.isChecking());
        assertEquals(1, referenceColumn.getParentId());
        assertEquals(5, referenceColumn.getRefBookAttributeId());
    }
}
