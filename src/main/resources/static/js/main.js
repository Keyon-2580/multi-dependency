require.config({
	baseUrl:"/js",
	paths: {
		"jquery": "import/jquery-3.4.1.min",
		"bootstrap": "import/bootstrap/bootstrap-3.3.2.min",
		"bootstrap-multiselect": "import/bootstrap/bootstrap-multiselect-0.9.15",
		"bootstrap-treeview": "import/bootstrap/bootstrap-treeview.min",
		"jqplot": "import/jqplot/jquery.jqplot.min",
		"cytoscape": "import/cytoscape.js-3.13.1/cytoscape.min",
		"cytoscape-dagre": "import/cytoscape.js-3.13.1/cytoscape-dagre",
		"dagre": "import/cytoscape.js-3.13.1/dagre.min",
		"testcase": "testcase",
		"utils": "util",
		"project": "project",
		"multiple": "multiple",
		"cytoscapeUtils" : "cytoscapeutils"
	},
	shim:{
		"bootstrap":['jquery'],
		"bootstrap-multiselect":['jquery'],
		"bootstrap-treeview":['jquery'],
		"jqplot":['jquery'],
		"cytoscape-dagre":['cytoscape']
		
	}

});
