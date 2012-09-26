package com.aplana.taxaccounting.controller.form;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.util.ColumnMixIn;

public class ColumnJsonTest {
	@Test
	public void testColumnSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		Column col = new NumericColumn();
		col.setName("Тестовый столбец");		
		col.setAlias("testColumn");
		((NumericColumn)col).setPrecision(3);
		
		ObjectMapper om = new ObjectMapper();
		om.getSerializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
		
		String st = om.writeValueAsString(col);
		assert st.contains("numeric");
	}
	
	@Test
	public void testColumnDataSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		Column col = new NumericColumn();
		col.setName("Тестовый столбец");		
		col.setAlias("testColumn");
		((NumericColumn)col).setPrecision(3);
		Form form = new Form();
		form.getColumns().add(col);
		
		ObjectMapper om = new ObjectMapper();
		om.getSerializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
		
		String st = om.writeValueAsString(form);
		assert st.contains("numeric");
	}
	
}
