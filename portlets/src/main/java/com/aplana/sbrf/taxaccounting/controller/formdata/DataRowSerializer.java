package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.aplana.sbrf.taxaccounting.model.DataRow;

public class DataRowSerializer extends JsonSerializer<DataRow> {

	@Override
	public void serialize(DataRow dataRow, JsonGenerator jg, SerializerProvider p) throws IOException, JsonProcessingException {
		jg.writeStartObject();
		jg.writeStringField("code", dataRow.getCode());
		for (Map.Entry<String, Object> entry: dataRow.getData().entrySet()) {
			jg.writeStringField(entry.getKey(), "test");
		}
		jg.writeEndObject();
	}
}
