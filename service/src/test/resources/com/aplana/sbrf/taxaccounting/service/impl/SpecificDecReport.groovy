package com.aplana.sbrf.taxaccounting.service.impl

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

switch (formDataEvent) {
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        def writer = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream());
        writer.write(scriptSpecificReportHolder.getDeclarationSubreport().getAlias())
        writer.close()
        scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getDeclarationSubreport().getAlias()+".txt")
        break
}