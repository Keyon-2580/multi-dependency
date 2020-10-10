var doublefile = function() {

    var showtree = function(divId, zNodes) {
        var zTreeObj;
        var setting = {
            check : {
                enable : false
            },
            data : {
                simpleData : {
                    enable : true
                }
            }
        };

        console.log(zNodes);
        zTreeObj = $.fn.zTree.init($("#" + divId), setting, zNodes);
    }

    var loaddata = function(fileId1,fileId2,cloneType,linesSize1,linesSize2,loc1,loc2,value,cochange,filePath1,filePath2,cochangeId) {
        // showtree("tree_file1", data1);
        // showtree("tree_file2", data2);
        $.ajax({
            type : "GET",
            url : "/clone/file/double/json?fileId1="+ fileId1 +"&fileId2=" + fileId2,
            success : function(result) {
                var html = ""
                html += "<table class = \"gridtable\">"
                    + "<tr><th>ID</th><th>filePath</th><th>cloneType</th><th>linesSize</th><th>loc</th></tr>"
                    + "<tr><td>1</td><td>" + filePath1 + "</td><td>" + cloneType.split("=")[1] + "</td><td>" + linesSize1 +"</td><td>" + loc1 +"</td></tr>"
                    + "<tr><td>2</td><td>" + filePath2 + "</td><td>" + cloneType.split("=")[1] + "</td><td>" + linesSize2 +"</td><td>" + loc2 +"</td></tr>";
                html += "</table>";

                html += "<table class = \"gridtable\" style = \"margin-top: 20px\">"
                    + "<tr><th>cochange</th><th>value</th></tr>"
                    + "<tr><td><a class='cochangeTimes' target='_blank' href='/git/cochange/commits?cochangeId=" + cochangeId
                    + "'>" + cochange + "</a></td><td><a target='_blank' href='/clone/compare?id1=" + fileId1 + "&id2=" + fileId2 +"'>" + value + "</td></tr>";
                html += "</table>";
                // console.log(html)

                $("#file_table").html(html);

                var data1 = result[fileId1];
                // console.log()
                showtree("tree_file1", data1);
                var data2 = result[fileId2];
                showtree("tree_file2", data2);

            }
        })
    }

    return {
        init : function(fileId1,fileId2,cloneType,linesSize1,linesSize2,loc1,loc2,value,cochange,filePath1,filePath2,cochangeId) {
            loaddata(fileId1,fileId2,cloneType,linesSize1,linesSize2,loc1,loc2,value,cochange,filePath1,filePath2,cochangeId);
        }
    }
}
