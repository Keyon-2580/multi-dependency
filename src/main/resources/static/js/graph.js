var graph = function() {
	var showGraph = function(data) {
		var myChart = echarts.init(document.getElementById('package_graph'));
		option = {
				legend: {
					data: data.legend
				},
				series: [{
					type: 'graph',
					layout: 'force',
					animation: false,
					label: {
						position: 'right',
						formatter: '{b}'
					},
					draggable: true,
					data: data.nodes,
					categories: data.categories,
					force: {
						edgeLength: 5,
						repulsion: 20,
						gravity: 0.2
					},
					edges: data.links
				}]
		};
		
		myChart.setOption(option);
	}
	var drawPic = function(canvas, width, height, result){
		console.log(result);
		var data = result.data;
		console.log(data);
		console.log(canvas);
		var packageIndex = result.packageIndex;
		var cxt = canvas.getContext("2d");
		var per = 10;
		cxt.save();
		cxt.fillStyle="#ffffff";
		cxt.fillRect(0, 0, width, height);
		cxt.restore();
		cxt.strokeStyle = "#000000";
		cxt.lineWidth = 0.1;
		/*for(var i = 0; i < data.length; i++){
			cxt.moveTo(i * per, 0);
			cxt.lineTo(i * per, height);
			cxt.moveTo(0, i * per);
			cxt.lineTo(width, i * per);
		}*/
		cxt.save();
		cxt.strokeStyle = "yellow";
		cxt.lineWidth = 10;
		for(var i = 0; i < packageIndex.length; i++) {
			cxt.moveTo(packageIndex[i] * per ,0);
			cxt.lineTo(packageIndex[i] * per ,height);
			cxt.moveTo(0, packageIndex[i] * per);
			cxt.lineTo(width, packageIndex[i] * per);
		}
		cxt.restore();
		cxt.save();
		for(var i = 0; i < data.length; i++){
			for(var j = 0; j < data[i].length; j++) {
				if("" != data[i][j].type) {
					if(false == data[i][j].differentPackage) {
						cxt.fillStyle="red";
						cxt.fillRect(i * per, j * per, per, per);
					} else {
						cxt.fillStyle="green";
						cxt.fillRect(i * per, j * per, per, per);
					}
				}
			}
		}
		cxt.restore();
		cxt.stroke();
//		console.log("eeeeeeeeeee");
//		return cxt;
	}
	var _graph = function() {
		$.ajax({
			type : "get",
			url : "/graph/file",
			success : function(result) {
				var width = result.data.length * 10;
				$("#file_graph").attr("width", width);
				$("#file_graph").attr("height", width);
				var canvas = document.getElementById("file_graph");
				drawPic(canvas, width, width, result);
			}
		});
	}
	
	return {
		init : function() {
			_graph();
		}
	}
}
