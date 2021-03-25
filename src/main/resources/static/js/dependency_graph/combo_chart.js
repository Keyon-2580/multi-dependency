let data = {};

const COLOR_DEPENDSON = 'rgb(119, 243, 252)';
const COLOR_CLONE = 'rgb(176,66,6)';
const COLOR_COCHANGE = 'rgb(255,165,0)';
const COLOR_LINK_NORMAL = '#f7c8ca';
const COLOR_LINK_INNER = '#d8d5d5';
const COLOR_LINK_CLICK = '#bd0303';
let last_click_node = "";
// let data_path = "../data/package_data.json"
// let data_path = "../data/atlas_0325.json"
let data_path = "../data/atlas_0325_original.json"

const EDGE_CLICK_MODEL = {
    style: {
        stroke: COLOR_LINK_CLICK,
        lineWidth: 3,
        endArrow: {
            path: G6.Arrow.vee(5, 8, 3),
            d: 3,
            fill: COLOR_LINK_CLICK,
        },
    },
};

const EDGE_NORMAL_MODEL = {
    style: {
        stroke: COLOR_LINK_NORMAL,
        endArrow: {
            path: G6.Arrow.vee(5, 8, 3),
            d: 3,
            fill: COLOR_LINK_NORMAL,
        },
    },
};

const EDGE_INNER_MODEL = {
    style: {
        stroke: COLOR_LINK_INNER,
        endArrow: {
            path: G6.Arrow.vee(5, 8, 3),
            d: 3,
            fill: COLOR_LINK_INNER,
        },
    },
};

// 注册自定义名为 pie-node 的节点类型
G6.registerNode('pie-node', {
    draw: (cfg, group) => {
        let linkTypeNum = [];

        if(cfg.dependsonDegree === 1){
            linkTypeNum.push(COLOR_DEPENDSON);
        }
        if(cfg.cloneDegree === 1){
            linkTypeNum.push(COLOR_CLONE);
        }
        if(cfg.cochangeDegree === 1){
            linkTypeNum.push(COLOR_COCHANGE);
        }

        const radius = cfg.size / 2; // node radius

        switch (linkTypeNum.length) {
            case 1:
                return group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', radius, 0],
                            ['A', radius, radius, 0, 1, 0, radius, 0.001],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[0],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape',
                });
            case 2:
                let inPercentage = 0.5;
                const inAngle = inPercentage * Math.PI * 2; // the anble for the indegree fan
                const inArcEnd = [radius * Math.cos(inAngle), -radius * Math.sin(inAngle)]; // the end position for the in-degree fan
                // fan shape for the in degree
                const fanIn = group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', radius, 0],
                            ['A', radius, radius, 0, 0, 0, inArcEnd[0], inArcEnd[1]],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[0],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape',
                });
                // draw the fan shape
                group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', inArcEnd[0], inArcEnd[1]],
                            ['A', radius, radius, 0, 0, 0, radius, 0],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[1],
                        cursor: "pointer",
                    },
                    name: 'out-fan-shape',
                });
                // 返回 keyshape
                return fanIn;
            case 3:
                let inPercentage_1 = 1 / 3;
                let inPercentage_2 = 2 / 3;

                const inAngle_1 = inPercentage_1 * Math.PI * 2; // the anble for the indegree fan
                const inArcEnd_1 = [radius * Math.cos(inAngle_1), -radius * Math.sin(inAngle_1)]; // the end position for the in-degree fan
                let isInBigArc_1 = 0,
                    isOutBigArc_1 = 1;
                if (inAngle_1 > Math.PI) {
                    isInBigArc_1 = 1;
                    isOutBigArc_1 = 0;
                }

                const inAngle_2 = inPercentage_2 * Math.PI * 2; // the anble for the indegree fan
                const inArcEnd_2 = [radius * Math.cos(inAngle_2), -radius * Math.sin(inAngle_2)]; // the end position for the in-degree fan
                // fan shape for the in degree
                const fanIn_1 = group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', radius, 0],
                            ['A', radius, radius, 0, 0, 0, inArcEnd_1[0], inArcEnd_1[1]],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[0],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape-1',
                });

                group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', inArcEnd_1[0], inArcEnd_1[1]],
                            ['A', radius, radius, 0, 0, 0, inArcEnd_2[0], inArcEnd_2[1]],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[1],
                        cursor: "pointer",
                    },
                    name: 'in-fan-shape-1',
                });
                // draw the fan shape
                group.addShape('path', {
                    attrs: {
                        path: [
                            ['M', inArcEnd_2[0], inArcEnd_2[1]],
                            ['A', radius, radius, 0, 0, 0, radius, 0],
                            ['L', 0, 0],
                            ['Z'],
                        ],
                        lineWidth: 0,
                        fill: linkTypeNum[2],
                        cursor: "pointer",
                    },
                    name: 'out-fan-shape',
                });
                // 返回 keyshape
                return fanIn_1;
        }
    },
});

//鼠标悬停提示项
const tooltip = new G6.Tooltip({
    offsetX: 20,
    offsetY: 40,
    getContent(e) {
        const outDiv = document.createElement('div');
        outDiv.style.width = '180px';
        outDiv.innerHTML = e.item.getModel().group_type === 'combo' ?
            `<b class="combo_label">${e.item.getModel().name}</b>
          <ul>
            <li>ID: ${e.item.getModel().id}</li>
          </ul>`

            : e.item.getModel().group_type === 'node' ?
                `<b class="combo_label">${e.item.getModel().name}</b>
          <ul>
            <li>ID: ${e.item.getModel().id}</li>
          </ul>` :

                `<b class="combo_label">${e.item.getModel().source}  ${e.item.getModel().target}</b>`
        // <ul>
        //   <li>source: ${e.item.getModel().source_name}</li>
        //   <li>target: ${e.item.getModel().target_name}</li>
        // </ul>
        return outDiv
    },
    itemTypes: ['node', 'combo', 'edge']
});

const descriptionDiv = document.createElement('div');
descriptionDiv.innerHTML = "";

const container = document.getElementById('combo_chart');
container.appendChild(descriptionDiv);
const width = container.scrollWidth;
const height = container.scrollHeight || 500;
const grid = new G6.Grid();

const graph = new G6.Graph({
    container: 'combo_chart',
    width,
    height,
    fitCenter: true,
    defaultNode: {
        type: 'circle',
        size: 50,
        style: {
            cursor: "pointer",
            fill: "#cce9f8",
            stroke: "#a0d6f4",
        },
    },
    groupByTypes: false,
    modes: {
        // default: ['drag-canvas', 'drag-combo', 'drag-node', 'collapse-expand-combo', 'zoom-canvas'],
        default: ['drag-canvas', 'drag-combo', 'drag-node','collapse-expand-combo', 'zoom-canvas'],
    },
    // layout: {
    //     type: 'comboForce',
    //     nodeSpacing: (d) => 8,
    //     comboSpacing: 50,
    //     preventOverlap: true,
    //     // gravity: 20
    // },
    // layout: {
    //     type: 'fruchterman',
    //     gravity: 2,
    //     speed: 10,
    //     relayout: false,
    // },
    defaultCombo: {
        type: 'rect',
        /* The minimum size of the combo. combo 最小大小 */
        size: [50, 50],
        /* style for the keyShape */
        // style: {
        //   lineWidth: 1,
        // },
        labelCfg: {
            /* label's offset to the keyShape */
            // refY: 10,
            /* label's position, options: center, top, bottom, left, right */
            position: 'top',
            /* label's style */
            // style: {
            //   fontSize: 18,
            // },
        },
        style: {
            lineWidth : 2,
            stroke : '#a0d6f4'
        }
    },
    defaultEdge: {
        type: 'quadratic',
        // type: 'cubic-vertical',
        size: 1,
        labelCfg: {
            style: {
                fontSize: 8,
            },
        },
        style: {
            stroke: COLOR_LINK_NORMAL,
            lineWidth: 1.5,
            endArrow: {
                path: G6.Arrow.vee(5, 8, 3),
                d: 3,
                fill: COLOR_LINK_NORMAL,
                // lineWidth: 3,
            },
            cursor: "pointer"
            // ... 其他样式属性
        },
    },
    plugins: [tooltip, grid],
    minZoom: 0.1,
    /* styles for different states, there are built-in styles for states: active, inactive, selected, highlight, disable */
    /* you can extend it or override it as you want */
    // comboStateStyles: {
    //   active: {
    //     fill: '#f00',
    //     opacity: 0.5
    //   },
    // },
});

let nodeId = 1;

function DrawComboChart(json_data){
    let package_data = json_data[0]["result"]["nodes"];
    let link_data = json_data[1]["links"];

    let temp_nodes = [];
    let temp_combos = [];
    let temp_edges = [];

    console.log(package_data);

    package_data.forEach(function (item){
        let temp_combo = {};
        temp_combo["id"] = item.id;
        temp_combo["label"] = item.name;
        temp_combo["name"] = item.name;
        temp_combo["node_num"] = item.children.length;
        temp_combo["group_type"] = 'combo';
        temp_combos.push(temp_combo);

        item.children.forEach(function (d){
            let temp_node = {};
            temp_node["id"] = d.id;
            temp_node["name"] = d.name;
            temp_node["size"] = d.size;
            temp_node["group_type"] = 'node';
            temp_node["comboId"] = item.id;
            temp_node["inDegree"] = 80;
            temp_node["degree"] = 160;
            temp_node["index"] = 2;
            temp_node["dependsonDegree"] = 0;
            temp_node["cloneDegree"] = 0;
            temp_node["cochangeDegree"] = 0;
            temp_node["outerNode"] = 0;
            temp_nodes.push(temp_node);
        });
    });

    let splice_index = 0;
    link_data.forEach(function (link){
        let temp_link = {};
        temp_link["source"] = link.source_id;
        temp_link["target"] = link.target_id;
        temp_link["source_name"] = link.source_name;
        temp_link["target_name"] = link.target_name;
        temp_link["curveOffset"] = 50;
        temp_link["index"] = 1;
        temp_link["link_type"] = link.type;
        temp_link["group_type"] = 'edge';
        temp_link["visible"] = link.type === "dependson";
        // temp_link["visible"] = false;
        temp_link["label"] = link.type === "dependson" ? link.dependsOnTypes : "";

        const source_node = temp_nodes.find((n) => n.id === link.source_id);
        const target_node = temp_nodes.find((n) => n.id === link.target_id);

        // console.log(typeof(source_node));
        // console.log(typeof(target_node));

        if(typeof(source_node) !== "undefined" && typeof(target_node) !== "undefined"){
            source_node[link.type + "Degree"] = 1;
            target_node[link.type + "Degree"] = 1;

            if((source_node["outerNode"] === 0 || target_node["outerNode"] === 0) && source_node["comboId"] !== target_node["comboId"]){
                source_node["outerNode"] = 1;
                target_node["outerNode"] = 1;
            }else if(source_node["comboId"] === target_node["comboId"]){
                temp_link["inner_edge"] = 1;
            }
            temp_edges.push(temp_link);
        }
    })

    //
    // temp_edges.forEach(function (item){
    //     const source_node = temp_nodes.find((n) => n.id === item.source);
    //     const target_node = temp_nodes.find((n) => n.id === item.target);
    //
    //     if(typeof(source_node) === "undefined" || typeof(target_node) === "undefined"){
    //         temp_edges.splice(temp_edges.indexOf(item), 1);
    //     }
    // });

    data["nodes"] = temp_nodes;
    data["edges"] = temp_edges;
    data["combos"] = temp_combos;
    console.log(data);

//     data = {
//     nodes: [
//         { id: 'node', comboId: 'combo1', x: 0, y: 0, path:"1234", size: 50, dependsonDegree: 1, cloneDegree: 1, cochangeDegree: 1},
//         { id: 'node1', comboId: 'combo1', x: 50, y: 50, path:"1234", size: 50, dependsonDegree: 1, cloneDegree: 1, cochangeDegree: 1},
//         { id: 'node2', comboId: 'combo1', x: 50, y: 100, path:"1234", size: 50, dependsonDegree: 1, cloneDegree: 1, cochangeDegree: 0},
//         { id: 'node3', comboId: 'combo3', x: 50, y: 400, path:"1234", size: 50, dependsonDegree: 1, cloneDegree: 1, cochangeDegree: 1},
//         { id: 'node4', comboId: 'combo2', x: 150, y: 150, path:"1234", size: 50, dependsonDegree: 1, cloneDegree: 1, cochangeDegree: 0},
//         { id: 'node5', comboId: 'combo2', x: 200, y: 150, path:"1234", size: 50, dependsonDegree: 1, cloneDegree: 1, cochangeDegree: 1},
//         { id: 'node6', comboId: 'combo2', x: 200, y: 200, path:"1234", size: 50, dependsonDegree: 1, cloneDegree: 0, cochangeDegree: 1},
//         // { id: 'node1', comboId: 'combo1', path:"1234", size: 15, inDegree: 80, degree: 160},
//         // { id: 'node2', comboId: 'combo1', path:"1234", size: 15, inDegree: 80, degree: 160},
//         // { id: 'node3', comboId: 'combo3', path:"1234", size: 15, inDegree: 80, degree: 160},
//         // { id: 'node4', comboId: 'combo2', path:"1234", size: 15, inDegree: 80, degree: 160},
//         // { id: 'node5', comboId: 'combo2', path:"1234", size: 15, inDegree: 80, degree: 160},
//         // { id: 'node6', comboId: 'combo2', path:"1234", size: 15, inDegree: 80, degree: 160},
//     ],
//     edges: [
//         // { source: 'node1', target: 'node2', visible: true},
//         // { source: 'node1', target: 'node3', visible: true},
//         { source: 'node', target: 'node1', visible: true},
//         // { source: 'node3', target: 'node2', visible: true},
//         // { source: 'node5', target: 'node3', visible: true},
//         // { source: 'combo1', target: 'combo2', visible: true},
//         // { source: 'combo1', target: 'node2', visible: true},
//     ],
//     // combos: [
//     //     { id: 'combo1', label: 'Combo 1'},
//     //     { id: 'combo2', label: 'Combo 2'},
//     //     { id: 'combo3', label: 'Combo 3'},
//     // ],
// };

    autoLayout();
    G6.Util.processParallelEdges(data["edges"]);

    graph.data(data);
    graph.render();

    const nodes = graph.getNodes();
    // 遍历节点实例，将所有节点提前。
    nodes.forEach((node) => {
        node.toFront();
    });
    // 更改层级后需要重新绘制图
    graph.paint();

    const edge_list = graph.getEdges();

    edge_list.forEach(function (item){
        if(item._cfg.model.inner_edge === 1){
            graph.updateItem(item, EDGE_INNER_MODEL);
        }
    });

    graph.on('combo:mouseenter', (evt) => {
        const { item } = evt;
        graph.setItemState(item, 'active', true);
    });

    graph.on('combo:mouseleave', (evt) => {
        const { item } = evt;
        graph.setItemState(item, 'active', false);
    });

// graph.on('combo:click', (evt) => {
//     const { item } = evt;
//     graph.setItemState(item, 'selected', true);
// });

    graph.on('canvas:click', (evt) => {
        graph.getCombos().forEach((combo) => {
            graph.clearItemStates(combo);
        });
    });

    //节点点击函数
    graph.on('node:click', (evt) => {
        const { item } = evt;
        if(last_click_node === ""){
            let neighbors = item.getNeighbors();

            neighbors.forEach(function (d){
                const model = {
                    type: 'pie-node',
                };
                // console.log(d);
                d.update(model);
            });
            graph.setItemState(item, 'selected', true);

            const node_edges = item.getEdges();

            node_edges.forEach(function (item2){
                graph.updateItem(item2, EDGE_CLICK_MODEL);
            });

            last_click_node = item._cfg.id;
        }else if(last_click_node === item._cfg.id){
            let nodes = graph.findAll('node', (node) => {
                return node.get('currentShape') === "pie-node";
            });

            nodes.forEach(function (d){
                const model = {
                    type: 'circle',
                };
                // console.log(d);
                d.update(model);
            });
            graph.setItemState(item, 'selected', false);

            const node_edges = item.getEdges();

            node_edges.forEach(function (item){
                if(item._cfg.model.inner_edge === 1){
                    graph.updateItem(item, EDGE_INNER_MODEL);
                }else{
                    graph.updateItem(item, EDGE_NORMAL_MODEL);
                }
            });

            last_click_node = "";
        }else{
            let nodes = graph.findAll('node', (node) => {
                return node.get('currentShape') === "pie-node";
            });

            nodes.forEach(function (d){
                const model = {
                    type: 'circle',
                };
                // console.log(d);
                d.update(model);

                const node_edges = d.getEdges();

                node_edges.forEach(function (item){
                    if(item._cfg.model.inner_edge === 1){
                        graph.updateItem(item, EDGE_INNER_MODEL);
                    }else{
                        graph.updateItem(item, EDGE_NORMAL_MODEL);
                    }
                });
            });

            let neighbors = item.getNeighbors();

            neighbors.forEach(function (d){
                const model = {
                    type: 'pie-node',
                };
                // console.log(d);
                d.update(model);
            });
            graph.setItemState(graph.findById(last_click_node), 'selected', false);
            graph.setItemState(item, 'selected', true);

            const node_edges = item.getEdges();

            node_edges.forEach(function (item2){
                graph.updateItem(item2, EDGE_CLICK_MODEL);
            });

            last_click_node = item._cfg.id;
        }

        // graph.setItemState(item, 'active', true);
    });
}

//加载数据
var loadPageData = function () {
    $.ajax({
        type: "GET",
        url: "/project/all/id",
        success: function (result) {
            projectGraphAjax(result);
        }
    });
}

//调用接口请求数据
var projectGraphAjax = function(projectIds){
    $.ajax({
        type:"POST",
        url : "/project/has/combo",
        contentType: "application/json",
        dataType:"json",
        data:JSON.stringify(projectIds),
        success : function(result) {
            DrawComboChart(result);
        }
    });
}

var main = function () {
    return {
        init : function() {
            loadPageData();
        }
    }
}

// const int=self.setInterval(function(){
//     addComboItem();
// },150);

// function addComboItem(){
//     const temp_node = { id: 'nodess' + nodeId, comboId: 'combo1', size: 15, dependsonDegree: 1, cloneDegree: 0, cochangeDegree: 1}
//     data['nodes'].push(temp_node);
//     descriptionDiv.innerHTML = 'nodess' + nodeId;
//
//     graph.data(data);
//     graph.render();
//
//     const temp_combo = graph.findById('combo1')
//     console.log(temp_combo.getNodes());
//     nodeId += 1;
// }

//自动布局
function autoLayout(){
    let combo_list = data["combos"];
    let node_list = data["nodes"];
    let combo_num = combo_list.length;
    let radius = node_list.length * 50 / 6.28 * 0.5;
    let cord = radius * 0.5;
    // console.log(radius);

    let node_index = 0;

    combo_list.forEach((item, index) => {
        let combo_cord = [];
        let outerNodeLineIndex = 1;
        let innerNodeLineIndex = 1;
        let line_node_num = parseInt(Math.sqrt(item.node_num) * 1.5);
        let combo_width = 75 * line_node_num;
        let combo_height = 75 * line_node_num * (1 / 3);

        if(index !== combo_num){
            combo_cord = [radius * Math.cos((index / combo_num) * Math.PI * 2) - combo_width * 0.5 + cord,
                (-radius) * Math.sin((index / combo_num) * Math.PI * 2) - combo_height * 0.5 + cord];
        }else{
            combo_cord = [radius - combo_width * 0.5 + cord,  -(combo_height * 0.5) + cord];
        }

        for(let i = 0; i < item.node_num; i++){
            let temp_node = node_list[node_index];
            let innerLineIndex = Math.ceil(innerNodeLineIndex / line_node_num);
            let outerLineIndex = Math.ceil(outerNodeLineIndex / line_node_num);
            // console.log(innerLineIndex);
            // console.log(outerLineIndex);
            if(temp_node.outerNode === 0){
                temp_node["x"] = combo_cord[0] + 75 * (innerNodeLineIndex % line_node_num);
                temp_node["y"] = combo_cord[1] + 75 * innerLineIndex + combo_height;
                innerNodeLineIndex++;
            }else{
                temp_node["x"] = combo_cord[0] + 75 * (outerNodeLineIndex % line_node_num);
                temp_node["y"] = combo_cord[1] + 75 * outerLineIndex;
                outerNodeLineIndex++;
            }
            node_index++;
        }
    })
}

if (typeof window !== 'undefined')
    window.onresize = () => {
        if (!graph || graph.get('destroyed')) return;
        if (!container || !container.scrollWidth || !container.scrollHeight) return;
        graph.changeSize(container.scrollWidth, container.scrollHeight);
    };