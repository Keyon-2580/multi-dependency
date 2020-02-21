var project = function() {
	console.log("rrr");
	$.ajax({
    	type: 'GET',
    	url: "/project/treeview",
    	success: function(result) {
    		console.log(result);
    		if(result.result == "success") {
    			showTreeView($("#tree"), result.value)
    		}
    	}
    });
	
};

var itemOnclick = function (target){
	console.log(target);

}