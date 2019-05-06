package com.aplana.sbrf.taxaccounting.model.messaging;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeclarationShortInfo {
    @JsonSerialize(using = ToStringSerializer.class)
    Long id;
    String typeName;
    String departmentName;
    String reportPeriodName;
}
