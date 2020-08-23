var rType = function(typeId, cytoscapeutil) {
	var _type = function() {
		containFunction(typeId);
		containField(typeId);
	};
	
	var containFunction = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/contain/function",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/function/" + result[i].id + "' >";
					html += result[i].name + result[i].parametersIdentifies;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#contain_function_content").html(html);
			}
		});
	}
	
	var containField = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/contain/field",
			success: function(result) {
				var html = "<ul>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/field/" + result[i].id + "' >";
					html += result[i].name + " : " + result[i].typeIdentify;
					html += "</a></li>";
				}
				html += "</ul>";
				$("#contain_field_content").html(html);
			}
		});
	}
	
	return {
		init: function(){
			_type();
		}
	}
}

