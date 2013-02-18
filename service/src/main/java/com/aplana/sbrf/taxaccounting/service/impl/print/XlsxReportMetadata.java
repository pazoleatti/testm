package com.aplana.sbrf.taxaccounting.service.impl.print;

public class XlsxReportMetadata {
	
	public static String DATE_CREATE = "« %s » %s 20%s г.";
	public static String REPORT_PERIOD = "за %s ";
	
	public static String RANGE_DATE_CREATE = "date_create";
	public static String RANGE_REPORT_PERIOD = "report_period";
	public static String RANGE_SUBDIVISION = "subdivision";
	public static String RANGE_POSITION = "position";
	public static String RANGE_FIO = "fio";
	
	public static int CELL_POS = 0; //cell for naming position of signer 
	public static int CELL_SIGN = 8; //cell for sign
	public static int CELL_FIO = 9; //cell for FIO of signer

}
