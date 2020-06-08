/**
 * "/clone/" + level + "/table/microservice",
 * "/clone/" + level + "/group/cytoscape?top=" + top,
 * "/clone/" + level + "/group/histogram",
 * "/clone/" + level + "/group/cytoscape/" + num,
 */
function copyToClip(content) {
    var aux = document.createElement("input"); 
    aux.setAttribute("value", content); 
    document.body.appendChild(aux); 
    aux.select();
    document.execCommand("copy"); 
    document.body.removeChild(aux);
    alert("复制成功");
}
var clone = function(cytoscapeutil, level, removeFileClone, removeDataClass) {
	var urlRemoveParams = "removeFileClone=" + removeFileClone + "&removeDataClass=" + removeDataClass;
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
	var toggleNode = function(id, checked, cy) {
		var node = cy.$("#" + id);
		if(node == null) {
			return;
		}
		if(checked) {
			cy.$("#" + id).style({"visibility": "visible"});
			for(var i = 0; i < node.connectedEdges().length; i++) {
				cy.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "visible"});
			}
		} else {
			cy.$("#" + id).style({"visibility": "hidden"});
			for(var i = 0; i < node.connectedEdges().length; i++) {
				cy.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "hidden"});
			}
		}
	}
	var cys = [];
	var showZTree = function(nodes, container, cy) {
		var setting = {
				callback: {
					onCheck: function(event, treeId, treeNode) {
						var id = treeNode.id;
						toggleNode(id, treeNode.checked, cy);
					}
				},
				check: {
					enable: true,
					chkStyle: "checkbox",
					chkboxType: { "Y" : "", "N" : "" }
				}
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var _showCytoscape = function(container, data, copyDivId = "") {
		var cy = cytoscapeutil.showDataInCytoscape(container, data, "random");
		if(copyDivId != "") {
			console.log(copyDivId);
			cy.on('tap', 'node', function(evt){
				var node = evt.target;
				if(node.data().type == "File") {
					var value = node.data().value;
					var html = "<a class='clipBoard'>";
					html += value;
					html += "</a>";
					$("#" + copyDivId).html(html);
					$(".clipBoard").click(function(){
						var content = $(this).text();
						copyToClip(content)
					});
				} else if(node.data().type == "Function") {
					var value = node.data().value;
					var html = "<a class='clipBoard'>";
					html += value;
					html += "</a>";
					html += "&nbsp;&nbsp;from&nbsp;&nbsp;";
					var connect = node.connectedEdges();
					for(var i = 0; i < connect.length; i++) {
						if(connect[i].data().type == "Contain") {
							html += "<a class='clipBoard'>";
							html += cy.$("#" + connect[i].data().source).data().value;
							html += "</a>";
						}
					}
					$("#" + copyDivId).html(html);
					$(".clipBoard").click(function(){
						var content = $(this).text();
						copyToClip(content)
					});
				}
			})
			cy.on('tap', 'edge', function(evt){
				var edge = evt.target;
				console.log(edge.data);
				$(".clipBoard").click(function(){
					var content = $(this).text();
					console.log(content);
					copyToClip(content)
				});
			})
		}
		cys[cys.length] = cy;
		cy.style().selector('node[type="Function"]').style({
			'shape' : 'ellipse',
			'width': function(content) {
				var split = content.data().name.split("\n");
				var maxWidth = 0;
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
		cy.style().selector('node[type="CloneGroup"]').style({
			'shape' : 'rectangle',
			'width': function(content) {
				var split = content.data().name.split("\n");
				var maxWidth = 0;
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
			url : "/clone/" + level + "/table/microservice?" + urlRemoveParams,
			success : function(result) {
				$("#table").addClass("table");
				$("#table").addClass("table-bordered");
				console.log(result);
				var html = "<table class='table table-bordered' id='table'>";
				html += "<tr>";
				html += "<th>";
				html += "</th>";
				for(var i = 0; i < result.microservices.length; i++) {
					var ms = result.microservices[i];
					html += "<th>";
					html += ms.name;
					html += "</th>";
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
								html += data.cloneFiles[j].path;
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
		var _showGroupsResult = function(result) {
			console.log(result);
			var size = result.size;
			var html = "";
			html += "<div class='col-sm-12'><button class='btn btn-default fullscreen_btn_top' name='group'>全屏</button>";
			html += "<p></p></div>";
			html += "<div><h4>groups: " + result.groups + "</h4></div>"
			html += '<div class="col-sm-12 div_cytoscape_div" id="fullscreenAble_group">';
			html += '<div class="div_cytoscape_treeview">';
			html += '<ul id="node_ztree_groups" class="ztree"></ul>';
			html += '</div>';
			html += '<div class="div_cytoscape" style="float: left; display: inline;">';
			html += '<div id="cloneGroupsDiv" class="div_cytoscape_content cy"></div>';
			html += '</div>'
			html += '</div>';
			html += '<div class="col-sm-12" id="copyDiv_group"></div>';
			html += '<div class="col-sm-12"><hr/></div>';
			$("#groupCytoscape").html(html);
			showZTree(result.groupValue.ztree, $("#node_ztree_groups"), _showCytoscape($("#cloneGroupsDiv"), result.groupValue, "copyDiv_group"));
			
			html = "";
			for(var i = 0; i < size; i++) {
				html += "<div class='col-sm-12'><button class='btn btn-default fullscreen_btn_top' name='" + i +"'>全屏</button>";
				html += "<div><h4>groups: " + result.groups[i] + "</h4></div>"
//				html += "<button class='btn btn-default save_top' name='" + i +"'>保存图片</button><p></p></div>";
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
				html += '</div>';
				html += '<div class="col-sm-12" id="copyDiv_' + i + '"></div>';
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
				var cy = _showCytoscape($("#cloneGroupDiv_" + i), result.value[i], "copyDiv_" + i);
				cys[i] = cy;
				showZTree(result.value[i].ztree, $("#node_ztree_" + i), cy);
			}
		}
		
		$("#searchSelected").click(function(){
			var groups = $("#searchGroups").val();
			if(groups.length == 0) {
				return ;
			}
			var params = {
				"groups" : groups
			}
			var url = "/clone/" + level + "/group/cytoscape?" + urlRemoveParams;
			$.ajax({
				type : "POST",
				contentType : "application/json",
				dataType : "json",
				url : url,
				data : JSON.stringify(params),
				success : function(result) {
					if(result.result == "success") {
						_showGroupsResult(result);
					}
				}
			});
		});
		$("#searchCountOfMSs").click(function(){
			var count = $("#CountMSsInput").val();
			console.log(count);
			if(top < 0) {
				return ;
			}
			$.ajax({
				type : "GET",
				url : "/clone/" + level + "/group/cytoscape?projectsCount=" + count + "&" + urlRemoveParams,
				success : function(result) {
					if(result.result == "success") {
						_showGroupsResult(result);
					}
				}
			});
		});
		$("#searchTop").click(function(){
			var top = $("#topInput").val();
			console.log(top);
			if(top <= 0) {
				return ;
			}
			$.ajax({
				type : "GET",
				url : "/clone/" + level + "/group/cytoscape?top=" + top + "&" + urlRemoveParams,
				success : function(result) {
					if(result.result == "success") {
						_showGroupsResult(result);
					}
				}
			});
		});
		table();
		var myChart = echarts.init(document.getElementById('main'));
		$.ajax({
			type : "GET",
			url : "/clone/" + level + "/group/histogram?" + urlRemoveParams,
			success : function(result) {
				console.log(result);
				$("#searchGroups").append('<optgroup id="select_single" label="single">单项目克隆</optgroup>');
				$("#searchGroups").append('<optgroup id="select_between" label="between">跨项目克隆</optgroup>');
				
				var xAxisData = [];
				var nodesData = [];
				var projectsData = [];
				for(var i = 0; i < result.size; i++) {
					xAxisData[i] = "group_" + i;
					nodesData[i] = result.value["nodeSize"][i];
					projectsData[i] = result.value.projectSize[i];
					var html = '<option value="' + i + '" >' + xAxisData[i] + '_' + nodesData[i] + ',' + projectsData[i]  + '</option>';
					if(projectsData[i] > 1) {
						$("#select_between").append(html);
					} else {
						$("#select_single").append(html);
					}
				}
				$('#searchGroups').multiselect({
					maxHeight: 200,
					enableCollapsibleOptGroups: true,
		            enableClickableOptGroups: true,
		            enableCollapsibleOptGroups: true,
		            includeSelectAllOption: true
				});
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
						url : "/clone/" + level + "/group/cytoscape/" + num + "?" + urlRemoveParams,
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
								html += '<div class="col-sm-12" id="copyDiv_group_one"></div>';
								html += '<div class="col-sm-12"><hr/></div>';
								$("#specifiedCytoscape").html(html);
								$(".fullscreen_btn").unbind("click");
								$(".fullscreen_btn").click(function(){
									showFull("fullscreenAble");
								})
								var cy = _showCytoscape($("#cloneGroupDiv"), result.value, "copyDiv_group_one");
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
