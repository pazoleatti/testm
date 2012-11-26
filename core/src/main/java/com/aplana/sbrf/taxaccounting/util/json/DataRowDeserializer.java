package com.aplana.sbrf.taxaccounting.util.json;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;

/**
 * Преобразует данные JSON в набор строк {@link данных DataRow}
 */
public class DataRowDeserializer extends JsonDeserializer<DataRow>{
	private Log logger = LogFactory.getLog(getClass());
	
	private FormTemplate form;
	private boolean failOnWrongFields;
	private DateFormat dateFormat;
	
	public DataRowDeserializer(FormTemplate form, DateFormat dateFormat, boolean failOnWrongFields) {
		this.form = form;
		this.failOnWrongFields = failOnWrongFields;
		this.dateFormat = dateFormat;
	}
	
	private void validateToken(JsonToken token, JsonToken  expectedToken, JsonParser jp) throws JsonParseException, IOException {
		if (token != expectedToken) {
			throw new IOException(expectedToken.toString() + " expected but " + token.toString() + " found, text is " + jp.getText());
		}
	}
	
	@Override
	public DataRow deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {
		DataRow result = new DataRow(form.getColumns());
		JsonToken token = jp.nextToken();
		while (token != JsonToken.END_OBJECT && token != null) {
			validateToken(token, JsonToken.FIELD_NAME, jp);
			String fieldName = jp.getText();
			token = jp.nextToken();
			if ("alias".equals(fieldName)) {
				validateToken(token, JsonToken.VALUE_STRING, jp);
				result.setAlias(jp.getText()); 
			} else if ("order".equals(fieldName)) {
				validateToken(token, JsonToken.VALUE_NUMBER_INT, jp);
				int order = jp.getIntValue();
				result.setOrder(order);
			} else {
				Column col;
				try {
					col = form.getColumn(fieldName);
				} catch (IllegalArgumentException e) {
					if (failOnWrongFields) {
						throw e;
					} else {
						jp.skipChildren();
						token = jp.nextToken();
						continue;
					}
				}
				Object value;
				
				if (token == JsonToken.VALUE_NULL) {
					value = null;
				} else if (col instanceof NumericColumn) {
					try {
						value = jp.getDecimalValue();
					} catch (JsonParseException e) {
						logger.warn("Failed to parse numeric value " + jp.getText() + ", assuming null");
						value = null;
					}
				} else if (col instanceof DateColumn) {
					// Даты могут передаваться как в виде строк ISO-формата, так и  виде объектов
					String stDate;
					if (token == JsonToken.START_OBJECT) {
						token = jp.nextToken();
						assert token == JsonToken.FIELD_NAME && jp.getText().equals("_type");
						token = jp.nextToken();
						assert token == JsonToken.VALUE_STRING && jp.getText().equals("Date");
						token = jp.nextToken();
						assert token == JsonToken.FIELD_NAME && jp.getText().equals("_value");
						token = jp.nextToken();
						stDate = jp.getText();
						token = jp.nextToken();
						assert token == JsonToken.END_OBJECT;
					} else {
						stDate = jp.getText();
					}
					try {
						value = dateFormat.parseObject(stDate);
					} catch (ParseException e) {
						logger.warn("Failed to parse date: " + stDate + ", assuming null");
						value = null;
					}
				} else if (col instanceof StringColumn) {
					value = jp.getText();
				} else {
					throw new IllegalArgumentException("Unknown column type: " + col.getClass());
				}
				result.put(fieldName, value);
			}
			token = jp.nextToken();
		}
		if (result.getAlias() == null) {
			throw new IllegalArgumentException("Alias is not specified in json string representing data row");
		}
		return result;
	}
}
