package com.clarabridge.refine.oracle.operations;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.clarabridge.refine.oracle.ServerSettings;
import com.google.refine.history.Change;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.operations.OperationRegistry;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.Pool;

public class SaveServerSettingsOperation extends AbstractOperation {
	final protected ServerSettings _settings;
	
  static public AbstractOperation reconstruct(Project project, JSONObject obj) throws Exception {
	    return new SaveServerSettingsOperation(
	        ServerSettings.reconstruct(obj.getJSONObject("oracle"))
	    );
	}
	
	public SaveServerSettingsOperation(ServerSettings settings) {
		_settings = settings;
	}

	@Override
	public void write(JSONWriter writer, Properties options) throws JSONException {
		//System.out.println("SaveServerSettingsOperation - write");
    writer.object();
    writer.key("op"); writer.value(OperationRegistry.s_opClassToName.get(this.getClass()));
    writer.key("description"); writer.value(getBriefDescription(null));
    writer.key("oracle"); _settings.write(writer, options);
    writer.endObject();
	}
	
  protected String getBriefDescription(Project project) {
	    return "Save Oracle server settings";
	}
  
  @Override
  protected HistoryEntry createHistoryEntry(Project project, long historyEntryID) throws Exception {
  	//System.out.println("SaveServerSettingsOperation - createHistoryEntry");
      String description = "Save Oracle server settings";

      Change change = new ServerSettingsChange(_settings);

      return new HistoryEntry(historyEntryID, project, description, SaveServerSettingsOperation.this, change);
  }
  
  static public class ServerSettingsChange implements Change {
	    final protected ServerSettings _newSettings;
	    protected ServerSettings _oldSettings;
	
	    public ServerSettingsChange(ServerSettings settings) {
	    	_newSettings = settings;
	    }
	
	    public void apply(Project project) {
	    	//System.out.println("SaveServerSettingsOperation - apply");
	        synchronized (project) {
	        	_oldSettings = (ServerSettings) project.overlayModels.get("oracleServerSettings");
	
	          project.overlayModels.put("oracleServerSettings", _newSettings);
	        }
	    }
	
	    public void revert(Project project) {
	    	//System.out.println("SaveServerSettingsOperation - revert");
	        synchronized (project) {
	            if (_oldSettings == null) {
	                project.overlayModels.remove("oracleServerSettings");
	            } else {
	                project.overlayModels.put("oracleServerSettings", _oldSettings);
	            }
	        }
	    }
	
	    public void save(Writer writer, Properties options) throws IOException {
	    	//System.out.println("SaveServerSettingsOperation - save");
	        writer.write("newOracleSettings="); 
	        writeServerSettings(_newSettings, writer); 
	        writer.write('\n');
	        
	        writer.write("oldOracleSettings="); 
	        writeServerSettings(_oldSettings, writer); 
	        writer.write('\n');
	        
	        writer.write("/ec/\n"); // end of change marker
	    }
	
	    static public Change load(LineNumberReader reader, Pool pool) throws Exception {
	    	//System.out.println("SaveServerSettingsOperation - load");
	        ServerSettings oldSettings = null;
	        ServerSettings newSettings = null;
	
	        String line;
	        while ((line = reader.readLine()) != null && !"/ec/".equals(line)) {
	        	//System.out.println(line);
	            int equal = line.indexOf('=');
	            CharSequence field = line.subSequence(0, equal);
	            String value = line.substring(equal + 1);
	
	            if ("oldOracleSettings".equals(field) && value.length() > 0) {
	                oldSettings = ServerSettings.reconstruct(ParsingUtilities.evaluateJsonStringToObject(value));
	            } else if ("newOracleSettings".equals(field) && value.length() > 0) {
	                newSettings = ServerSettings.reconstruct(ParsingUtilities.evaluateJsonStringToObject(value));
	            }
	        }
	
	        ServerSettingsChange change = new ServerSettingsChange(newSettings);
	        change._oldSettings = oldSettings;
	
	        return change;
	    }
	
	    static protected void writeServerSettings(ServerSettings p, Writer writer) throws IOException {
	    	//System.out.println("SaveServerSettingsOperation - writeServerSettings");
	        if (p != null) {
	            JSONWriter jsonWriter = new JSONWriter(writer);
	            try {
	                p.write(jsonWriter, new Properties());
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}

}
