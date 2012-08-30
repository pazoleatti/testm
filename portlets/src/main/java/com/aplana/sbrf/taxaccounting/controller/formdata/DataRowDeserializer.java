package com.aplana.sbrf.taxaccounting.controller.formdata;

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
import com.aplana.sbrf.taxaccounting.util.FormatUtils;

/**
 * Преобразует данные JSON в набор строк {@link данных DataRow}
 */
public class DataRowDeserializer extends JsonDeserializer<DataRow>{
	Form form;
	public DataRowDeserializer(Form form) {
		this.form = form;
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
			} else {				
				Column col = form.getColumn(fieldName);
				if (token == JsonToken.VALUE_NULL) {
					result.setColumnValue(fieldName, null);
				} else if (col instanceof NumericColumn) {
					BigDecimal value = jp.getDecimalValue();					
					result.setColumnValue(fieldName, value);
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
						result.setColumnValue(fieldName, isoFormat.parseObject(stDate));
					} catch (ParseException e) {
						throw new IOException("Wrong date format: " + stDate, e);
					}
				} else if (col instanceof StringColumn) {
					result.setColumnValue(fieldName, jp.getText());
				}
			}
			token = jp.nextToken();
		}
		jp.disable(Feature.ALLOW_NON_NUMERIC_NUMBERS);
		return result;
	}
}
