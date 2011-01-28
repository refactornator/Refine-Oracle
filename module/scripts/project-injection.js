/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

// This file is added to the /project page

var OracleExtension = { handlers: {} };

OracleExtension.handlers.editConnectionSettings = function() {
    new OracleConnectionDialog(theProject.overlayModels.oracleServerSettings);
};

OracleExtension.handlers.exportToTable = function() {
    var name = $.trim(theProject.metadata.name.replace(/\W/g, ' ')).replace(/\s+/g, '-');
    
    
    var frame = DialogSystem.createDialog();
    frame.addClass("dialog-busy");

    var body = $('<div>').attr('id', 'loading-message').appendTo(frame);
    $('<img>').attr("src", "images/large-spinner.gif").appendTo(body);
    $('<span>').text(" Exporting...").appendTo(body)
    $('<div id="progressBar">').appendTo(body);
    //$("#progressBar").progressBar();

    var level = DialogSystem.showDialog(frame);
    OracleExtension.handlers.checkProgress();
    
    var intervalID = 0;

    $.ajax({
        type: "POST",
        url: "/command/core/export-rows/" + name,
        data: { "engine" : JSON.stringify(ui.browsingEngine.getJSON()), 
        		"project" : theProject.id, 
        		"format" : "oracle" },
        dataType: "json",
        success: function (data) {
            clearInterval(intervalID);
            DialogSystem.dismissUntil(level - 1);
        }
    });
    
    intervalID = setInterval(OracleExtension.handlers.checkProgress, 10000);
    
    /*
    var form = document.createElement("form");
    $(form)
        .css("display", "none")
        .attr("method", "post")
        .attr("action", "/command/core/export-rows/" + name)
        .attr("target", "refine-export");

    $('<input />')
        .attr("name", "engine")
        .attr("value", JSON.stringify(ui.browsingEngine.getJSON()))
        .appendTo(form);
    $('<input />')
        .attr("name", "project")
        .attr("value", theProject.id)
        .appendTo(form);
    $('<input />')
        .attr("name", "format")
        .attr("value", "oracle")
        .appendTo(form);

    document.body.appendChild(form);

    window.open("about:blank", "refine-export");
    form.submit();
    
    document.body.removeChild(form);*/
};

OracleExtension.handlers.checkProgress = function() {
	Refine.reinitializeProjectData();
	var filtered = theProject.rowModel.filtered;
	var currentRow = theProject.overlayModels.oracleServerSettings.currentRow;
	
	var percentage = Math.floor(100 * parseInt(currentRow) / parseInt(filtered));
	$("#progressBar").html(percentage + "%");
	//$("#progressBar").progressBar(percentage);
}

ExtensionBar.addExtensionMenu({
    "id" : "oracle",
    "label" : "Oracle",
    "submenu" : [
        {
            "id" : "oracle/create-connection",
            label: "Create Oracle Connection",
            click: function() { OracleExtension.handlers.editConnectionSettings(); }
        }
    ]
});

MenuSystem.appendTo( 
    ExporterManager.MenuItems, 
    [ ], 
    [
		   {},
           { 
               "id" : "core/export-oracle", 
               "label":"Oracle Table", 
               "click": function() { OracleExtension.handlers.exportToTable(); } 
           } 
    ] 
);