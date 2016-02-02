package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.test.BDUtilsMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

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
    public void getFormColumns() {
        //Given FORM_ID_FOR_TEST, NUMBER_OF_STYLES

        //When
        List<Column> listOfColumnsInDb = columnDao.getFormColumns(FORM_ID_FOR_TEST);

        //Then
        Assert.assertEquals(NUMBER_OF_COLUMNS, listOfColumnsInDb.size());

        Assert.assertEquals(Integer.valueOf(1), listOfColumnsInDb.get(FIRST_COLUMN).getId());
        Assert.assertEquals("Строковый столбец", listOfColumnsInDb.get(FIRST_COLUMN).getName());
        Assert.assertEquals(1, listOfColumnsInDb.get(FIRST_COLUMN).getOrder());
        Assert.assertEquals("stringColumn", listOfColumnsInDb.get(FIRST_COLUMN).getAlias());
        Assert.assertEquals(500, ((StringColumn) listOfColumnsInDb.get(FIRST_COLUMN)).getMaxLength());
        Assert.assertEquals(true, listOfColumnsInDb.get(FIRST_COLUMN).isChecking());

        Assert.assertEquals(Integer.valueOf(2), listOfColumnsInDb.get(SECOND_COLUMN).getId());
        Assert.assertEquals("Числовой столбец", listOfColumnsInDb.get(SECOND_COLUMN).getName());
        Assert.assertEquals(2, listOfColumnsInDb.get(SECOND_COLUMN).getOrder());
        Assert.assertEquals("numericColumn", listOfColumnsInDb.get(SECOND_COLUMN).getAlias());
        Assert.assertEquals(false, listOfColumnsInDb.get(SECOND_COLUMN).isChecking());

        Assert.assertEquals(Integer.valueOf(3), listOfColumnsInDb.get(THIRD_COLUMN).getId());
        Assert.assertEquals("Дата-столбец", listOfColumnsInDb.get(THIRD_COLUMN).getName());
        Assert.assertEquals(3, listOfColumnsInDb.get(THIRD_COLUMN).getOrder());
        Assert.assertEquals("dateColumn", listOfColumnsInDb.get(THIRD_COLUMN).getAlias());
        Assert.assertEquals(false, listOfColumnsInDb.get(SECOND_COLUMN).isChecking());

        Assert.assertEquals(Integer.valueOf(4), listOfColumnsInDb.get(FOURTH_COLUMN).getId());
        Assert.assertEquals("Автонумеруемая графа", listOfColumnsInDb.get(FOURTH_COLUMN).getName());
        Assert.assertEquals(4, listOfColumnsInDb.get(FOURTH_COLUMN).getOrder());
        Assert.assertEquals("autoNumerationColumn", listOfColumnsInDb.get(FOURTH_COLUMN).getAlias());
        Assert.assertEquals(false, listOfColumnsInDb.get(FOURTH_COLUMN).isChecking());
        if (ColumnType.AUTO.equals(listOfColumnsInDb.get(FOURTH_COLUMN).getColumnType())) {
            Assert.assertEquals(1, ((AutoNumerationColumn)listOfColumnsInDb.get(FOURTH_COLUMN)).getNumerationType().getId());
        }
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

        Assert.assertEquals(5, columnList.size());
        Assert.assertEquals("newColumn", newColumn.getAlias());
        Assert.assertEquals("Новый столбец", newColumn.getName());
        Assert.assertEquals(5, newColumn.getOrder());
        Assert.assertEquals(100, newColumn.getMaxLength());
        Assert.assertEquals(false, newColumn.isChecking());
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
        Assert.assertEquals(4, columnList.size());
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

        Assert.assertEquals(6, columnList.size());
        Assert.assertEquals("referenceColumn", referenceColumn.getAlias());
        Assert.assertEquals("Зависимая графа", referenceColumn.getName());
        Assert.assertEquals(6, referenceColumn.getOrder());
        Assert.assertEquals(false, referenceColumn.isChecking());
        Assert.assertEquals(1, referenceColumn.getParentId());
        Assert.assertEquals(5, referenceColumn.getRefBookAttributeId());
    }
}
