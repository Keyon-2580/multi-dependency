let implicitCrossModuleDependency = function() {
	let _implicitCrossModuleDependency = function(projects, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap) {
		let html = "";
		for (let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let fileImplicitCrossModuleDependencyList = fileImplicitCrossModuleDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>File1</th>";
				html += "<th>File2</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-Change Times</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileImplicitCrossModuleDependencyList) {
					if (fileImplicitCrossModuleDependencyList.hasOwnProperty(fileIndex)) {
						let fileImplicitCrossModuleDependency = fileImplicitCrossModuleDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td><a href='/relation/file/" + fileImplicitCrossModuleDependency.node1.id + "' target='_blank'>" + fileImplicitCrossModuleDependency.node1.path + "</a></td>";
						html += "<td><a href='/relation/file/" + fileImplicitCrossModuleDependency.node2.id + "' target='_blank'>" + fileImplicitCrossModuleDependency.node2.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileImplicitCrossModuleDependency.cochangeTimes + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";

				let packageImplicitCrossModuleDependencyList = packageImplicitCrossModuleDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>Package1</th>";
				html += "<th>Package2</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-Change Times</th>";
				html += "</tr>";
				index = 1;
				for(let packageIndex in packageImplicitCrossModuleDependencyList) {
					if (packageImplicitCrossModuleDependencyList.hasOwnProperty(packageIndex)) {
						let packageImplicitCrossModuleDependency = packageImplicitCrossModuleDependencyList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td>" + packageImplicitCrossModuleDependency.node1.directoryPath + "</td>";
						html += "<td>" + packageImplicitCrossModuleDependency.node2.directoryPath + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageImplicitCrossModuleDependency.cochangeTimes + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
			}
		}
		$("#content").html(html);
	}

	let _save = function() {
		let setProjectMinCoChange = function(projectId, minFileCoChange, minPackageCoChange) {
			$.ajax({
				type: "post",
				url: "/as/icd/cochange/" + projectId
					+ "?minFileCoChange=" + minFileCoChange
					+ "&minPackageCoChange=" + minPackageCoChange,
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
			let projectId;
			$("#logicalCouplingProjects").change(function() {
				projectId = $(this).val();
			})
			if($("#logicalCouplingProjects").val() != null) {
				projectId = $("#logicalCouplingProjects").val();
			}
			let minFileCoChange = $("#icdMinFileCoChange").val();
			let minPackageCoChange = $("#icdMinPackageCoChange").val();
			setProjectMinCoChange(projectId, minFileCoChange, minPackageCoChange);
		})
	}

	let _get = function() {
		let getProjectMinCoChange = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/icd/cochange/" + projectId,
				success: function(result) {
					$("#icdMinFileCoChange").val(result[0]);
					$("#icdMinPackageCoChange").val(result[1]);
				}
			})
		}
		$("#logicalCouplingProjects").change(function() {
			getProjectMinCoChange($(this).val());
		})
		if($("#logicalCouplingProjects").val() != null) {
			getProjectMinCoChange($("#logicalCouplingProjects").val());
		}
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		implicitCrossModuleDependency: function(projects, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap) {
			_implicitCrossModuleDependency(projects, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap);
		}
	}
}
