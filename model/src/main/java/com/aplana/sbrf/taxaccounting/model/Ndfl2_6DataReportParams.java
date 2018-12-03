package com.aplana.sbrf.taxaccounting.model;

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
    Date dateFrom;
    Date dateTo;
    boolean adjustNegativeValues;
    List<String> kppList;
}
