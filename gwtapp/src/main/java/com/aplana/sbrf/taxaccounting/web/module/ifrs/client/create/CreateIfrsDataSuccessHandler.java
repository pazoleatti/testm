package com.aplana.sbrf.taxaccounting.web.module.ifrs.client.create;

import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.CreateIfrsDataResult;

/**
 * Хэндлер для подтверждения создания отчетности для МСФО
 */
public interface CreateIfrsDataSuccessHandler {
    void onSuccess(CreateIfrsDataResult result);
}
