var clone = function(cytoscapeutil, level) {
	var isFullScreen = false;
	function showFull(divId){
		var full=document.getElementById(divId);
		launchIntoFullscreen(full);
		isFullScreen = true;
		$(".div_cytoscape").css("height", "100%");
		$(".div_cytoscape_content").css("height", "100%");
		$(".div_cytoscape_treeview").css("height", "100%");
	}
	function delFull() {
		exitFullscreen();
	}
	window.onresize = function() {
		console.log("resize");
		if(isFullScreen == true) {
			isFullScreen = false;
		} else {
			$(".div_cytoscape").css("height", "500px");
			$(".div_cytoscape_content").css("height", "500px");
			$(".div_cytoscape_treeview").css("height", "500px");
		}
	};
	var cys = [];
	var showZTree = function(nodes, container, cy) {
		var setting = {
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var _showCytoscape = function(container, data) {
		var cy = cytoscapeutil.showDataInCytoscape(container, data, "random");
		cys[cys.length] = cy;
		cy.style().selector('node[type="Function"]').style({
			'shape' : 'ellipse',
			'width': function(content) {
				var split = content.data().name.split("\n");
				var maxWidth = 0;
				console.log(split);
				for(var i = 0; i < split.length; i++) {
					var width = split[i].replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
					if(width > maxWidth) {
						maxWidth = width;
					}
				}
				return maxWidth;
			},
			'height': function(content) {
				var split = content.data().name.split("\n");
				var length = split.length;
				return 21 * length;
			},
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)',
			'text-wrap': 'wrap'
		}).update();
		cy.style().selector('node[type="File"]').style({
			'shape' : 'ellipse',
			'width': function(content) {
				var split = content.data().name.split("\n");
				var maxWidth = 0;
				console.log(split);
				for(var i = 0; i < split.length; i++) {
					var width = split[i].replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
					if(width > maxWidth) {
						maxWidth = width;
					}
				}
				return maxWidth;
			},
			'height': function(content) {
				var split = content.data().name.split("\n");
				var length = split.length;
				return 21 * length;
			},
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)',
			'text-wrap': 'wrap'
		}).update();
		cy.style().selector('node[type="Project"]').style({
			'shape' : 'rectangle',
			'width': function(content) {
				return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
			},
			'height': 30,
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)'
		}).update();
		cy.style().selector('node[type="MicroService"]').style({
			'shape' : 'hexagon',
			'width': function(content) {
				return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
			},
			'height': 30,
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)'
		}).update();
		cy.style().selector('edge[type="Clone"]').style({
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'green',
			'line-style': 'dashed',
			'target-arrow-shape' : 'none',
			'font-size' : 20
		}).update();
		var edges = cy.remove('edge[type="Clone"]');
		cy.layout({name : 'dagre'}).run();
		cy.add(edges);
		return cy;
	}
	var cys = {};
	var table = function(){
		$.ajax({
			type : "GET",
			url : "/clone/" + level + "/table/microservice",
			success : function(result) {
				$("#table").addClass("table");
				$("#table").addClass("table-bordered");
				console.log(result);
				var html = "<table class='table table-bordered' id='table'>";
				html += "<tr>";
				html += "<td>";
				html += "</td>";
				for(var i = 0; i < result.microservices.length; i++) {
					var ms = result.microservices[i];
					html += "<td>";
					html += ms.name;
					html += "</td>";
				}
				html += "</tr>";
				for(var group in result.data) {
					if(group == -1) {
						continue;
					}
					html += "<tr>";
					html += "<td>" + group + "</td>";
					var map = result.data[group];
					for(var i = 0; i < result.microservices.length; i++) {
						html += "<td>";
						var data = map[result.microservices[i].id];
						if(level == "file") {
							for(var j = 0; j < data.cloneFiles.length; j++) {
								html += data.cloneFiles[j].name;
								if(j != data.cloneFiles.length - 1) {
									html += "<br/>";
								}
							}
						} else if(level == "function") {
							for(var j = 0; j < data.cloneFunctions.length; j++) {
								html += data.cloneFunctions[j].name;
								if(j != data.cloneFunctions.length - 1) {
									html += "<br/>";
								}
							}
						}
						html += "</td>";
					}
					html += "</tr>"
				}
				html += "</table>";
				$("#table_div").html(html);
				$("#table_wait").text("");
			}
		});
	}
	var _clone = function() {
		$("#searchTop").click(function(){
			var top = $("#topInput").val();
			console.log(top);
			$.ajax({
				type : "GET",
				url : "/clone/" + level + "/group/cytoscape?top=" + top,
				success : function(result) {
					if(result.result == "success") {
						console.log(result.value);
						var size = result.size;
						var html = "";
						for(var i = 0; i < size; i++) {
							html += "<div class='col-sm-12'><button class='btn btn-default fullscreen_btn_top' name='" + i +"'>全屏</button>";
//							html += "<button class='btn btn-default save_top' name='" + i +"'>保存图片</button><p></p></div>";
							html += "<p></p></div>";
							html += '<div class="col-sm-12 div_cytoscape_div" id="fullscreenAble_' + i + '">';
								html += '<div class="div_cytoscape_treeview">';
									html += '<ul id="node_ztree_' + i + '" class="ztree"></ul>';
								html += '</div>';
								html += '<div class="div_cytoscape" style="float: left; display: inline;">';
									html += '<div id="cloneGroupDiv_' + i + '" class="div_cytoscape_content cy"></div>';
								html += '</div>'
							html += '</div>';
							html += '<div id="cloneImg_' + i + '">'
							html += '</div>'
							html += '<div class="col-sm-12"><hr/></div>';
						}
						$("#content").html(html);
						$(".fullscreen_btn_top").unbind("click");
						$(".fullscreen_btn_top").click(function() {
							showFull("fullscreenAble_" + $(this).attr("name"));
						});
						/*$(".save_top").click(function(){
							var i = $(this).attr("name");
							var cy = cys[i];
							cytoscapeutil.showImg(cy, "cloneImg_" + i)
							$("#clone_img_" + i).css("height", "500px");
							console.log(cys);
						});*/
						for(var i = 0; i < size; i++) {
							var cy = _showCytoscape($("#cloneGroupDiv_" + i), result.value[i]);
							cys[i] = cy;
							showZTree(result.value[i].ztree, $("#node_ztree_" + i), cy);
						}
					}
				}
			});
		});
		table();
		var myChart = echarts.init(document.getElementById('main'));
		$.ajax({
			type : "GET",
			url : "/clone/" + level + "/group/histogram",
			success : function(result) {
				console.log(result);
				var xAxisData = [];
				var nodesData = [];
				var projectsData = [];
				for(var i = 0; i < result.size; i++) {
					xAxisData[i] = "group_" + i;
					nodesData[i] = result.value["nodeSize"][i];
					projectsData[i] = result.value.projectSize[i];
				}
				var legendLevel = "";
				if(level == "function") {
					legendLevel = "克隆组相关方法数";
				}
				if(level == "file") {
					legendLevel = "克隆组相关文件数";
				}
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
		        	        data: [legendLevel, '克隆跨项目数']
		        	    },
		        	    grid: {
		        	        left: '3%',
		        	        right: '4%',
		        	        bottom: '3%',
		        	        containLabel: true
		        	    },
		        	    xAxis: [{
		        	            type: 'category',
		        	            data: xAxisData,
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
		        	            name: legendLevel,
		        	            type: 'bar',
		        	            stack: 'cloneNode',
		        	            data: nodesData
		        	        },{
		        	            name: '克隆跨项目数',
		        	            type: 'bar',
		        	            stack: 'cloneProject',
		        	            data: projectsData
		        	        }
		        	    ]
		        	};
		        myChart.setOption(option);
		        myChart.on('click', function(params) {
		        	var name = params.name;
		        	console.log(name);
		        	var num = name.split("_")[1];
		        	console.log(num);
		        	$.ajax({
						type : "GET",
						url : "/clone/" + level + "/group/cytoscape/" + num,
						success : function(result) {
							if(result.result == "success") {
								console.log(result.value);
								var html = "";
								html += "<div class='col-sm-12'><button class='btn btn-default fullscreen_btn'>全屏</button>";
								html += "<p></p></div>";
								html += '<div class="col-sm-12 div_cytoscape_div" id="fullscreenAble">';
								html += '<div class="div_cytoscape_treeview">';
								html += '<ul id="node_ztree_num" class="ztree"></ul>';
								html += '</div>';
								html += '<div class="div_cytoscape" style="float: left; display: inline;">';
								html += '<div id="cloneGroupDiv" class="div_cytoscape_content cy"></div>';
								html += '</div>'
								html += '</div>';
								html += '<div class="col-sm-12"><hr/></div>';
								$("#specifiedCytoscape").html(html);
								$(".fullscreen_btn").unbind("click");
								$(".fullscreen_btn").click(function(){
									showFull("fullscreenAble");
								})
								var cy = _showCytoscape($("#cloneGroupDiv"), result.value);
								showZTree(result.value.ztree, $("#node_ztree_num"), cy);
							}
						}
					});
		        });
			}
		});
	};
	var _showImg = function(){
		$("#showImg").click(function() {
			console.log("showImg");
			cytoscapeutil.showImg(cy, "entry-png-eg");
		})
	}
	var _clearMemo = function() {
		$("#clearMemo").click(function() {
			console.log("clearMemo");
			if(cy == null) {
				return ;
			}
			cy = cytoscapeutil.refreshCy(cy);
			cy.expandCollapse();
			var api = cy.expandCollapse('get');
			api.collapseAll();
			cy.layout({
				name: "dagre"
			}).run();
		});
	};
	
	return {
		init: function(){
			_clone();
		}
	}
}

