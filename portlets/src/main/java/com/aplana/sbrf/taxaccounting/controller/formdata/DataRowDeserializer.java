package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;

public class DataRowDeserializer extends JsonDeserializer<DataRow>{
	Form form;
	public DataRowDeserializer(Form form) {
		this.form = form;
	}
	@Override
	public DataRow deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {

		DataRow result = null;
		DateFormat isoFormat = FormatUtils.getIsoDateFormat();

		JsonToken t = jp.nextToken();
		while (t != JsonToken.END_OBJECT && t != null) {
			if (t != JsonToken.FIELD_NAME) {
				throw new RuntimeException("Field name expected but " + t.toString() + " found, text is " + jp.getText());
			}
			String fieldName = jp.getText();
			t = jp.nextToken();
			if ("alias".equals(fieldName)) {
				// требуется, что alias был первым полем в json-представлении объекта
				// в противном случае возможны NullPointerException'ы
				String alias = jp.getText();
				result = new DataRow(alias, form.getColumns()); 
			} else {
				Column col = form.getColumn(fieldName);
				if (col instanceof NumericColumn) {
					// TODO: Добавить округление данных в соответствии с точностью, указанной в объекте Column
					result.setColumnValue(fieldName, jp.getDecimalValue());
				} else if (col instanceof DateColumn) {
					// Даты могут передаваться как в виде строк ISO-формата, так и  виде объектов
					String stDate;
					if (t == JsonToken.START_OBJECT) {
						t = jp.nextToken();
						assert t == JsonToken.FIELD_NAME && jp.getText().equals("_type");
						t = jp.nextToken();
						assert t == JsonToken.VALUE_STRING && jp.getText().equals("Date");
						t = jp.nextToken();
						assert t == JsonToken.FIELD_NAME && jp.getText().equals("_value");
						t = jp.nextToken();
						stDate = jp.getText();
						t = jp.nextToken();
						assert t == JsonToken.END_OBJECT;
					} else {
						stDate = jp.getText();
					}
					if (!"null".equals(stDate)) {
						try {
							result.setColumnValue(fieldName, isoFormat.parseObject(stDate));
						} catch (ParseException e) {
							throw new IOException("Wrong date format: " + stDate, e);
						}
					}
				} else if (col instanceof StringColumn) {
					result.setColumnValue(fieldName, jp.getText());
				}
			}
			t = jp.nextToken();
		}
		return result;
	}
}
