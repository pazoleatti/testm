package com.aplana.sbrf.taxaccounting.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParser.Feature;
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

/**
 * Преобразует данные JSON в набор строк {@link данных DataRow}
 */
public class DataRowDeserializer extends JsonDeserializer<DataRow>{
	private Form form;
	private boolean failOnWrongFields;
	
	public DataRowDeserializer(Form form, boolean failOnWrongFields) {
		this.form = form;
		this.failOnWrongFields = failOnWrongFields;
	}
	
	private void validateToken(JsonToken token, JsonToken  expectedToken, JsonParser jp) throws JsonParseException, IOException {
		if (token != expectedToken) {
			throw new IOException(expectedToken.toString() + " expected but " + token.toString() + " found, text is " + jp.getText());
		}
	}
	
	@Override
	public DataRow deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {

		DataRow result = null;
		DateFormat isoFormat = FormatUtils.getIsoDateFormat();
		jp.enable(Feature.ALLOW_NON_NUMERIC_NUMBERS);

		JsonToken token = jp.nextToken();
		while (token != JsonToken.END_OBJECT && token != null) {
			validateToken(token, JsonToken.FIELD_NAME, jp);
			String fieldName = jp.getText();
			token = jp.nextToken();
			if ("alias".equals(fieldName)) {
				// требуется, что alias был первым полем в json-представлении объекта
				// в противном случае возможны NullPointerException'ы
				validateToken(token, JsonToken.VALUE_STRING, jp);
				String alias = jp.getText();
				result = new DataRow(alias, form); 
			} if ("order".equals(fieldName)) {
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
				if (token == JsonToken.VALUE_NULL) {
					result.put(fieldName, null);
				} else if (col instanceof NumericColumn) {
					BigDecimal value = jp.getDecimalValue();					
					result.put(fieldName, value);
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
						result.put(fieldName, isoFormat.parseObject(stDate));
					} catch (ParseException e) {
						throw new IOException("Wrong date format: " + stDate, e);
					}
				} else if (col instanceof StringColumn) {
					result.put(fieldName, jp.getText());
				}
			}
			token = jp.nextToken();
		}
		jp.disable(Feature.ALLOW_NON_NUMERIC_NUMBERS);
		return result;
	}
}
