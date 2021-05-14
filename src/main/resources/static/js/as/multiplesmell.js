let multipleSmell = function(project, files, cytoscapeutil) {
	let param = {
			cycle: true,
			hublike: true,
			logicCoupling: true,
			similar: true,
			unstable: true,
//			hierarchy: false,
//			godComponent: false,
			unused: true,
			unutilized: false
	}
	
	function paramToRequestParam() {
		let str = "?cycle=" + param.cycle + 
			"&hublike=" + param.hublike + 
			"&logicCoupling=" + param.logicCoupling + 
			"&similar=" + param.similar + 
			"&unstable=" + param.unstable + 
//			"&hierarchy=" + param.hierarchy + 
//			"&godComponent=" + param.godComponent + 
			"&unused=" + param.unused + 
			"&unutilized=" + param.unutilized;
		return str;
	}
	
	let _histogram = function(data, divId) {
		let histogramChart = echarts.init(document.getElementById(divId));
		let option = {
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
	
	let _pie = function(project, pies, allFilesPieDivId, smellAndIssueFilesPieDivId, issuesDivId) {
		let issuesPie = echarts.init(document.getElementById(issuesDivId));
		let allFilesPie = echarts.init(document.getElementById(allFilesPieDivId));
		let smellAndIssueFilesPie = echarts.init(document.getElementById(smellAndIssueFilesPieDivId));
		let issuesPieOption = {
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
					data: ['无Smell Files关联的Issues', '有Smell File关联的Issues']
				},
				series: [
					{
						name: '文件',
						type: 'pie',
						radius: '55%',
						center: ['50%', '60%'],
						data: [
							{value: (pies.allIssues.length - pies.smellIssues.length), name: '无Smell Files关联的Issues'},
							{value: pies.smellIssues.length, name: '有Smell File关联的Issues'}
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
		let allFilesOption = {
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
		
		let warnFilesOption = {
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
	
	let _multipleSmell = function() {
		if (project != null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4><a target='_blank' href='/as/multiple/project/" + project.id + paramToRequestParam() + "'>" + project.name + " (" + project.language + ") </h4></a>";
			html += "</div>";
			html += "<div  style='width: 100%'>";
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
			html += "<div style='width: 100%'>";
			html += "<div id='circle_" + project.id + "'></div>";
			html += "</div>";
			html += "<div>";
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th style='text-align: center; vertical-align: middle'>ID</th>";
			html += "<th>File</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Cyclic Dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Hub-Like Dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Unstable dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Implicit Cross Module Dependency</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Unutilized Abstraction</th>";
			html += "<th style='text-align: center; vertical-align: middle'>Unused Include</th>";
			html += "</tr>";
			for(let j = 0 ; j < files[project.id].length; j++) {
				let value = files[project.id][j];
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>" + value.file.id + "</td>";
				html += "<td><a target='_blank' href='/relation/file/" + value.file.id + "'>" + value.file.path + "</a></td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.cycle === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.hublike === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.unstable === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.logicCoupling === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.unutilized === true ? "T" : "") + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + (value.unused === true ? "T" : "") + "</td>";
				html += "</tr>";
			}
			html += "</table>";
			html += "</div>";
			html += "</div>";
			$("#table").html(html);
		}
		let data = {
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
				for(let i = 0; i < projects.length; i++) {
					let data = {};
					let project = projects[i];
					let minIssueSize = 0;
					let maxIssueSize = 0;
					let circles = result[project.id];
					for(let j = 0; j < circles.length; j++) {
					}
					
				}
			}
		});
		$.ajax({
			type: "get",
			url: "/as/issue/pie",
			data : param,
			success: function(result) {
				for(let i = 0; i < projects.length; i++) {
					let project = projects[i];
					_pie(project, result[project.id], "allFilesPie_" + project.id, "issueFilesPie_" + project.id, "issuesPie_" + project.id);
				}
			}
		});
		$.ajax({
			type: "get",
			url: "/as/multiple/histogram",
			success: function(result) {
				let allFiles = [];
				let smellFiles = [];
				let issueFiles = [];
				let projectsName = [];
				for(let i = 0; i < projects.length; i++) {
					let project = projects[i];
					allFiles[i] = result[project.id].allFilesCount;
					smellFiles[i] = result[project.id].smellFilesCount;
					issueFiles[i] = result[project.id].issueFilesCount;
					projectsName[i] = project.name;
				}
				let data = {
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
			_multipleSmell();
		}
	}
}
