package com.clarabridge.refine.exporters;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;
import java.sql.*;

import com.clarabridge.refine.oracle.OracleColumn;
import com.clarabridge.refine.oracle.ServerSettings;
import com.google.refine.browsing.Engine;
import com.google.refine.browsing.FilteredRows;
import com.google.refine.browsing.RowVisitor;
import com.google.refine.exporters.*;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Project;
import com.google.refine.model.Row;

public class OracleExporter implements WriterExporter {
	ServerSettings settings;
	String ipAddress;
	String dbName;
	String port;
	
	String username;
	String password;
	
	String table;
	
	ArrayList<OracleColumn> oracleColumns;
  
	public OracleExporter() {
		System.out.println("Initializing Oracle Export");
	}

	@Override
	public String getContentType() {
		return "application/x-unknown";
	}

	@Override
	public void export(Project project, Properties options, Engine engine, Writer writer)
			throws IOException {
		System.out.println("Exporting to Oracle");

    RowVisitor visitor = new RowVisitor() {
    	Connection con = null;
    	PreparedStatement insertStatement;

        public boolean visit(Project project, int rowIndex, Row row) {
        	try {
            //Insert row into Oracle
        		insertStatement.clearParameters();
            
        		for (Column col : project.columnModel.columns) {
              int cellIndex = col.getCellIndex();
              Cell cell = row.cells.get(cellIndex);
              if (cell != null && cell.value != null) {
                Object v = cell.value;
                insertStatement.setObject(cellIndex + 1, v);
                
                //TODO: I THINK THIS IS HOW IT SHOULD WORK
              	/*if(v.getClass().toString().equals("class java.util.Date")) {
              		if(oracleColumns.get(cellIndex).datatype.equals("date")) {
              			insertStatement.setDate(cellIndex, (Date)v);
              		} else {
              			insertStatement.setObject(cellIndex, v.toString());
              		}
              	} else {
              		insertStatement.setObject(cellIndex + 1, v);
              	}*/
              } else {
              	insertStatement.setObject(cellIndex + 1, null);
              }
            }
        		
            synchronized (project) {
          		ServerSettings settings = (ServerSettings) project.overlayModels.get("oracleServerSettings");
          		settings.currentRow = rowIndex;
          		project.overlayModels.put("oracleServerSettings", settings);
            }
            
        		insertStatement.executeUpdate();
        	} catch(Exception e){
        		e.printStackTrace();
        		for (Column col : project.columnModel.columns) {
              int cellIndex = col.getCellIndex();
              if (cellIndex < row.cells.size()) {
                Cell cell = row.cells.get(cellIndex);
                if (cell != null && cell.value != null) {
                  System.out.print("cell " + cellIndex + " value: " + cell.value);
                }
              }
            }
        	}
          	
          return false;
        }

        @Override
        public void start(Project project) {
        	settings = (ServerSettings) project.overlayModels.get("oracleServerSettings");
        	ipAddress = settings.ip;
        	dbName = settings.db;
        	port = settings.port;
        	
        	username = settings.username;
        	password = settings.password;
        	
        	table = settings.table;
        	
        	oracleColumns = settings.oracleColumns;
        	
        	try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con=DriverManager.getConnection(
              "jdbc:oracle:thin:@"+ipAddress+":"+port+":"+dbName,
              username,
              password);
            
            /*int cellIndex = -1;
            for (Column col : project.columnModel.columns) {
              cellIndex = col.getCellIndex();
              String suspectedColumnType = "";
              
            	for(Row row : project.rows) {
            		Cell cell = row.cells.get(cellIndex);
            		
            		suspectedColumnType = "Number";
            		
                if (cell != null && cell.value != null) {
                  Object v = cell.value;
                  
                  if (v instanceof String) {
                  	String s = (String) v;
                  	if(s.length() > 4000) {
                  		suspectedColumnType = "Clob";
                  		columnTypes.add(suspectedColumnType);
                  		break;
                  	} else {
                  		suspectedColumnType = "Varchar2(4000)";
                  	}
                  } else if (v instanceof Double) {
										suspectedColumnType = "Double Precision";
									} else if (v instanceof Number) {
										suspectedColumnType = "Number";
									} else if (v instanceof Date) {
	              	 suspectedColumnType = "Date";
	                }
                }
            	}
            	if(suspectedColumnType != "Clob")
            		columnTypes.add(suspectedColumnType);
            }*/
            
            Boolean tableExists = false;
            Statement stmt = con.createStatement();
            ResultSet rs;
            
            rs = stmt.executeQuery("select count(*) table_count from user_tables where table_name='"+table.toUpperCase()+"'");
            while ( rs.next() ) {
            	if(rs.getInt("table_count") > 0) {
            		tableExists = true;
            	}
            }
            
            if(!tableExists) {
	            String createTableQuery = "CREATE TABLE " + table + " (";
	            int index = 0;
	          	for (OracleColumn column : oracleColumns) {
	            	createTableQuery += column.oracleName + " " + column.datatype;
	            	index++;
	
	            	if(column.datatype.equals("varchar2")) {
	            		createTableQuery += "(" + column.size + ")";
	            	}
	            	if(index < oracleColumns.size()) {
	            		createTableQuery += ", ";
	            	}
	            }
	          	createTableQuery += ")";
	          	System.out.println(createTableQuery);
	          	stmt.executeUpdate(createTableQuery);
            }
          	
        	} catch(Exception e){e.printStackTrace();}
        	
        	try{
          	String query = "insert into " + table;
            String columns = "(";
            String values = " values(";

            int index = 0;
            for (OracleColumn column : oracleColumns) {
            	columns += column.oracleName;
            	values += "?";
            	index++;
            	
            	if(index < oracleColumns.size()) {
            		columns += ", ";
            		values += ",";
            	}
            }
            
            columns += ")";
            values += ")";
            
            query += columns + values;
            
            System.out.println(query);
            
            insertStatement = con.prepareStatement(query); // create a statement
            
            System.out.println("Successfully Connected to Oracle!");
            
         } catch(Exception e){e.printStackTrace();}
        }

        @Override
        public void end(Project project) {
        	//close Oracle Connection
          try {
						con.close();
					}
					catch (SQLException e) {e.printStackTrace();}
					
        	System.out.println("Finished Exporting");
        }
    };

    FilteredRows filteredRows = engine.getAllFilteredRows();
    filteredRows.accept(project, visitor);
		
	}

}
