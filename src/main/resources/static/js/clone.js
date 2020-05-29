var clone = function(cytoscapeutil) {
	var cys = [];
	var showZTree = function(nodes, container = $("#ztree")) {
		var setting = {
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var _clone = function() {
		$("#searchTop").click(function(){
			var top = $("#topInput").val();
			console.log(top);
			$.ajax({
				type : "GET",
				url : "/clone/file/group/cytoscape?top=" + top,
				success : function(result) {
					if(result.result == "success") {
						console.log(result.value);
						var size = result.size;
						console.log(size);
						var html = "";
						for(var i = 0; i < size; i++) {
							html += '<div class="div_cytoscape" style="float: left; display: inline;">';
//							html += '<div class="div_cytoscape_treeview">';
//							html += '<ul id="ztree" class="ztree"></ul>';
//							html += '</div>';
							html += '<div id="cloneGroupDiv_' + i + '" class="div_cytoscape_content cy"></div>';
							html += '</div>'
							html += '<div><hr/></div>';
						}
						$("#content").html(html);
						for(var i = 0; i < size; i++) {
							var cy = cytoscapeutil.showDataInCytoscape($("#cloneGroupDiv_" + i), result.value[i], "dagre");
							cys[cys.length] = cy;
							cy.style().selector('node[type="File"]').style({
								'shape' : 'ellipse',
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
							cy.style().selector('edge[type="FileCloneFile"]').style({
								'content': 'data(value)',
								'curve-style': 'bezier',
								'width': 1,
								'line-color': 'green',
								'line-style': 'dashed',
								'target-arrow-shape' : 'none',
								'font-size' : 20
							}).update();
							var edges = cy.remove('edge[type="FileCloneFile"]');
							cy.layout({name : 'dagre'}).run();
							console.log(edges);
							cy.add(edges);
						}
					}
				}
			});
		});
		/*var myChart = echarts.init(document.getElementById('main'));
		$.ajax({
			type : "GET",
			url : "/project/fanIO/file/" + project.id,
			success : function(result) {
				console.log(result);
				var xAxisData = [];
				var fanInData = [];
				var fanOutData = [];
				console.log(result.length);
				for(var i = 0; i < result.length; i++) {
					xAxisData[i] = result[i].node.name;
					console.log(xAxisData[i]);
					fanInData[i] = result[i].fanIn.length == 0 ? null : result[i].fanIn.length;
					fanOutData[i] = result[i].fanOut.length == 0 ? null : result[i].fanOut.length;
				}
				var option = {
		        	    tooltip: {
		        	        trigger: 'axis',
		        	        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
		        	            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
		        	        }
		        	    },
		        	    legend: {
		        	        data: ['FAN_IN', 'FAN_OUT']
		        	    },
		        	    grid: {
		        	        left: '3%',
		        	        right: '4%',
		        	        bottom: '3%',
		        	        containLabel: true
		        	    },
		        	    xAxis: [
		        	        {
		        	            type: 'category',
		        	            data: xAxisData,
		        	            axisLabel: {  
		        	                interval:0,  
		        	                rotate:40  
		        	             }  
		        	        }
		        	    ],
		        	    yAxis: [
		        	        {
		        	            type: 'value'
		        	        }
		        	    ],
		        	    series: [
		        	        {
		        	            name: 'FAN_IN',
		        	            type: 'bar',
		        	            stack: 'fan',
		        	            data: fanInData
		        	        },
		        	        {
		        	            name: 'FAN_OUT',
		        	            type: 'bar',
		        	            stack: 'fan',
		        	            data: fanOutData
		        	        }
		        	    ]
		        	};
		        // 使用刚指定的配置项和数据显示图表。
		        myChart.setOption(option);
			}
		});*/
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

