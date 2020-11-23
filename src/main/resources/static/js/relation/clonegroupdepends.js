var rclonegroupdepends = function (name, cytoscapeutil) {

    var _clonegroup = function() {

        dependsmatrix(name);

        dependedmatrix(name);

        filedetail(name);
    };

    var dependsmatrix = function(name) {
        $.ajax({
            type: "GET",
            url: "/relation/clonegroup/" + name + "/dependsmatrix",
            success: function(result) {
                var html = "<tr>";
                html += "<td></td>";
                for(var i = 0; i < result.dependsnodes.length; i++){
                    html += "<td>" + result.dependsnodes[i].id + "</td>";
                }
                html += "</tr>";
                for(var i = 0; i < result.nodes.length; i++){
                    html += "<tr>";
                    html += "<td>" + result.nodes[i].path + "</td>";
                    for(var j = 0;j < result.dependsnodes.length;j++){
                        html += "<td>" + result.matrix[i][j] + "</td>";
                    }
                    html += "</tr>";
                }
                $("#dependmatrix").html(html);
            }
        })
    }

    var dependedmatrix = function(name) {
        $.ajax({
            type: "GET",
            url: "/relation/clonegroup/" + name + "/dependedmatrix",
            success: function(result) {
                var html = "<tr>";
                html += "<td></td>";
                for(var i = 0; i < result.dependsnodes.length; i++){
                    html += "<td>" + result.dependsnodes[i].id + "</td>";
                }
                html += "</tr>";
                for(var i = 0; i < result.nodes.length; i++){
                    html += "<tr>";
                    html += "<td>" + result.nodes[i].path + "</td>";
                    for(var j = 0;j < result.dependsnodes.length;j++){
                        html += "<td>" + result.matrix[i][j] + "</td>";
                    }
                    html += "</tr>";
                }
                $("#dependedmatrix").html(html);
            }
        })
    }

    var filedetail = function(name) {
        $.ajax({
                type: "GET",
                url: "/relation/clonegroup/" + name + "/aldependsnodes",
                success: function(result) {
                    var html = "<ol>";
                    for(var i = 0;i < result.length;i++){
                        html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>" + + result[i].id + "." + result[i].path + "</a></li>";
                    }
                    html += "</ol>";
                    $("#filedetail").html(html);
                }
            })
    }

    return {
        init: function(){
            _clonegroup();
        }
    }
}