var icd = function(cytoscapeutil) {
	var _icd = function(files) {
		var html = "";
		html += "<table class='table table-bordered'>";
		html += "<tr>";
		html += "<th>File1</th>";
		html += "<th>File2</th>";
		html += "<th>Co-change Times</th>";
		html += "</tr>";
		for(var fileIndex in files) {
			var file = files[fileIndex];
			console.log(file);
			html += "<tr>";
			html += "<td width='40%'>" + file.file1.path + "</td>";
			html += "<td width='40%'>" + file.file2.path + "</td>";
			html += "<td width='20%'>" + file.cochangeTimes + "</td>";
			html += "</tr>";
		}
		html += "</table>";
		
		$("#content").html(html);
	}
	
	var _save = function() {
		var set = function(icdMinCoChange) {
			$.ajax({
				type: "post",
				url: "/as/icd/cochange?minCoChange=" + icdMinCoChange,
				success: function(result) {
					if(result == true) {
						alert("修改成功");
					} else {
						alert("修改失败");
					}
				}
			})
		}
		$("#icdMinCoChangeSave").click(function() {
			var icdMinCoChange = $("#icdMinCoChange").val();
			set(icdMinCoChange);
		})
	}
	
	var _get = function() {
		$.ajax({
			type: "get",
			url: "/as/icd/cochange",
			success: function(result) {
				console.log(result);
				$("#icdMinCoChange").val(result);
			}
		})
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		icd: function(files) {
			_icd(files);
		}
	}
}
