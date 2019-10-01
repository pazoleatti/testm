package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
  Тип для получения и передачи в скрипт формирования хмл по 2-НДФЛ номеров справки и корректировки
 */
public class NumFor2Ndfl {

    /**
     * Номер справки для корректирующего периода
     */
    private Integer sprNum;

    /**
     * Номер корректировки
     */
    private Integer corrNum;
}
