package com.aplana.sbrf.taxaccounting.dao.security;

import com.aplana.sbrf.taxaccounting.model.security.TAUser;

public interface TAUserDao {
	TAUser getUser(String login);
	TAUser getUser(int userId);
}
