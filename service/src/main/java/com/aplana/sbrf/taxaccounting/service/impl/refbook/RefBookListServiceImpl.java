package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.result.RefBookListResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RefBookListServiceImpl implements RefBookListService {

    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    @PreAuthorize("isAuthenticated()")
    public PagingResult<RefBookListResult> fetchAllRefbooks() {
        List<RefBook> refBookList = refBookFactory.getAll(true);
        PagingResult<RefBookListResult> toRet = new PagingResult<>();
        for (RefBook refBook : refBookList) {
            RefBookListResult res = new RefBookListResult();
            res.setRefBookName(refBook.getName());
            res.setRefBookType(refBook.getType());
            res.setRefBookId(refBook.getId());
            res.setReadOnly(refBook.isReadOnly());
            toRet.add(res);
        }
        return toRet;
    }
}
