package com.aplana.sbrf.taxaccounting.web.widget.signin.server;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Component
public class GetUserInfoActionHandler extends AbstractActionHandler<GetUserInfoAction, GetUserInfoResult>{
	

	public GetUserInfoActionHandler() {
		super(GetUserInfoAction.class);
	}

	@Override
	public GetUserInfoResult execute(GetUserInfoAction action,
			ExecutionContext context) throws ActionException {
		
		  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	      String name = auth.getName();
	      
	      GetUserInfoResult result = new GetUserInfoResult();
	      result.setUserName(name);
	      return result;
	      
	}

	@Override
	public void undo(GetUserInfoAction action, GetUserInfoResult result,
			ExecutionContext context) throws ActionException {
	}

}
