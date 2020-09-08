var similar = function(cytoscapeutil, files) {
	var _similar = function() {
		var html = "";
		html += "<table class='table table-bordered'>";
		html += "<tr>";
		html += "<th>File1</th>";
		html += "<th>Module1</th>";
		html += "<th>File2</th>";
		html += "<th>Module2</th>";
		html += "<th>clone value</th>";
		html += "<th>Co-change Times</th>";
		html += "</tr>";
		for(var fileIndex in files) {
			var file = files[fileIndex];
			console.log(file);
			html += "<tr>";
			html += "<td>" + file.node1.path + "</td>";
			html += "<td>" + file.module1.name + "</td>";
			html += "<td>" + file.node2.path + "</td>";
			html += "<td>" + file.module2.name + "</td>";
			html += "<td>" + file.value + "</td>";
			html += "<td>" + file.cochangeTimes + "</td>";
			html += "</tr>";
		}
		html += "</table>";
		
		$("#content").html(html);
	}
	
//	var _save = function() {
//		var set = function(icdMinCoChange) {
//			$.ajax({
//				type: "post",
//				url: "/as/icd/cochange?minCoChange=" + icdMinCoChange,
//				success: function(result) {
//					if(result == true) {
//						alert("修改成功");
//					} else {
//						alert("修改失败");
//					}
//				}
//			})
//		}
//		$("#icdMinCoChangeSave").click(function() {
//			var icdMinCoChange = $("#icdMinCoChange").val();
//			set(icdMinCoChange);
//		})
//	}
//	
//	var _get = function() {
//		$.ajax({
//			type: "get",
//			url: "/as/icd/cochange",
//			success: function(result) {
//				console.log(result);
//				$("#icdMinCoChange").val(result);
//			}
//		})
//	}
	
	return {
		init : function() {
			_similar();
//			_save();
//			_get();
		}
	}
}
