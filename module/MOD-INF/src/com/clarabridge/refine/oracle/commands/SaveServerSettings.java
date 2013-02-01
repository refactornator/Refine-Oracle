package com.clarabridge.refine.oracle.commands;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.clarabridge.refine.oracle.ServerSettings;
import com.clarabridge.refine.oracle.operations.SaveServerSettingsOperation;
import com.google.refine.commands.Command;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Project;
import com.google.refine.process.Process;
import com.google.refine.util.ParsingUtilities;

public class SaveServerSettings extends Command {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
      
      try {
          Project project = getProject(request);
          
          String jsonString = request.getParameter("oracle");
          JSONObject json = ParsingUtilities.evaluateJsonStringToObject(jsonString);
          
          ServerSettings settings = ServerSettings.reconstruct(json);
          AbstractOperation op = new SaveServerSettingsOperation(settings);
          
          Process process = op.createProcess(project, new Properties());
          performProcessAndRespond(request, response, project, process);
      } catch (Exception e) {
          respondException(response, e);
      }
  }
}
