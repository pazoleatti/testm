package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Service("transactionHelperGlobal")
public class TransactionHelperGlobalImpl implements TransactionHelper {

    @Autowired
    private PlatformTransactionManager secondaryTransactionManager;

    @Override
    public <T> T executeInNewTransaction(final TransactionLogic<T> logic) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(secondaryTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate.execute(new TransactionCallback<T>() {
            @Override
            public T doInTransaction(TransactionStatus status) {
				return logic.execute();
            }
        });
    }

	@Override
	public <T> T executeInNewReadOnlyTransaction(final TransactionLogic<T> logic) {
		TransactionTemplate transactionTemplate = new TransactionTemplate(secondaryTransactionManager);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.setReadOnly(true); // только чтение
		return transactionTemplate.execute(new TransactionCallback<T>() {
			@Override
			public T doInTransaction(TransactionStatus status) {
                status.setRollbackOnly();
				return logic.execute();
			}
		});
	}
}