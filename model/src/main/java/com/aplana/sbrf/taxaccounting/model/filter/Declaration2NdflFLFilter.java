package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.json.DateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Фильтр для журнала форм 2-НДФЛ (ФЛ)
 */
@Getter
@Setter
public class Declaration2NdflFLFilter {
    // Реквизиты форм
    private List<Integer> reportPeriodIds;
    private List<Integer> departmentIds;
    private String declarationDataId;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private Date creationDateFrom;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private Date creationDateTo;
    private List<Integer> formStates;
    private String note;
    private String creationUserName;
    private String kpp;
    private String oktmo;
    // Реквизиты ФЛ
    private String lastName;
    private String firstName;
    private String middleName;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private Date birthDateFrom;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private Date birthDateTo;
    private List<Long> docTypeIds;
    private String documentNumber;
    private List<Long> citizenshipCountryIds;
    private List<Long> taxpayerStateIds;
    private String inn;
    private String innForeign;
    private String snils;
}
