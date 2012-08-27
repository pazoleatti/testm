package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.aplana.sbrf.taxaccounting.model.DataRow;

public class DataRowSerializer extends JsonSerializer<DataRow> {
	@Override
	public void serialize(DataRow dataRow, JsonGenerator jg, SerializerProvider p) throws IOException, JsonProcessingException {
		Calendar cal = Calendar.getInstance();
		
		jg.writeStartObject();
		jg.writeStringField("alias", dataRow.getAlias());
		for (Map.Entry<String, Object> entry: dataRow.getData().entrySet()) {
			Object val = entry.getValue();
			String alias = entry.getKey();
			if (val == null) {
				jg.writeNullField(alias);				
			} else if (val instanceof BigDecimal) {
				jg.writeNumberField(alias, (BigDecimal)val);
			} else if (val instanceof String) {
				jg.writeStringField(alias, (String)val);
			} else if (val instanceof Date) {
				jg.writeFieldName(alias);				
				Date dt = (Date)val;
				cal.setTime(dt);
				jg.writeRawValue(String.format("new Date(%d, %d, %d)", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
			} else {
				assert false;
			}
		}
		jg.writeEndObject();
	}
}
