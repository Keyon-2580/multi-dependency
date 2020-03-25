require.config({
	baseUrl:"/js",
	paths: {
		"jquery": "import/jquery-3.4.1.min",
		"bootstrap-multiselect": "import/bootstrap/bootstrap-multiselect-0.9.15",
		"jqplot": "import/jqplot/jquery.jqplot.min",
		"cytoscape": "import/cytoscape.js-3.13.1/cytoscape.min",
		"cytoscape-dagre": "import/cytoscape.js-3.13.1/cytoscape-dagre",
		"dagre": "import/cytoscape.js-3.13.1/dagre.min",
		"testcase": "testcase",
		"utils": "util"
	},
	shim:{
		"bootstrap_multiselect":['jquery'],
		"jqplot":['jquery'],
		"cytoscape-dagre":['cytoscape']
		
	}

});
