package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;

/**
 * DAO-интерфейс для работы с таблицей "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
 */
public interface RaschsvRashOssZakDao {

    /**
     * Сохранение "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
     * @param raschsvRashOssZak
     * @return
     */
    Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak);
}
