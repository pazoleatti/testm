package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Вид декларации.
 * @author dsultanbekov
 */
@Getter @Setter @ToString
public class DeclarationType implements Serializable {
	private static final long serialVersionUID = 1L;

	//РНУ_НДФЛ (первичная)
	public final static int NDFL_PRIMARY = 100;
	//РНУ_НДФЛ (консолидированная)
	public final static int NDFL_CONSOLIDATE = 101;
	//6-НДФЛ
	public final static int NDFL_6 = 103;
	public final static String NDFL_6_NAME = "6-НДФЛ";
	//2-НДФЛ (1)
	public final static int NDFL_2_1 = 102;
	public final static String NDFL_2_1_NAME = "2-НДФЛ (1)";
	//2-НДФЛ (2)
	public final static int NDFL_2_2 = 104;
	public final static String NDFL_2_2_NAME = "2-НДФЛ (2)";
	
	private int id;
	private String name;
    private VersionedObjectStatus status;
    private Integer versionsCount;

}
