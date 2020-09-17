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

    var loaddata = function(fileId1,fileId2) {
        // showtree("tree_file1", data1);
        // showtree("tree_file2", data2);

        $.ajax({
            type : "GET",
            url : "/clone/file/double/json?fileId1="+ fileId1 +"&fileId2=" + fileId2,
            success : function(result) {
                var data1 = result[fileId1];
                console.log()
                showtree("tree_file1", data1);
                var data2 = result[fileId2];
                showtree("tree_file2", data2);

            }
        })
    }

    return {
        init : function(fileId1,fileId2) {
            loaddata(fileId1,fileId2);
        }
    }
}
