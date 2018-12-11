package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Параметры для формирования отчета "Данные для включения в 2-НДФЛ и 6-НДФЛ"
 */
@Getter
@Setter
public class Ndfl2_6DataReportParams implements Serializable {
    @JsonDeserialize(using = ISODateDeserializer.class)
    Date dateFrom;
    @JsonDeserialize(using = ISODateDeserializer.class)
    Date dateTo;
    boolean adjustNegativeValues;
    List<String> kppList;
}
