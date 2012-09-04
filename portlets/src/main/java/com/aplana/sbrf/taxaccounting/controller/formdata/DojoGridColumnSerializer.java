package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;

/**
 * Используется для сериализации представления карточки
 * @author Aplana
 *
 */
public class DojoGridColumnSerializer extends JsonSerializer<Column> {

	@Override
	public void serialize(Column col, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
		jg.writeStartObject();
		jg.writeStringField("name", col.getName());
		jg.writeStringField("field", col.getAlias());
		jg.writeStringField("width", col.getWidth() + "em");
		jg.writeBooleanField("editable", col.isEditable());
		if (col instanceof DateColumn) {
			jg.writeFieldName("type");
			jg.writeRawValue("dojox.grid.cells.DateTextBox");
			jg.writeFieldName("formatter");
			jg.writeRawValue("formatDate");
			jg.writeFieldName("constraint");
			jg.writeStartObject();
			jg.writeStringField("datePattern", "dd.MM.yyyy");
			jg.writeStringField("selector", "date");
			jg.writeEndObject();
		} else if (col instanceof NumericColumn) {
			NumericColumn nc = (NumericColumn)col;
			jg.writeFieldName("type");
			jg.writeRawValue("dojox.grid.cells._Widget");
			jg.writeFieldName("widgetClass");
			jg.writeRawValue("dijit.form.NumberTextBox");
			jg.writeFieldName("constraint");
			jg.writeStartObject();
			jg.writeNumberField("places", nc.getPrecision());
			jg.writeFieldName("formatter");
			jg.writeRawValue("formatNumber");			
			jg.writeEndObject();
		}
		jg.writeEndObject();		
	}

}
