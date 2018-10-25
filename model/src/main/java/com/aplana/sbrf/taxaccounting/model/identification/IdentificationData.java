package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Класс инкапсулирующий данные для идентификации ФЛ
 */
@Getter @Setter
public class IdentificationData {

    private NaturalPerson naturalPerson;

    private List<NaturalPerson> refBookPersonList;

    private int tresholdValue;

    private long declarationDataAsnuId;

    /**
     * Кэш проритетов АСНУ. Ключ id ref_book-asnu, значение асну
     */
    private Map<Long, RefBookAsnu> priorityMap;

}
