package com.aplana.sbrf.taxaccounting.service.impl.print.formdata;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class XlsxReportMetadata {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private XlsxReportMetadata() {
	}
	
	public static final String DATE_CREATE = "« %s » %s 20%s г.";
	public static final String REPORT_PERIOD = "за %s %d г./%s %d г.";
	public static final String MONTHLY = "за %s %d г.";

	public static final String RANGE_DATE_CREATE = "date_create";
    public static final String RANGE_REPORT_CODE = "report_code";
    public static final String RANGE_REPORT_PERIOD = "report_period";
    public static final String RANGE_REPORT_NAME = "report_name";
    public static final String RANGE_SUBDIVISION = "subdivision";
    public static final String RANGE_POSITION = "position";
    public static final String RANGE_SUBDIVISION_SIGN = "subdivision_sign";
    public static final String RANGE_FIO = "fio";
	
	public static final char REPORT_DELIMITER = '|';
	
	public static final int CELL_POS = 0; //cell for naming position of signer

	public static final SimpleDateFormat sdf_y = new SimpleDateFormat("yy");
	public static final SimpleDateFormat sdf_m = new SimpleDateFormat("MMMMMM", new Locale("ru"));
	public static final SimpleDateFormat sdf_d = new SimpleDateFormat("dd");
	public static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	
	/*
	 * Patterns for printing in Exel. "###," shows that we must grouping by 3 characters
	 */
    public static String getPrecision(int number){
        StringBuffer str = new StringBuffer("# ##,0");
        if(number>0) {
            str.append(".");
            for (int i = 0; i < number; i++)
                str.append("0");
        }
        return str.toString();
    }

}
