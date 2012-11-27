package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Тестируем "сериализацию" строк данных.
 * @author Vitalii Samolovskikh
 */
public class SerializationUtilsTest {

	private List<Column> columns;

	public SerializationUtilsTest() {
		// Columns
		columns = new ArrayList<Column>();

		Column stringColumn = new StringColumn();
		stringColumn.setAlias("stringColumn");
		columns.add(stringColumn);

		NumericColumn numericColumn = new NumericColumn();
		numericColumn.setAlias("numericColumn");
		numericColumn.setPrecision(2);
		columns.add(numericColumn);

		Column dateColumn = new DateColumn();
		dateColumn.setAlias("dateColumn");
		columns.add(dateColumn);
	}

	@Test
	public void testSimple(){
		assert true;
	}

	@Test
	public void testSerialization(){
		// Prepare
		List<DataRow> data = prepareData();
		assertNotNull("The initial data is null.", data);
		assertFalse("The initial data is empty.", data.isEmpty());

		// Serialize
		String string = SerializationUtils.serialize(data);
		assertNotNull("The result of serialization is null.", string);
		assertFalse("The result of serialization is empty.", string.isEmpty());

		System.out.println(string);

		// Deserialize
		List<DataRow> deserializedData = SerializationUtils.deserialize(string, columns);
		assertNotNull("The result of deserialization is null.", deserializedData);
		assertFalse("The result of deserialization is empty.", deserializedData.isEmpty());

		// Check equals
		assertEquals("The result of deserialization doesn't equals the initial data.", data, deserializedData);
	}

	public List<DataRow> prepareData(){
		List<DataRow> rows = new ArrayList<DataRow>();

		// Empty row
		rows.add(new DataRow(columns));

		// Row with alias and order parameter
		DataRow row = new DataRow(columns);
		row.setAlias("alias");
		row.setOrder(1001);
		rows.add(row);


		row = new DataRow("withColumns", columns);
		row.put("stringColumn", "test");
		row.put("numericColumn", new BigDecimal(1234.56));
		row.put("dateColumn", new Date());
		rows.add(row);

		return rows;
	}
}
