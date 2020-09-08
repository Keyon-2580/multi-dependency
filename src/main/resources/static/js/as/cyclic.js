var cyclic = function(cytoscapeutil) {
	var charts = echarts.init(document.getElementById('cycleGraph'));
	var showTable = function(projects, files, packages) {
		console.log(files);
		console.log(packages);
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var cyclicFiles = files[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Partition</th>";
			html += "<th>Files</th>";
			html += "</tr>";
			for(var cycleIndex in cyclicFiles) {
				var cycle = cyclicFiles[cycleIndex];
				console.log(cycleIndex);
				console.log(cycle);
				html += "<tr>";
				html += "<td><a href='#cycleGraph' class='cycleFiles' project='" + project.id + "' partition='" + cycle.partition + "'>" + cycle.partition + "</a></td>";
				html += "<td>";
				for(var i = 0; i < cycle.components.length; i++) {
					html += "<a target='_blank' href='/relation/file/" + cycle.components[i].id + "'>" + cycle.components[i].path + "</a><br/>";
				}
				html += "</td>";
				html += "</tr>";
			}
			
			var cyclicPackages = packages[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Partition</th>";
			html += "<th>Packages</th>";
			html += "</tr>";
			for(var packageIndex in cyclicPackages) {
				var pcks = cyclicPackages[packageIndex];
				html += "<tr>";
				html += "<td>" + pcks.partition + "</td>";
				html += "<td>";
				for(var i = 0; i < pcks.components.length; i++) {
					html += pcks.components[i].directoryPath + "<br/>";
				}
				html += "</td>";
				html += "</tr>";
			}
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	var _cyclic = function(projects, files, packages) {
		showTable(projects, files, packages);
		$(".cycleFiles").click(function() {
			var partition = $(this).attr("partition");
			var projectId = $(this).attr("project");
			var cycleFiles = files[projectId][partition];
			console.log(cycleFiles);
			showGraph(cycleFiles);
		});
	}
	
	var showGraph = function(cycleFiles) {
		console.log(cycleFiles);
		var categories = [];
		for(var i = 0; i < cycleFiles.groups.length; i++) {
			categories[i] = {
				name : "module_" + cycleFiles.groups[i].id
			};
		}
		var nodes = [];
		for(var i = 0; i < cycleFiles.components.length; i++) {
			var file = cycleFiles.components[i];
			nodes[i] = {
				name: file.name,
				id: file.id + "",
				category: "module_" + cycleFiles.componentToGroup[file.id].id,
				draggable: true
			}
		}
		console.log(nodes);
		var links = [];
		for(var i = 0; i < cycleFiles.relations.length; i++) {
			var relation = cycleFiles.relations[i];
			links[i] = {
				source: relation.startNode.id + "",
				target: relation.endNode.id + "",
				weight: 1,
				name: "dependsOn"
			}
		}
		console.log(links);
		var option = {
				title: {
					text: 'cycle files',
					top: 'top',
					left: 'left'
				},
				tooltip: {},
				animation: false,
				legend: [{
		            // selectedMode: 'single',
		            data: categories.map(function (a) {
		                return a.name;
		            })
		        }],
				series : [
					{
						name: 'cycle',
						type: 'graph',
						layout: 'force',
						data: nodes,
						links: links,
						categories: categories,
						label: {
							position: 'right'
						},
						roam: true,
						edgeSymbol: ['arrow']
					}
				]
		}
		charts.setOption(option);
	}
	
	return {
		cyclic: function(projects, files, packages) {
			_cyclic(projects, files, packages);
		}
	}
}
