package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.json.DateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class DeclarationDataFilter implements Serializable {
    private static final long serialVersionUID = -4400641153082281834L;

    private TaxType taxType;

    private List<Integer> reportPeriodIds;

    private List<Integer> departmentIds;

    private List<Long> declarationTypeIds;

    private List<Integer> formStates;

    private Boolean correctionTag;

    private Date correctionDate;

    /*Стартовый индекс списка записей */
    private int startIndex;

    /*Количество записей, которые нужно вернуть*/
    private int countOfRecords;

    private Long declarationDataId;

    private String declarationDataIdStr;

    private DeclarationDataSearchOrdering searchOrdering;

    private String taxOrganCode;

    private String taxOrganKpp;

    private String oktmo;

    private List<Long> docStateIds;

    private List<Long> formKindIds;

    private String fileName;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private Date creationDateFrom;
    @JsonDeserialize(using = DateTimeDeserializer.class)
    private Date creationDateTo;

    private String creationUserName;

    private String note;

    private List<Long> asnuIds;

    /**
     * Типы КНФ {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType}
     */
    private List<Long> knfTypeIds;

    /**
     * Подразделение пользователя, должно задаваться только пользователю с ролью Оператор
     */
    private Integer userDepartmentId;

    private Boolean controlNs;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;
    /**
     * Номер корректировки
     */
    private Integer correctionNum;

    /**
     * мапа ключ-идентификатор типа налоговой формы, значение-Список идентификаторов подразделений,
     * для которых подразделение пользователя назначено исполнителем
     */
    @JsonIgnore
    private Map<Integer, Set<Integer>> declarationTypeDepartmentMap;

    public void setFormState(State state) {
        formStates = Collections.singletonList(state.getId());
    }
}
