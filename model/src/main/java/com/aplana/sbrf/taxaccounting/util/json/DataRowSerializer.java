package com.aplana.sbrf.taxaccounting.util.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.aplana.sbrf.taxaccounting.model.DataRow;

public class DataRowSerializer extends JsonSerializer<DataRow> {
	DateFormat dateFormat;
	
	public DataRowSerializer(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	@Override
	public void serialize(DataRow dataRow, JsonGenerator jg, SerializerProvider p) throws IOException, JsonProcessingException {
		jg.writeStartObject();
		jg.writeStringField("alias", dataRow.getAlias());
		jg.writeNumberField("order", dataRow.getOrder());
		
		for (Map.Entry<String, Object> entry: dataRow.entrySet()) {
			Object val = entry.getValue();
			String alias = entry.getKey();
			if (val == null) {
				jg.writeNullField(alias);				
			} else if (val instanceof BigDecimal) {
				jg.writeNumberField(alias, (BigDecimal)val);
			} else if (val instanceof String) {
				jg.writeStringField(alias, (String)val);
			} else if (val instanceof Date) {
				Date dt = (Date)val;
				jg.writeStringField(alias, dateFormat.format(dt));
			} else {
				assert false;
			}
		}
		jg.writeEndObject();
	}
}
