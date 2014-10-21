package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormDataResult;

/**
 * Хэндлер для подтверждения создания НФ
 */
public interface CreateFormDataSuccessHandler {
    void onSuccess(CreateFormDataResult result);
}
