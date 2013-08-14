package com.aplana.sbrf.taxaccounting.dao.script.dictionary;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Справочник "ОКАТО"
 * @author Dmitriy Levykin
 */
@ScriptExposed
public interface RefBookOkatoDao {

    /**
     * Вычисление родительского кода ОКАТО и обновление записей
     * @param version Обновляемая версия
     */
    public int updateParentId(Date version);
}
