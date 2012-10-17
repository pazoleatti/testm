package com.aplana.sbrf.taxaccounting.util.json;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")
@JsonSubTypes({
	@Type(value = NumericColumn.class, name = "numeric"),
	@Type(value = StringColumn.class, name = "string"),
	@Type(value = DateColumn.class, name="date")})
public abstract class ColumnMixIn {
}
