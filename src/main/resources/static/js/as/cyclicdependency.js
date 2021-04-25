let cyclicDependency = function() {
	let _cyclicDependency = function(projects, typeCyclicDependencyMap, fileCyclicDependencyMap, packageCyclicDependencyMap) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let typeCyclicDependencyList = typeCyclicDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Types</th>";
				html += "</tr>";
				let index = 1;
				for(let typeIndex in typeCyclicDependencyList) {
					if (typeCyclicDependencyList.hasOwnProperty(typeIndex)) {
						let typeCyclicDependency = typeCyclicDependencyList[typeIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + typeCyclicDependency.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < typeCyclicDependency.components.length; i++) {
							html += typeCyclicDependency.components[i].name + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";

				let fileCyclicDependencyList = fileCyclicDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Files</th>";
				html += "</tr>";
				index = 1;
				for(let fileIndex in fileCyclicDependencyList) {
					if (fileCyclicDependencyList.hasOwnProperty(fileIndex)) {
						let fileCyclicDependency = fileCyclicDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileCyclicDependency.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < fileCyclicDependency.components.length; i++) {
							html += fileCyclicDependency.components[i].path + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}

				let packageCyclicDependencyList = packageCyclicDependencyMap[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Packages</th>";
				html += "</tr>";
				index = 1;
				for(let packageIndex in packageCyclicDependencyList) {
					if (packageCyclicDependencyList.hasOwnProperty(packageIndex)) {
						let packageCyclicDependency = packageCyclicDependencyList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageCyclicDependency.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < packageCyclicDependency.components.length; i++) {
							html += packageCyclicDependency.components[i].name + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
			}
		}
		$("#content").html(html);
	}
	
	return {
		cyclicDependency: function(projects, typeCyclicDependencyMap, fileCyclicDependencyMap, packageCyclicDependencyMap) {
			_cyclicDependency(projects, typeCyclicDependencyMap, fileCyclicDependencyMap, packageCyclicDependencyMap);
		}
	}
}
