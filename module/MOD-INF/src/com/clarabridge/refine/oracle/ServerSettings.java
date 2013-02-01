package com.clarabridge.refine.oracle;

import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.refine.model.OverlayModel;
import com.google.refine.model.Project;

public class ServerSettings implements OverlayModel {
	public String ip = "";
	public String port = "";
	public String db = "";
	
	public String username = "";
	public String password = "";
	
	public String table = "";
	
	public int currentRow = 0;
	
	public ArrayList<OracleColumn> oracleColumns = new ArrayList<OracleColumn>();

	@Override
	public void write(JSONWriter writer, Properties options) throws JSONException {
		writer.object();
			writer.key("ip");
			writer.value(ip);

			writer.key("port");
			writer.value(port);
		
			writer.key("db");
			writer.value(db);
		
			writer.key("username");
			writer.value(username);
		
			writer.key("password");
			writer.value(password);
			
			writer.key("table");
			writer.value(table);
			
			writer.key("currentRow");
			writer.value(currentRow);
			
			writer.key("columns");
			writer.array();
			for (OracleColumn column : oracleColumns) {
				writer.object();
				writer.key("name");
		    writer.value(column.oracleName);
		    
		    writer.key("datatype");
		    writer.value(column.datatype);
		    
		    writer.key("size");
		    writer.value(column.size);
		    writer.endObject();
			}
			writer.endArray();
			
		writer.endObject();
	}

	@Override
	public void dispose(Project project) {
	}

	@Override
	public void onAfterSave(Project project) {
	}

	@Override
	public void onBeforeSave(Project project) {
	}
	
  static public ServerSettings load(Project project, JSONObject obj) throws Exception {
	    return reconstruct(obj);
	}

	public static ServerSettings reconstruct(JSONObject json) throws JSONException{
    ServerSettings settings = new ServerSettings();
    
    if(json.has("ip")) {
    	settings.ip = json.getString("ip");
    } else {
    	settings.ip = "";
    }
    
    if(json.has("port")) {
    	settings.port = json.getString("port");
    } else {
    	settings.port = "";
    }
    
    if(json.has("db")) {
    	settings.db = json.getString("db");
    } else {
    	settings.db = "";
    }
    
    if(json.has("username")) {
    	settings.username = json.getString("username");
    } else {
    	settings.username = "";
    }
    
    if(json.has("password")) {
    	settings.password = json.getString("password");
    } else {
    	settings.password = "";
    }
    
    if(json.has("table")) {
    	settings.table = json.getString("table");
    } else {
    	settings.table = "";
    }
    
    if(json.has("currentRow")) {
    	settings.currentRow = json.getInt("currentRow");
    } else {
    	settings.currentRow = 0;
    }
    
    if(json.has("columns")) {
      JSONArray columns = json.getJSONArray("columns");
      int count = columns.length();
      
      for (int i = 0; i < count; i++) {
          JSONObject jsonColumn = columns.getJSONObject(i);
          OracleColumn column = new OracleColumn(i, 
          		jsonColumn.getString("name"), 
          		jsonColumn.getString("name"), 
          		jsonColumn.getString("datatype"), 
          		jsonColumn.getString("size"), 
          		true);
          settings.oracleColumns.add(column);
      }
    }
    
    return settings;
	}

}
