var icd = function(cytoscapeutil) {
	var _icd = function(projects, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap) {
		var html = "";
		for (var projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				var project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				var fileImplicitCrossModuleDependencyList = fileImplicitCrossModuleDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Index</th>";
				html += "<th>File1</th>";
				html += "<th>File2</th>";
				html += "<th>Co-Change Times</th>";
				html += "</tr>";
				var index = 1;
				for(var fileIndex in fileImplicitCrossModuleDependencyList) {
					if (fileImplicitCrossModuleDependencyList.hasOwnProperty(fileIndex)) {
						var fileImplicitCrossModuleDependency = fileImplicitCrossModuleDependencyList[fileIndex];
						html += "<tr>";
						html += "<td>" + index + "</td>";
						html += "<td><a href='/relation/file/" + fileImplicitCrossModuleDependency.node1.id + "' target='_blank'>" + fileImplicitCrossModuleDependency.node1.path + "</a></td>";
						html += "<td><a href='/relation/file/" + fileImplicitCrossModuleDependency.node2.id + "' target='_blank'>" + fileImplicitCrossModuleDependency.node2.path + "</a></td>";
						html += "<td>" + fileImplicitCrossModuleDependency.cochangeTimes + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";

				var packageImplicitCrossModuleDependencyList = packageImplicitCrossModuleDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Index</th>";
				html += "<th>Package1</th>";
				html += "<th>Package2</th>";
				html += "<th>Co-Change Times</th>";
				html += "</tr>";
				index = 1;
				for(var packageIndex in packageImplicitCrossModuleDependencyList) {
					if (packageImplicitCrossModuleDependencyList.hasOwnProperty(packageIndex)) {
						var packageImplicitCrossModuleDependency = packageImplicitCrossModuleDependencyList[packageIndex];
						html += "<tr>";
						html += "<td>" + index + "</td>";
						html += "<td>" + packageImplicitCrossModuleDependency.node1.directoryPath + "</td>";
						html += "<td>" + packageImplicitCrossModuleDependency.node2.directoryPath + "</td>";
						html += "<td>" + packageImplicitCrossModuleDependency.cochangeTimes + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
			}
		}
		$("#content").html(html);
	}
	
	var _save = function() {
		var set = function(projectId, icdMinFileCoChange, icdMinPackageCoChange) {
			$.ajax({
				type: "post",
				url: "/as/icd/cochange" + projectId
					+ "?icdMinFileCoChange=" + icdMinFileCoChange
					+ "?icdMinPackageCoChange=" + icdMinPackageCoChange,
				success: function(result) {
					if(result === true) {
						alert("修改成功");
					} else {
						alert("修改失败");
					}
				}
			})
		}
		$("#icdMinCoChangeSave").click(function() {
			var projectId;
			$("#logicalCouplingProjects").change(function() {
				projectId = $(this).val();
			})
			if($("#logicalCouplingProjects").val() != null) {
				projectId = $("#logicalCouplingProjects").val();
			}
			var icdMinFileCoChange = $("#icdMinFileCoChange").val();
			var icdMinPackageCoChange = $("#icdMinPackageCoChange").val();
			set(projectId, icdMinFileCoChange, icdMinPackageCoChange);
		})
	}

	let _get = function() {
		let getMinCoChange = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/icd/cochange/" + projectId,
				success: function(result) {
					console.log(result);
					$("#icdMinFileCoChange").val(result[0]);
					$("#icdMinPackageCoChange").val(result[1]);
				}
			})
		}
		$("#logicalCouplingProjects").change(function() {
			getMinCoChange($(this).val());
		})
		if($("#logicalCouplingProjects").val() != null) {
			getMinCoChange($("#logicalCouplingProjects").val());
		}
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		icd: function(projects, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap) {
			_icd(projects, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap);
		}
	}
}
