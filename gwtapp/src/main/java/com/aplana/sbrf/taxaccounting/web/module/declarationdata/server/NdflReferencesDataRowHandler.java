package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetNdflReferencesResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetNdflReferencesTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.NdflReferenceDTO;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class NdflReferencesDataRowHandler extends AbstractActionHandler<GetNdflReferencesTableDataAction, GetNdflReferencesResult> {

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;

    private final Long NDFL_REFERENCES_REF_BOOK_ID = 964L;
    private final String ID = "ID";
    private final String DECLARATION_DATA_ID = "DECLARATION_DATA_ID";
    private final String NUM = "NUM";
    private final String SURNAME = "SURNAME";
    private final String NAME = "NAME";
    private final String LASTNAME = "LASTNAME";
    private final String BIRTHDAY = "BIRTHDAY";
    private final String ERRTEXT = "ERRTEXT";

    public NdflReferencesDataRowHandler() {
        super(GetNdflReferencesTableDataAction.class);
    }

    @Override
    public GetNdflReferencesResult execute(GetNdflReferencesTableDataAction action, ExecutionContext context) throws ActionException {

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(NDFL_REFERENCES_REF_BOOK_ID);

        GetNdflReferencesResult result = new GetNdflReferencesResult();

        RefBook refBook = commonRefBookService.get(NDFL_REFERENCES_REF_BOOK_ID);

        String refNumber = action.getRefNumber();
        Long declarationDataId = action.getDeclarationDataId();
        String lastNamePattrern = action.getLastNamePattrern();
        String firstNamepattern = action.getFirstNamePattern();
        String middleNamePattern = action.getMiddleNamePattern();
        Date birthDateFrom = action.getBirthDateFrom();
        Date birthDateBefore = action.getBirthDateBefore();

        StringBuilder filterBuilder = new StringBuilder();
        filterBuilder.append(DECLARATION_DATA_ID)
                .append("=")
                .append(declarationDataId);

        if (refNumber != null && !refNumber.isEmpty()) {
            filterBuilder.append(" and ")
                    .append(NUM)
                    .append(" = ")
                    .append(refNumber);
        }
        if (lastNamePattrern != null && !lastNamePattrern.isEmpty()) {
            filterBuilder.append(" and ")
                    .append("LOWER(")
                    .append(SURNAME)
                    .append(") like LOWER('")
                    .append(lastNamePattrern)
                    .append("%'")
                    .append(")");
        }
        if (firstNamepattern != null && !firstNamepattern.isEmpty()) {
            filterBuilder.append(" and ")
                    .append("LOWER(")
                    .append(NAME)
                    .append(") like LOWER('")
                    .append(firstNamepattern).append("%'")
                    .append(")");
        }
        if (middleNamePattern != null && !middleNamePattern.isEmpty()) {
            filterBuilder.append(" and ")
                    .append("LOWER(")
                    .append(LASTNAME)
                    .append(") like LOWER('")
                    .append(middleNamePattern).append("%'")
                    .append(")");
        }

        PagingResult<Map<String, RefBookValue>> refBookPage = refBookDataProvider.getRecords(new Date(),
                null, filterBuilder.toString(), refBook.getAttribute(NUM), true);


        result.setNdflReferences(toNdflReferenceDTO(filterDates(refBookPage, birthDateFrom, birthDateBefore)));

        return result;
    }

    @Override
    public void undo(GetNdflReferencesTableDataAction action, GetNdflReferencesResult result, ExecutionContext context) throws ActionException {

    }

    private List<NdflReferenceDTO> toNdflReferenceDTO(PagingResult<Map<String, RefBookValue>> pagingResult) {
        List<NdflReferenceDTO> toReturn = new ArrayList<NdflReferenceDTO>();
        for (Map<String, RefBookValue> refBookValues : pagingResult) {
            toReturn.add(toNdflReferenceDTO(refBookValues));
        }
        return toReturn;
    }

    private NdflReferenceDTO toNdflReferenceDTO(Map<String, RefBookValue> refBookValues) {
        NdflReferenceDTO toReturn = new NdflReferenceDTO();
        for (Map.Entry<String, RefBookValue> refBookValueEntry : refBookValues.entrySet()) {
            if (refBookValueEntry.getKey().equalsIgnoreCase(ID)) {
                toReturn.setId(refBookValueEntry.getValue().getNumberValue().longValue());
            } else if (refBookValueEntry.getKey().equalsIgnoreCase(NUM)) {
                toReturn.setRefNumber(refBookValueEntry.getValue().getNumberValue().toString());
            } else if (refBookValueEntry.getKey().equalsIgnoreCase(SURNAME)) {
                toReturn.setLastName(refBookValueEntry.getValue().getStringValue());
            } else if (refBookValueEntry.getKey().equalsIgnoreCase(NAME)) {
                toReturn.setFirstName(refBookValueEntry.getValue().getStringValue());
            } else if (refBookValueEntry.getKey().equalsIgnoreCase(LASTNAME)) {
                toReturn.setMiddleName(refBookValueEntry.getValue().getStringValue());
            } else if (refBookValueEntry.getKey().equalsIgnoreCase(BIRTHDAY)) {
                toReturn.setBirthDate(refBookValueEntry.getValue().getDateValue());
            } else if (refBookValueEntry.getKey().equalsIgnoreCase(ERRTEXT)) {
                toReturn.setErrorText(refBookValueEntry.getValue().getStringValue());
            }
        }
        return toReturn;
    }

    private PagingResult<Map<String, RefBookValue>> filterDates(PagingResult<Map<String, RefBookValue>> pagingResult, Date fromDate, Date beforeDate) {
        PagingResult<Map<String, RefBookValue>> toReturn = new PagingResult<Map<String, RefBookValue>>();

        if (fromDate != null && beforeDate != null) {
            for (Map<String, RefBookValue> refBookValues : pagingResult) {
                Date birthDay = refBookValues.get(BIRTHDAY).getDateValue();
                if (birthDay != null && birthDay.compareTo(fromDate) >= 0 && birthDay.compareTo(beforeDate) <= 0) {
                    toReturn.add(refBookValues);
                }
            }
        } else if (fromDate != null) {
            for (Map<String, RefBookValue> refBookValues : pagingResult) {
                Date birthDay = refBookValues.get(BIRTHDAY).getDateValue();
                if (birthDay != null && birthDay.compareTo(fromDate) >= 0) {
                    toReturn.add(refBookValues);
                }
            }
        } else if (beforeDate != null) {
            for (Map<String, RefBookValue> refBookValues : pagingResult) {
                Date birthDay = refBookValues.get(BIRTHDAY).getDateValue();
                if (birthDay != null && birthDay.compareTo(birthDay) <= 0) {
                    toReturn.add(refBookValues);
                }
            }
        } else {
            return pagingResult;
        }
        return toReturn;
    }

    private String convertDateToString(Date date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

}
