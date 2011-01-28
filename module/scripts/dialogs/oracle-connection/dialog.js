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

var OracleConnection = {};

OracleConnection._cleanName = function(s) {
    s = s.replace(/\W/g, " ");
    s = s.replace(/\s+/g, "_");
    s = s.substr(0, 29);
    s = s.toUpperCase();
    return s;
};

OracleConnection._detectDatatype = function(cellIndex) {
	detectedColumnType = "number";
	
	for(rowIndex in theProject.rowModel.rows) {
		row = theProject.rowModel.rows[rowIndex];
		cell = row.cells[cellIndex];

		if (cell != null && cell.v != null) {
			v = cell.v;

			if (typeof v == 'string') {
				if(v.length > 4000) {
					detectedColumnType = "clob";
					break;
				} else {
					detectedColumnType = "varchar2";
				}
			} else if (typeof v == 'number') {
				detectedColumnType = "number";
			}
		}
	}

	return detectedColumnType;
};

OracleConnectionDialog._changeColumnType = function(columnIndex) {
	if($("#selectDatatype" + columnIndex).val() == 'varchar2') {
		$("#oracleColumnSize" + columnIndex).val(4000);
		$("#oracleColumnSize" + columnIndex).fadeIn()
	} else {
		$("#oracleColumnSize" + columnIndex).val();
		$("#oracleColumnSize" + columnIndex).fadeOut();
	}
};

OracleConnectionDialog._changeColumnName = function(columnIndex) {
	var currentColumnName = $("#oracleColumnName"+columnIndex).val();
	var cleanedColumnName = OracleConnection._cleanName(currentColumnName);
	$("#oracleColumnName"+columnIndex).val(cleanedColumnName);
};

function OracleConnectionDialog(settings) {
    this._hasUnsavedChanges = false;
    
    this._createDialog();

	if(settings == undefined) {
    	this._reset(settings);
	} else {
		this._load(settings);
	}
}

OracleConnectionDialog.prototype._load = function(settings) {
	var elmts = this._elmts;
	
	if(typeof settings  !== "undefined") {
		if('ip' in settings) {
			elmts.ip[0].value = settings.ip;
		}
		if('port' in settings) {
			elmts.port[0].value = settings.port;
		}
		if('db' in settings) {
			elmts.db[0].value = settings.db;
		}
	
		if('username' in settings) {
			elmts.username[0].value = settings.username;
		}
		if('password' in settings) {
			elmts.password[0].value = settings.password;
		}
		if('table' in settings) {
			elmts.table[0].value = settings.table;
		}
		
		if('columns' in settings) {
			for(columnIndex in settings.columns) {
				column = settings.columns[columnIndex];
				
				//TODO: GET RID OF THIS DUPLICATED CODE!!
				var tableRow = '<tr>';
				tableRow += '<td>' + column.name + '</td>';
				tableRow += '<td><input type="text" name="oracleColumnName" onchange="OracleConnectionDialog._changeColumnName('+columnIndex+')" id="oracleColumnName'+columnIndex+'" value="' + column.name + '"></input></td>';
				tableRow += "<td> \
				<select id='selectDatatype"+columnIndex+"' onchange='OracleConnectionDialog._changeColumnType("+columnIndex+")'> \
				  <option value='varchar2'>Varchar2</option> \
				  <option value='number'>Number</option> \
				  <option value='clob'>Clob</option> \
				</select> \
				</td>";
				tableRow += '<td><input type="text" name="size" id="oracleColumnSize'+columnIndex+'" size="1" /></td>';
				tableRow += '</tr>';
				this._oracleColumns.append(tableRow);

				var datatype = column.datatype;
				$("#selectDatatype"+columnIndex).val(datatype);
				if(datatype == "varchar2") {
					$("#oracleColumnSize"+columnIndex).val(column.size);
				} else {
					$("#oracleColumnSize"+columnIndex).hide();
				}
			}
		}
	}
}

OracleConnectionDialog.prototype._reset = function(settings) {
	var elmts = this._elmts;
	
	elmts.ip[0].value = "";
	elmts.port[0].value = "1521";
	elmts.db[0].value = "ORCL";
	
	elmts.username[0].value = "";
	elmts.password[0].value = "";
	
	elmts.table[0].value = OracleConnection._cleanName(theProject.metadata.name);
	
	this._oracleColumns.empty();
	for (i in theProject.columnModel.columns) {
		var tableRow = '<tr>';
		tableRow += '<td>' + theProject.columnModel.columns[i].name + '</td>';
		tableRow += '<td><input type="text" name="oracleColumnName" onchange="OracleConnectionDialog._changeColumnName('+columnIndex+')" id="oracleColumnName'+i+'" value="' + OracleConnection._cleanName(theProject.columnModel.columns[i].name) + '"></input></td>';
		tableRow += "<td> \
		<select id='selectDatatype"+i+"' onchange='OracleConnectionDialog._changeColumnType("+columnIndex+")'> \
		  <option value='varchar2'>Varchar2</option> \
		  <option value='number'>Number</option> \
		  <option value='clob'>Clob</option> \
		</select> \
		</td>";
		tableRow += '<td><input type="text" name="size" id="oracleColumnSize'+i+'" size="1" /></td>';
		tableRow += '</tr>';
		this._oracleColumns.append(tableRow);
		
		var datatype = OracleConnection._detectDatatype(i);
		$("#selectDatatype"+i).val(datatype);
		if(datatype == "varchar2") {
			$("#oracleColumnSize"+i).val(4000);
		} else {
			$("#oracleColumnSize"+i).hide();
		}
	}
	
};

OracleConnectionDialog.prototype._save = function() {
    var self = this;
    var oracle = this.getJSON();
    
    Refine.postProcess(
        "oracle",
        "save-server-settings",
        {},
        { oracle: JSON.stringify(oracle) },
        {},
        {   
            onDone: function() {
				theProject.overlayModels.oracleServerSettings = oracle;
            }
        }
    );
};

OracleConnectionDialog.prototype._createDialog = function() {
    var self = this;
    var frame = $(DOM.loadHTML("oracle", "scripts/dialogs/oracle-connection/schema-alignment-dialog.html"));
    var elmts = this._elmts = DOM.bind(frame);
    
    this._level = DialogSystem.showDialog(frame);
    
    var dismiss = function() {
        DialogSystem.dismissUntil(self._level - 1);
    };
    
    elmts.saveButton.click(function() {
        self._save();
    });
    elmts.saveAndLoadButton.click(function() {
        self._save(function() {
            dismiss();
        });
    });
    elmts.resetButton.click(function() {
        self._reset(null);
    });
    elmts.closeButton.click(function() {
        if (!self._hasUnsavedChanges || window.confirm("There are unsaved changes. Close anyway?")) {
            dismiss();
        }
    });
    
    this._canvas = $(".schema-alignment-dialog-canvas");
	this._oracleColumns = $('#oracleColumnsTable');
};

OracleConnectionDialog.prototype.getJSON = function() {
	var elmts = this._elmts;
	
	oracle = { 
		'ip': elmts.ip[0].value,
		'port': elmts.port[0].value,
		'db': elmts.db[0].value,
		'username': elmts.username[0].value,
		'password': elmts.password[0].value,
		'table': elmts.table[0].value
	};
	
	oracle.columns = [];
	
	oracleColumnsTable = elmts.oracleColumnsTable[0]
	rows = oracleColumnsTable.getElementsByTagName("tr");
	
	$(rows).each(function(index) {
		row = $(this)[0];
		
		column = {};
		column['name'] = row.children[1].children[0].value;
		column['datatype'] = row.children[2].children[0].value;
		column['size'] = row.children[3].children[0].value;
		
		oracle.columns.push(column);
	});
	
	return oracle;
};
