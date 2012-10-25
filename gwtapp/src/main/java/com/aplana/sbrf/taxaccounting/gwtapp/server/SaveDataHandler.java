package com.aplana.sbrf.taxaccounting.gwtapp.server;

import com.aplana.sbrf.taxaccounting.gwtapp.shared.SaveDataAction;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SaveDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.stereotype.Service;

/** @author Vitalii Samolovskikh */
@Service
public class SaveDataHandler extends AbstractActionHandler<SaveDataAction, SaveDataResult> {
    public SaveDataHandler() {
        super(SaveDataAction.class);
    }

    @Override
    public SaveDataResult execute(SaveDataAction action, ExecutionContext context) throws ActionException {
        System.out.println(action.getClass().getName());
        return new SaveDataResult();
    }

    @Override
    public void undo(SaveDataAction action, SaveDataResult result, ExecutionContext context)
            throws ActionException {
        // Nothing!
    }
}
