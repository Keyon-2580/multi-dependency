var multiple = function(projects, files, cytoscapeutil) {
	var param = {
			cycle: true,
			hublike: true,
			logicCoupling: true,
			similar: true,
			unstable: true,
//			hierarchy: false,
//			godComponent: false,
			unused: false
	}
	
	function paramToRequestParam() {
		var str = "?cycle=" + param.cycle + 
			"&hublike=" + param.hublike + 
			"&logicCoupling=" + param.logicCoupling + 
			"&similar=" + param.similar + 
			"&unstable=" + param.unstable + 
//			"&hierarchy=" + param.hierarchy + 
//			"&godComponent=" + param.godComponent + 
			"&unused=" + param.unused;
		return str;
	}
	
	var _histogram = function(data, divId) {
		var histogramChart = echarts.init(document.getElementById(divId));
		var option = {
				dataZoom: [{
					type: 'slider',
					show: true,
					xAxisIndex: [0],
					left: '9%',
					bottom: -5,
					start: 0,
					end: 50
				}],
        	    tooltip: {
        	        trigger: 'axis',
        	        axisPointer: {
        	            type: 'shadow'
        	        }
        	    },
        	    legend: {
        	        data: ["All Files", 'Smell Files', 'Issue Files']
        	    },
        	    grid: {
        	        left: '3%',
        	        right: '4%',
        	        bottom: '3%',
        	        containLabel: true
        	    },
        	    xAxis: [{
        	            type: 'category',
        	            data: data.projects,
        	            axisLabel: {  
        	                interval:0,  
        	                rotate:40  
        	            }  
        	        }
        	    ],
        	    yAxis: [{
        	            type: 'value'
        	        }
        	    ],
        	    series: [{
        	            name: "All Files",
        	            type: 'bar',
        	            stack: 'allFiles',
        	            data: data.allFiles
        	        },{
        	            name: 'Smell Files',
        	            type: 'bar',
        	            stack: 'smellFiles',
        	            data: data.smellFiles
        	        },{
        	        	name: 'Issue Files',
        	        	type: 'bar',
        	        	stack: 'issueFiles',
        	        	data: data.issueFiles
        	        }
        	    ]
        	};
		histogramChart.setOption(option);
	}
	
	var _pie = function(project, pies, allFilesPieDivId, smellAndIssueFilesPieDivId, issuesDivId) {
		console.log(pies);
		var issuesPie = echarts.init(document.getElementById(issuesDivId));
		var allFilesPie = echarts.init(document.getElementById(allFilesPieDivId));
		var smellAndIssueFilesPie = echarts.init(document.getElementById(smellAndIssueFilesPieDivId));
		var issuesPieOption = {
				title: {
					text: 'Issues占比',
					left: 'center'
				},
				tooltip: {
					trigger: 'item',
					formatter: '{a} <br/>{b} : {c} ({d}%)'
				},
				legend: {
					orient: 'vertical',
					left: 'left',
					data: ['没有smellFiles关联的issue', '有smellFiles关联的Issue']
				},
				series: [
					{
						name: '文件',
						type: 'pie',
						radius: '55%',
						center: ['50%', '60%'],
						data: [
							{value: (pies.allIssues.length - pies.smellIssues.length), name: '没有smellFiles关联的issue'},
							{value: pies.smellIssues.length, name: '有smellFiles关联的Issue'}
							],
							emphasis: {
								itemStyle: {
									shadowBlur: 10,
									shadowOffsetX: 0,
									shadowColor: 'rgba(0, 0, 0, 0.5)'
								}
							}
					}
					]
		}
		issuesPie.setOption(issuesPieOption);
		var allFilesOption = {
				title: {
					text: '文件占比',
					left: 'center'
				},
				tooltip: {
					trigger: 'item',
					formatter: '{a} <br/>{b} : {c} ({d}%)'
				},
				legend: {
					orient: 'vertical',
					left: 'left',
					data: ['normalFiles', 'onlyIssueFiles', 'issueAndSmellFiles', 'onlySmellFiles']
				},
				series: [
					{
						name: '文件',
						type: 'pie',
						radius: '55%',
						center: ['50%', '60%'],
						data: [
							{value: pies.normalFiles.length, name: 'normalFiles'},
							{value: pies.onlyIssueFiles.length, name: 'onlyIssueFiles'},
							{value: pies.issueAndSmellFiles.length, name: 'issueAndSmellFiles'},
							{value: pies.onlySmellFiles.length, name: 'onlySmellFiles'}
							],
							emphasis: {
								itemStyle: {
									shadowBlur: 10,
									shadowOffsetX: 0,
									shadowColor: 'rgba(0, 0, 0, 0.5)'
								}
							}
					}
					]
		};
		allFilesPie.setOption(allFilesOption);
		
		var warnFilesOption = {
				title: {
					text: '文件占比',
					left: 'center'
				},
				tooltip: {
					trigger: 'item',
					formatter: '{a} <br/>{b} : {c} ({d}%)'
				},
				legend: {
					orient: 'vertical',
					left: 'left',
					data: ['onlyIssueFiles', 'issueAndSmellFiles', 'onlySmellFiles']
				},
				series: [
					{
						name: '文件',
						type: 'pie',
						radius: '55%',
						center: ['50%', '60%'],
						data: [
							{value: pies.onlyIssueFiles.length, name: 'onlyIssueFiles'},
							{value: pies.issueAndSmellFiles.length, name: 'issueAndSmellFiles'},
							{value: pies.onlySmellFiles.length, name: 'onlySmellFiles'}
							],
							emphasis: {
								itemStyle: {
									shadowBlur: 10,
									shadowOffsetX: 0,
									shadowColor: 'rgba(0, 0, 0, 0.5)'
								}
							}
					}
					]
		};
		smellAndIssueFilesPie.setOption(warnFilesOption);
	}
	
	var _multiple = function() {
		var html = "";
		console.log(projects);
		for(var i = 0; i < projects.length; i++) {
			var project = projects[i];
			html += "<div>";
				html += "<div>";
				html += "<h4><a target='_blank' href='/as/multiple/project/" + project.id + paramToRequestParam() + "'>" + project.name + " (" + project.language + ") </h4></a>";
				html += "</div>";
				html += "<div class='col-sm-12 row'>";
					html += "<div class='col-sm-4'>";
					html += "<div id='allFilesPie_" + project.id + "' style='height: 400px;'></div>";
					html += "</div>";
					html += "<div class='col-sm-4'>";
					html += "<div id='issueFilesPie_" + project.id + "' style='height: 400px;'></div>";
					html += "</div>";
					html += "<div class='col-sm-4'>";
					html += "<div id='issuesPie_" + project.id + "' style='height: 400px;'></div>";
					html += "</div>";
				html += "</div>";
				html += "<div class=''>";
				html += "<div id='circle_" + project.id + "'></div>";
				html += "</div>";
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>id</th>";
				html += "<th>File</th>";
				html += "<th>cycle</th>";
				html += "<th>hublike</th>";
				html += "<th>unstable</th>";
				html += "<th>logic coupling</th>";
				html += "<th>simiar</th>";
//				html += "<th>cyclic hierarchy</th>";
//				html += "<th>god component</th>";
//				html += "<th>unused component</th>";
				html += "<th>page rank</th>";
				html += "</tr>";
				for(var j = 0 ; j < files[project.id].length; j++) {
					var value = files[project.id][j];
					html += "<tr>";
					html += "<td>" + value.file.id + "</td>";
					html += "<td><a target='_blank' href='/relation/file/" + value.file.id + "'>" + value.file.path + "</a></td>";
					html += "<td>" + (value.cycle == true ? "T" : "") + "</td>";
					html += "<td>" + (value.hublike == true ? "T" : "") + "</td>";
					html += "<td>" + (value.unstable == true ? "T" : "") + "</td>";
					html += "<td>" + (value.logicCoupling == true ? "T" : "") + "</td>";
					html += "<td>" + (value.similar == true ? "T" : "") + "</td>";
//					html += "<td>" + (value.cyclicHierarchy == true ? "T" : "") + "</td>";
//					html += "<td>" + (value.god == true ? "T" : "") + "</td>";
//					html += "<td>" + (value.unused == true ? "T" : "") + "</td>";
					html += "<td>" + value.file.score + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				html += "</div>";
			html += "</div>";
		}
		$("#table").html(html);
		var data = {
				"children": [{
					"children": [{
							"size": 1000,
							"name": "TraverserRewrite.java"
						},{
							"size": 1000,
							"name": "CharMatcherRewrite.java"
						}],
						"name": "default"
				},{
					"children": [{
							"size": 1000,
							"name": "XmlEscapersTest.java"
					}],
					"name": "com.google.common.xml"
				}],
				"name": "google__fdse__guava"
	}
//	projectToGraph(data, "circle_0");
		$.ajax({
			type: "get",
			url: "/as/issue/circle",
			data: param,
			success: function(result) {
				console.log(result);
				for(var i = 0; i < projects.length; i++) {
					var data = {};
					var project = projects[i];
					var minIssueSize = 0;
					var maxIssueSize = 0;
					var circles = result[project.id];
					for(var j = 0; j < circles.length; j++) {
					}
					
				}
			}
		});
		$.ajax({
			type: "get",
			url: "/as/issue/pie",
			data : param,
			success: function(result) {
				console.log(result);
				for(var i = 0; i < projects.length; i++) {
					var project = projects[i];
					_pie(project, result[project.id], "allFilesPie_" + project.id, "issueFilesPie_" + project.id, "issuesPie_" + project.id);
				}
			}
		});
		$.ajax({
			type: "get",
			url: "/as/multiple/histogram",
			success: function(result) {
				console.log(result);
				var allFiles = [];
				var smellFiles = [];
				var issueFiles = [];
				var projectsName = [];
				for(var i = 0; i < projects.length; i++) {
					var project = projects[i];
					allFiles[i] = result[project.id].allFilesCount;
					smellFiles[i] = result[project.id].smellFilesCount;
					issueFiles[i] = result[project.id].issueFilesCount;
					projectsName[i] = project.name;
				}
				var data = {
					projects: projectsName,
					allFiles: allFiles,
					smellFiles: smellFiles,
					issueFiles: issueFiles
				};
				_histogram(data, "graph_version");
			}
		})
		
	}
	
	return {
		init : function() {
			_multiple();
		}
	}
}
