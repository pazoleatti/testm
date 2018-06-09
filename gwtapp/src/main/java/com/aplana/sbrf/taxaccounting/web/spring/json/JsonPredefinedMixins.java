package com.aplana.sbrf.taxaccounting.web.spring.json;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Класс со списком готовых mixin'ов
 * Именовать классы стоит по шаблону <Имя сущности>Filter<Имя проекции> Пример: UserFilterCommon
 * Классы располагать в алфавитном порядке, как в JsonPredefinedFilter
 */
public class JsonPredefinedMixins {

    /**
     * Фильтр - только идентификатор
     */
    @JsonFilter("idOnlyFilter")
    @JsonFilterDescription(name = "idOnlyFilter", filter = JsonPredefinedFilter.ID_ONLY)
    public interface IdOnlyFilter {
    }

    /**
     * Метаданные справочника
     */
    @JsonFilter("refBookMetaFilter")
    @JsonFilterDescription(name = "refBookMetaFilter", filter = JsonPredefinedFilter.REF_BOOK_META)
    public interface RefBookMetaFilter {
    }

    /**
     * Справочник - идентификатор + код + наименование
     */
    @JsonFilter("refBookFilterIdCodeName")
    @JsonFilterDescription(name = "refBookFilterIdCodeName", filter = JsonPredefinedFilter.ABSTRACT_REF_BOOK_CODE_NAME)
    public interface RefBookFilterIdCodeName {
    }
}