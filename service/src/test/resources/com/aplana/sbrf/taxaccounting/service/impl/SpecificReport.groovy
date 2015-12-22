import com.aplana.sbrf.taxaccounting.model.FormDataEvent

switch (formDataEvent) {
    case FormDataEvent.GET_SPECIFIC_REPORT_TYPES:
        specificReportType.add("Type1")
        specificReportType.add("Type2(CSV)")
        specificReportType.add("Тип3 список")
        specificReportType.add("XLSM")
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        def writer = new PrintWriter(scriptSpecificReportHolder.getFileOutputStream());
        writer.write(scriptSpecificReportHolder.getSpecificReportType())
        writer.close()
        scriptSpecificReportHolder.setFileName(scriptSpecificReportHolder.getSpecificReportType()+".txt")
        break
}