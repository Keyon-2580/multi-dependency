var multiple = function(cytoscapeutil) {
	
	var _pie = function() {
		var allFilesPie = echarts.init(document.getElementById('pieInAllFiles'));
		var smellAndIssueFilesPie = echarts.init(document.getElementById('pieInIssueAndSmellFiles'));
		$.ajax({
			type: "get",
			url: "/as/issue/pie",
			success: function(result) {
				console.log(result);
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
									{value: result.normalFiles.length, name: 'normalFiles'},
									{value: result.onlyIssueFiles.length, name: 'onlyIssueFiles'},
									{value: result.issueAndSmellFiles.length, name: 'issueAndSmellFiles'},
									{value: result.onlySmellFiles.length, name: 'onlySmellFiles'}
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
									{value: result.onlyIssueFiles.length, name: 'onlyIssueFiles'},
									{value: result.issueAndSmellFiles.length, name: 'issueAndSmellFiles'},
									{value: result.onlySmellFiles.length, name: 'onlySmellFiles'}
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
		})
	}
	
	var _multiple = function() {
		$.ajax({
			type: "get",
			url: "/as/api/multiple",
			success: function(result) {
				console.log(result);
				_pie();
				var html = "";
				for(var project in result) {
					console.log(project);
					console.log(result[project]);
					var p = result[project][0].project;
					html += "<div>";
					html += "<div>";
					html += "<h4>" + p.name + " (" + p.language + ") ";
					html += "</div>";
					html += "<div>";
					html += "<table class='table table-bordered'>";
					html += "<tr>";
					html += "<th>文件</th>";
					html += "<th>cycle</th>";
					html += "<th>hublike</th>";
					html += "<th>unstable</th>";
					html += "<th>logic coupling</th>";
					html += "<th>simiar</th>";
					html += "<th>cyclic hierarchy</th>";
					html += "<th>god component</th>";
					html += "<th>page rank</th>";
					html += "</tr>";
					for(var i = 0 ; i < result[project].length; i++) {
						var value = result[project][i];
						html += "<tr>";
						html += "<td><a target='_blank' href='/relation/file/" + value.file.id + "'>" + value.file.path + "</a></td>";
						html += "<td>" + (value.cycle == true ? "T" : "") + "</td>";
						html += "<td>" + (value.hublike == true ? "T" : "") + "</td>";
						html += "<td>" + (value.unstable == true ? "T" : "") + "</td>";
						html += "<td>" + (value.logicCoupling == true ? "T" : "") + "</td>";
						html += "<td>" + (value.similar == true ? "T" : "") + "</td>";
						html += "<td>" + (value.cyclicHierarchy == true ? "T" : "") + "</td>";
						html += "<td>" + (value.god == true ? "T" : "") + "</td>";
						html += "<td>" + value.file.score + "</td>";
						html += "</tr>";
					}
					html += "</table>";
					html += "</div>";
					html += "</div>";
				}
				$("#table").html(html);
			}
		})
	}
	
	return {
		init : function() {
			_multiple();
		}
	}
}
