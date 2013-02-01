package com.clarabridge.refine.oracle;

public class OracleColumn {
	public int index;
	public String originalName;
	public String oracleName;
	public String datatype;
	public String size;
	public boolean enabled;
	
	public OracleColumn() {
		
	}
	
	public OracleColumn(int index, String originalName, String oracleName, String datatype, String size, boolean enabled) {
		this.index = index;
		this.originalName = originalName;
		this.oracleName = oracleName;
		this.datatype = datatype;
		this.size = size;
		this.enabled = enabled;
	}
	
}
