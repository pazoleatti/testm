package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
  Тип для получения и передачи в скрипт свойств для проверок формирования анулирующей 2-НДФЛ
 */
public class ReferenceAnnulResult {

    private Long declarationDataId;  // Идентификатор налоговой формы к которой относятся данные
    private Long personId;           // Физическое лицо
    private Integer sprNum;          // Номер справки для корректирующего периода
    private String surname;          // Фамилия
    private String name;             // Имя
    private String lastname;         // Отчество
    private Integer corrNum;         // Номер корректировки
    private Long ndfl_person_id;         // Номер корректировки

}
