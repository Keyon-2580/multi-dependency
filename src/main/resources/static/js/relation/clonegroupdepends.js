var rclonegroupdepends = function (name, cytoscapeutil) {

    var _clonegroup = function() {

        dependsmatrix(name);

        dependedmatrix(name);

        dependfiledetail(name);

        dependedfiledetail(name);
    };

    var dependsmatrix = function(name) {
        $.ajax({
            type: "GET",
            url: "/relation/clonegroup/" + name + "/dependsmatrix",
            success: function(result) {
                var html = "<tr>";
                html += "<td></td>";
                for(var i = 0; i < result.dependsnodes.length; i++){
                    html += "<td>" + (i + 1) + "</td>";
                }
                html += "</tr>";
                for(var i = 0; i < result.nodes.length; i++){
                    html += "<tr>";
                    html += "<td><a target='_blank' href='/relation/file/" + result.nodes[i].id + "'>" + result.nodes[i].path + "</a></td>";
                    for(var j = 0;j < result.dependsnodes.length;j++){
                        html += "<td>";
                        if(result.matrix[i][j].toString() === "true"){
                            html +=  "T";
                        }
                        html += "</td>";
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
                    html += "<td>" + (i + 1) + "</td>";
                }
                html += "</tr>";
                for(var i = 0; i < result.nodes.length; i++){
                    html += "<tr>";
                    html += "<td><a target='_blank' href='/relation/file/" + result.nodes[i].id + "'>" + result.nodes[i].path + "</a></td>";
                    for(var j = 0;j < result.dependsnodes.length;j++){
                        html += "<td>";
                        if(result.matrix[i][j].toString() === "true"){
                            html +=  "T";
                        }
                        html += "</td>";
                    }
                    html += "</tr>";
                }
                $("#dependedmatrix").html(html);
            }
        })
    }

    var dependfiledetail = function(name) {
        $.ajax({
                type: "GET",
                url: "/relation/clonegroup/" + name + "/alldependsonnodes",
                success: function(result) {
                    var html = "<ol>";
                    for(var i = 0;i < result.length;i++){
                        html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                    }
                    html += "</ol>";
                    $("#dependfiledetail").html(html);
                }
            })
    }

    var dependedfiledetail = function(name) {
        $.ajax({
            type: "GET",
            url: "/relation/clonegroup/" + name + "/alldependednodes",
            success: function(result) {
                var html = "<ol>";
                for(var i = 0;i < result.length;i++){
                    html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "'>" + result[i].path + "</a></li>";
                }
                html += "</ol>";
                $("#dependedfiledetail").html(html);
            }
        })
    }

    return {
        init: function(){
            _clonegroup();
        }
    }
}