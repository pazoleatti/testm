package com.aplana.sbrf.taxaccounting.service.impl.print;

import java.text.SimpleDateFormat;

public class XlsxReportMetadata {
	
	public static final String DATE_CREATE = "« %s » %s 20%s г.";
	public static final String REPORT_PERIOD = "за %s ";
	
	public static final String RANGE_DATE_CREATE = "date_create";
	public static final String RANGE_REPORT_PERIOD = "report_period";
	public static final String RANGE_SUBDIVISION = "subdivision";
	public static final String RANGE_POSITION = "position";
	public static final String RANGE_FIO = "fio";
	
	public static final int CELL_POS = 0; //cell for naming position of signer
	public static final int CELL_SIGN = 8; //cell for sign
	public static final int CELL_FIO = 9; //cell for FIO of signer
	
	public static final SimpleDateFormat sdf_y = new SimpleDateFormat("yy");
	public static final SimpleDateFormat sdf_m = new SimpleDateFormat("MMMMMM");
	public static final SimpleDateFormat sdf_d = new SimpleDateFormat("dd");
	public static final SimpleDateFormat sdf = new SimpleDateFormat("dd,MMMMMM,yyyy");

}
