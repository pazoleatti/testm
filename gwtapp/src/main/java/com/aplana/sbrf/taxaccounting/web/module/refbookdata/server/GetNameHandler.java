package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetNameAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetNameResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("isAuthenticated()")
public class GetNameHandler extends AbstractActionHandler<GetNameAction, GetNameResult> {

	@Autowired
	RefBookFactory refBookFactory;
    @Autowired
    RefBookHelper refBookHelper;

	public GetNameHandler() {
		super(GetNameAction.class);
	}

	@Override
	public GetNameResult execute(GetNameAction action, ExecutionContext executionContext) throws ActionException {
        GetNameResult result = new GetNameResult();
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());

        if (action.getUniqueRecordId() != null) {
            result.setName(refBook.getName());
            result.setRefBookType(refBook.getType());
            result.setUniqueAttributeValues(refBookHelper.buildUniqueRecordName(refBook,
                    refBookDataProvider.getUniqueAttributeValues(action.getUniqueRecordId())));
            Long recordId = refBookDataProvider.getRecordId(action.getUniqueRecordId());
            result.setRecordId(recordId);
        }

		return result;
	}

	@Override
	public void undo(GetNameAction getNameAction, GetNameResult getNameResult, ExecutionContext executionContext) throws ActionException {
	}
}
