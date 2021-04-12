let data = {};
let projectList_global; //存放选定项目列表
let in_out_list = [] //存放出度入度节点
let actual_edges = [] //存放原有的线以及拆分后的线ID
let smell_data_global = [] //存放异味信息
let smell_hover_nodes = [] //存放当前鼠标悬停异味节点数组
let reliable_dependency_list = [];//存放可确定依赖关系
let unreliable_dependency_list = [];//存放不可信依赖关系
let evt_global; //存放当前的鼠标点击事件
reliable_dependency_list = [];
unreliable_dependency_list = [];
let nodeId = 1;
let last_click_node = "";
let repaint_flag = false; //判断是否为重新绘制

const REGULAR_NODE_SIZE = 25;
const loading_div = $("#loading_div");
const COLOR_DEPENDSON = '#FFA500';
const COLOR_CLONE = '#B04206';
const COLOR_COCHANGE = '#a29bfe';
// const COLOR_LINK_NORMAL = '#f7c8ca';
const COLOR_LINK_NORMAL = '#9aa6d2';
const COLOR_LINK_INNER = '#d8d5d5';
// const COLOR_LINK_CLICK = '#bd0303';
const COLOR_LINK_CLICK = '#1515ff';
const COLOR_SMELL_NORMAL = '#f19083';
const COLOR_SMELL_CLICK = '#bd0303';

const EDGE_CLICK_MODEL = {
    style: {
        stroke: COLOR_LINK_CLICK,
        lineWidth: 2,
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

        if(cfg.hasOwnProperty("pienode")){
            cfg.pienode.forEach(item => {
                if(item.source === last_click_node || item.target === last_click_node){
                    // console.log(item);
                    for(let key in item.dependency){
                        switch (key){
                            case "dependsonDegree":
                                if(typeof(linkTypeNum.find(n => n === COLOR_DEPENDSON)) === "undefined"){
                                    linkTypeNum.push(COLOR_DEPENDSON);
                                }
                                break;
                            case "cloneDegree":
                                if(typeof(linkTypeNum.find(n => n === COLOR_CLONE)) === "undefined"){
                                    linkTypeNum.push(COLOR_CLONE);
                                }
                                break;
                            case "cochangeDegree":
                                if(typeof(linkTypeNum.find(n => n === COLOR_COCHANGE)) === "undefined"){
                                    linkTypeNum.push(COLOR_COCHANGE);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            })
        }

        const radius = cfg.size / 2 - 2; // node radius

        group.addShape('circle', {
            attrs: {
                "r": radius + 2,
                "lineWidth": 6,
                "stroke": '#5f95ff',
                "fill": '#ffffff',
            }
        });

        if(typeof linkTypeNum)
        switch (linkTypeNum.length) {
            case 0:
                return group.addShape('circle', {
                    attrs: {
                        "r": radius + 5,
                        "lineWidth": 6,
                        "stroke": '#5f95ff',
                        "fill": '#ffffff',
                    }
                });
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
                        fill: linkTypeNum[Object.keys(linkTypeNum)[0]],
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
                        fill: linkTypeNum[Object.keys(linkTypeNum)[0]],
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
                        fill: linkTypeNum[Object.keys(linkTypeNum)[1]],
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
                        fill: linkTypeNum[Object.keys(linkTypeNum)[0]],
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
                        fill: linkTypeNum[Object.keys(linkTypeNum)[1]],
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
                        fill: linkTypeNum[Object.keys(linkTypeNum)[2]],
                        cursor: "pointer",
                    },
                    name: 'out-fan-shape',
                });
                // 返回 keyshape
                return fanIn_1;
        }
    },
});

const grid = new G6.Grid();
const tooltip = new G6.Tooltip({
    offsetX: 70,
    offsetY: 150,
    getContent(e) {
        $(".g6-component-tooltip").css({"left": e.clientX + 20 + "px", "top": e.clientY + 20 + "px"});
        const outDiv = document.createElement('div');
        outDiv.style.width = '500px';
        outDiv.innerHTML = e.item.getModel().group_type === 'combo' ?
            `<b class="combo_label">${e.item.getModel().name}</b>
          <ul>
            <li>ID: ${e.item.getModel().id}</li>
            <li>x: ${e.item.getModel().x}</li>
            <li>y: ${e.item.getModel().y}</li>
          </ul>`

            : e.item.getModel().group_type === 'node' ?
                `<b class="combo_label">${e.item.getModel().name}</b>
          <ul>
            <li>ID: ${e.item.getModel().id}</li>
          </ul>` :

                `<b class="combo_label">${e.item.getModel().id}</b>
            <ul>
              <li>source: ${e.item.getModel().source_name}</li>
              <li>source_id: ${e.item.getModel().source}</li>
              <li>target: ${e.item.getModel().target_name}</li>
              <li>target_id: ${e.item.getModel().target}</li>
            </ul>`
        return outDiv
    },
    itemTypes: ['node', 'combo', 'edge']
});  //鼠标悬停提示项
const container = document.getElementById('combo_chart');
const width = container.scrollWidth;
const height = container.scrollHeight || 500;

const contextMenu = new G6.Menu({
    getContent(evt) {
        let item = evt.item._cfg;
        let html = `<ul class="combo_ul">${item.model.id}_${item.model.name}</ul>`;
        if(item.model.smellType === "CyclicDependency"){
            html += `<ul class="combo_li">
            <a class="combo_a" href="/project/has/cyclicDependencyGraph/${item.model.id}" target="_blank">打开异味详情</a>
            </ul>`;
        }
        return html;
    },
    offsetX: 0,
    offsetY: 0,
    itemTypes: ['node'],
});

const graph = new G6.Graph({
    container: 'combo_chart',
    width,
    height,
    fitCenter: true,
    fitView: true,
    defaultNode: {
        type: 'circle',
        size: 10,
        style: {
            cursor: "pointer",
            // fill: "#cce9f8",
            // stroke: "#a0d6f4",
            fill: "rgb(129,236,236)",
            stroke: "rgb(113,151,234)",

        },
    },
    nodeStateStyles: {
        smell_normal: {
            fill: "#ffffff",
            lineWidth: 4,
            shadowBlur: 10,
            shadowColor: COLOR_SMELL_NORMAL,
            stroke: COLOR_SMELL_NORMAL,
            "text-shape": {
                fontWeight: 500
            }
        },
        smell_hover: {
            fill: "#e6714f",
            lineWidth: 4,
            shadowBlur: 10,
            shadowColor: COLOR_SMELL_CLICK,
            stroke: COLOR_SMELL_CLICK,
            "text-shape": {
                fontWeight: 500
            }
        },
        unreliable: {
            fill: "#ffffff",
            lineWidth: 4,
            shadowBlur: 10,
            shadowColor: "#df0e23",
            stroke: "#df0e23",
        },
    },
    groupByTypes: false,
    modes: {
        default: [{
            type: 'drag-combo',
            enableDelegate: true,
        }, 'drag-canvas', 'collapse-expand-combo', 'zoom-canvas'],
    },
    defaultCombo: {
        type: 'rect',
        size: [50, 50],
        labelCfg: {
            position: 'top',
        },
        style: {
            lineWidth : 1,
            stroke : '#2d3436'
        }
    },
    defaultEdge: {
        // type: 'quadratic',
        type: 'cubic-vertical',
        size: 1,
        labelCfg: {
            style: {
                fontSize: 5,
            },
        },
        style: {
            stroke: COLOR_LINK_NORMAL,
            lineWidth: 1.15,
            endArrow: {
                path: G6.Arrow.vee(5, 8, 3),
                d: 3,
                fill: COLOR_LINK_NORMAL,
            },
            cursor: "pointer"
        },
    },
    plugins: [tooltip, contextMenu],
    minZoom: 0.05,
});

function DrawComboChart(json_data){
    data = {};
    smell_data_global = [];
    in_out_list = [];
    actual_edges = [];
    let package_data = json_data[0]["result"]["nodes"];
    let link_data = json_data[1]["links"];
    smell_data_global = json_data[2]["smell"];

    let temp_nodes = [];
    let temp_combos = [];

    package_data.forEach(function (item){
        let temp_combo = {};
        temp_combo["id"] = item.id;
        temp_combo["label"] = item.name;
        temp_combo["name"] = item.name;
        temp_combo["fanIn"] = 0;
        temp_combo["fanOut"] = 0;
        temp_combo["node_num"] = item.children.length;
        temp_combo["group_type"] = 'combo';
        temp_combos.push(temp_combo);

        item.children.forEach(function (d){
            let temp_node = {};
            temp_node["id"] = d.id;
            temp_node["name"] = d.name;
            temp_node["size"] = d.size;
            temp_node["inOutNode"] = 0;
            temp_node["group_type"] = 'node';
            temp_node["comboId"] = item.id;
            temp_node["inDegree"] = 80;
            temp_node["degree"] = 160;
            temp_node["index"] = 2;
            temp_node["outerNode"] = 0;
            temp_node["pienode"] = [];
            temp_node["smellId"] = 0;
            temp_node["smellType"] = "";
            temp_nodes.push(temp_node);
        });
    });

    link_data.forEach(function (link){
        let temp_link = {};
        temp_link["id"] = link.source_id + "_" + link.target_id + "_" + link.type;
        temp_link["source"] = link.source_id;
        temp_link["target"] = link.target_id;
        temp_link["source_name"] = link.source_name;
        temp_link["target_name"] = link.target_name;
        temp_link["source_path"] = link.source_path;
        temp_link["target_path"] = link.target_path;
        temp_link["link_type"] = link.type;
        temp_link["group_type"] = 'edge';
        temp_link["inner_edge"] = 0;
        temp_link["visible"] = link.type === "dependson";
        temp_link["label"] = link.type === "dependson" ? link.dependsOnTypes : "";

        switch (link.type){
            case "dependson":
                temp_link["dependsOnTypes"] = link.dependsOnTypes;
                temp_link["dependsOnTimes"] = link.dependsOnTimes;
                temp_link["dependsOnWeightedTimes"] = link.dependsOnWeightedTimes;
                break;
            case "clone":
                temp_link["value"] = link.value;
                temp_link["cloneRelationType"] = link.cloneRelationType;
                temp_link["cloneType"] = link.cloneType;
                break;
            case "cochange":
                temp_link["coChangeTimes"] = link.coChangeTimes;
                temp_link["node1ChangeTimes"] = link.node1ChangeTimes;
                temp_link["node2ChangeTimes"] = link.node2ChangeTimes;
                break;
            default:
                break;
        }

        const source_node = temp_nodes.find((n) => n.id === link.source_id);
        const target_node = temp_nodes.find((n) => n.id === link.target_id);
        if(source_node != null && target_node != null) {
            temp_link["source_comboId"] = source_node.comboId;
            temp_link["target_comboId"] = target_node.comboId;

            if(typeof(source_node) !== "undefined" && typeof(target_node) !== "undefined"){
                if((source_node["outerNode"] === 0 || target_node["outerNode"] === 0) && source_node["comboId"] !== target_node["comboId"]){
                    source_node["outerNode"] = 1;
                    target_node["outerNode"] = 1;
                }else if(source_node["comboId"] === target_node["comboId"]){
                    temp_link["inner_edge"] = 1;
                }
                actual_edges.push(temp_link);
            }
        }
    })

    data["nodes"] = temp_nodes;
    data["combos"] = temp_combos;
    console.log(data);

    data["edges"] = splitLinks(actual_edges);
    autoLayout();
    graph.data(data);
    graph.render();

    const edge_list = graph.getEdges();
    edge_list.forEach(function (item){
        if(item._cfg.model.inner_edge === 1){
            graph.updateItem(item, EDGE_INNER_MODEL);
        }
    });

    const nodes = graph.getNodes();
    nodes.forEach((node) => {
        node.toFront();
    });
    graph.paint();

    $('#multipleProjectsButton').css('background-color', '#efefef');

    if(repaint_flag === false){
        graph.on('node:mouseenter', (evt) => {
            const { item } = evt;
            const smellId = item._cfg.model.smellId;

            if(smellId !== 0){
                smell_data_global.forEach(smell => {
                    if(smell.id === smellId){
                        smell.nodes.forEach(node_data => {
                            const node = graph.findById(node_data.id.split("_")[1]);
                            if(typeof(node) !== "undefined"){
                                smell_hover_nodes.push(node);
                                graph.setItemState(node, 'smell_hover', true);
                            }
                        })
                    }
                })
            }
        });

        graph.on('node:mouseleave', (evt) => {
            const { item } = evt;

            const smellId = item._cfg.model.smellId;

            if(smellId !== 0) {
                smell_hover_nodes.forEach(node => {
                    graph.setItemState(node, 'smell_hover', false);
                })

                smell_hover_nodes = [];
            }
        });

        graph.on('node:contextmenu', (evt) => {
            $(".g6-component-contextmenu").css({"left": evt.clientX + 20 + "px", "top": evt.clientY + 20 + "px"});
        });

        graph.on('canvas:click', (evt) => {
            graph.getCombos().forEach((combo) => {
                graph.clearItemStates(combo);
            });
        });

        graph.on('drag', (evt) => {
            const nodes = graph.getNodes();
            nodes.forEach((node) => {
                node.toFront();
            });
            graph.paint();
        });


        //节点点击函数
        graph.on('node:click', (evt) => {
            const { item: node_click } = evt;
            console.log(node_click);
            if(last_click_node === ""){
                showRelevantNodeAndEdge(node_click);
            }else if(last_click_node === node_click._cfg.id){
                last_click_node = "";
                deleteRelevantNodeAndEdge(node_click);
            }else{
                const lastClickNode = graph.findById(last_click_node);

                deleteRelevantNodeAndEdge(lastClickNode);
                showRelevantNodeAndEdge(node_click);
            }

            // graph.setItemState(node_click, 'active', true);
        });
        graph.on('edge:click', (evt) => {
            const { item: edge_click } = evt;
            console.log(edge_click);

            // graph.setItemState(node_click, 'active', true);
        });

        graph.on('combo:click', (evt) => {
            const { item: item } = evt;
            console.log(item);
        });

        repaint_flag = true;
    }

    loading_div.html("");
}

//显示与该节点相关的连线和节点
function showRelevantNodeAndEdge(node_click){
    last_click_node = node_click._cfg.id;
    graph.setItemState(node_click, 'selected', true);

    const node_edges = node_click.getEdges();

    node_edges.forEach(function (edge){
        if(edge._cfg.model.inner_edge === 1 || node_click._cfg.model.inOutNode === 1){
            node = getOtherEndNode(edge._cfg.model, node_click._cfg.id);
            updatePieNode(node);
            graph.setItemState(node, 'selected', true);
            graph.updateItem(edge, EDGE_CLICK_MODEL);
        }else{
            // console.log(item2);
            edge._cfg.model.children.forEach(link => {
                node = getOtherEndNode(link, node_click._cfg.id);
                graph.setItemState(node, 'selected', true);
                updatePieNode(node);
                link.split_edges.forEach(n => {
                    graph.updateItem(n.id, EDGE_CLICK_MODEL);
                })
            })
        }
    });
}

//删除与该节点相关的连线和节点
function deleteRelevantNodeAndEdge(node_click){
    graph.setItemState(node_click, 'selected', false);
    const node_edges = node_click.getEdges();

    node_edges.forEach(function (edge){
        if(edge._cfg.model.inner_edge === 1 || node_click._cfg.model.inOutNode === 1){
            node = getOtherEndNode(edge._cfg.model, node_click._cfg.id);
            graph.setItemState(node, 'selected', false);
            deletePieNode(node);
            if(node_click._cfg.model.inOutNode === 1){
                graph.updateItem(edge, EDGE_NORMAL_MODEL);
            }else{
                graph.updateItem(edge, EDGE_INNER_MODEL);
            }

        }else{
            edge._cfg.model.children.forEach(link => {
                node = getOtherEndNode(link, node_click._cfg.id);
                graph.setItemState(node, 'selected', false);
                deletePieNode(node);
                link.split_edges.forEach(n => {
                    graph.updateItem(n.id, EDGE_NORMAL_MODEL);
                })
            })
        }
    });
}

function getOtherEndNode(model, id){
    let node;
    if(model.source === id){
        node = graph.findById(model.target);
    }else{
        node = graph.findById(model.source);
    }

    return node;
}

function filterLinks(){
    let html = "<div style=\"position:fixed;height:100%;width:100%;z-index:10000;background-color: #5a6268;opacity: 0.5\">" +
        "<div class='loading_window' id='Id_loading_window' style=\"left: " + (width - 215) / 2 + "px; top:" + (height - 61) / 2 + "px;\">筛选连线中...</div>" +
        "</div>";
    loading_div.html(html);

    let unreliable_nodes = [];
    if(last_click_node !== ""){
        const lastClickNode = graph.findById(last_click_node);
        deleteRelevantNodeAndEdge(lastClickNode);
    }

    let filter = GetFilterCondition();
    let temp_edges = [];
    actual_edges.forEach(edge =>{
        // reliable_dependency_list.forEach(item => {
        //     if((edge.source === item.node1 && edge.target === item.node2) ||
        //         (edge.source === item.node2 && edge.target === item.node1)){
        //         edge.visible = false;
        //     }
        // })

        unreliable_dependency_list.forEach(item => {
            if((edge.source_path === item.node1 && edge.target_path === item.node2) ||
                (edge.source_path === item.node2 && edge.target_path === item.node1)){
                console.log(item);
                const node1 = graph.findById(edge.source);
                const node2 = graph.findById(edge.target);
                unreliable_nodes.push(node1);
                unreliable_nodes.push(node2);
                // graph.setItemState(node1, 'unreliable', true);
                // graph.setItemState(node2, 'unreliable', true);
            }
        })

        if (filter["dependson"]["checked"] && edge.link_type ==="dependson") {
            let dependson_flag = true;
            if (filter["dependson"]["dependsIntensity"]) {
                let intensityhigh = parseFloat(filter["dependson"]["intensityhigh"]).toFixed(2);
                let intensitybelow = parseFloat(filter["dependson"]["intensitybelow"]).toFixed(2);
                let intensity = edge.dependsOnWeightedTimes/ (edge.dependsOnWeightedTimes + 10.0);
                let temp_flag_intensity = false;

                if (filter["dependson"]["intensityCompareSelectBelow"] === "<=" && intensity >= intensitybelow) {
                    if (filter["dependson"]["intensityCompareSelectHigh"] === "<=" &&
                        intensity <= intensityhigh) {
                        temp_flag_intensity = true;
                    } else if (filter["dependson"]["intensityCompareSelectHigh"] === "<" &&
                        intensity < intensityhigh) {
                        temp_flag_intensity = true;
                    }
                } else if (filter["dependson"]["intensityCompareSelectBelow"] === "<" && intensity > intensitybelow) {
                    if (filter["dependson"]["intensityCompareSelectHigh"] === "<=" &&
                        intensity <= intensityhigh) {
                        temp_flag_intensity = true;
                    } else if (filter["dependson"]["intensityCompareSelectHigh"] === "<" &&
                        intensity < intensityhigh) {
                        temp_flag_intensity = true;
                    }
                }

                if (temp_flag_intensity === false) {
                    dependson_flag = false;
                }
            }

            if (filter["dependson"]["dependsOnTimes"] &&
                filter["dependson"]["dependencyTimes"] > edge.dependsOnTimes &&
                dependson_flag === true) {
                dependson_flag = false;
            }

            if (filter["dependson"]["dependsOnType"] && dependson_flag === true) {
                let value = filter["dependson"]["dependsTypeSelect"];
                let type_list = edge.dependsOnTypes.split("__");
                let temp_dependsType_flag = false;

                if (value.length === 0) {
                    alert("未选中任何类型！");
                }else{
                    value.forEach(type1 => {
                        type_list.forEach(type2 =>{
                            if(type1 === type2){
                                temp_dependsType_flag = true;
                            }
                        })
                    })
                }

                if (temp_dependsType_flag === false) {
                    dependson_flag = false;
                }
            }

            if(dependson_flag === true){
                temp_edges.push(edge);
            }
        }else if (filter["clone"]["checked"] && edge.link_type ==="clone") {
            let clone_flag = true;
            if (filter["clone"]["cloneSimilarity"]) {
                let temp_flag_clonesimilarity = false;
                let cloneValue = edge.value;
                let similarityhigh = parseFloat(filter["clone"]["similarityhigh"]).toFixed(2);
                let similaritybelow = parseFloat(filter["clone"]["similaritybelow"]).toFixed(2);

                if (filter["clone"]["similarityCompareSelectBelow"] === "<=" &&
                    cloneValue >= similaritybelow) {
                    if (filter["clone"]["similarityCompareSelectHigh"] === "<=" &&
                        cloneValue <= similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    } else if (filter["clone"]["similarityCompareSelectHigh"] === "<" &&
                        cloneValue < similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    }
                } else if (filter["clone"]["similarityCompareSelectBelow"] === "<" &&
                    cloneValue > similaritybelow) {
                    if (filter["clone"]["similarityCompareSelectHigh"] === "<=" &&
                        cloneValue <= similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    } else if (filter["clone"]["similarityCompareSelectHigh"] === "<" &&
                        cloneValue < similarityhigh) {
                        temp_flag_clonesimilarity = true;
                    }
                }

                if (temp_flag_clonesimilarity === false) {
                    clone_flag = false;
                }
            }

            if(clone_flag === true){
                temp_edges.push(edge);
            }
        }else if (filter["cochange"]["checked"] && edge.link_type ==="cochange") {
            if (filter["cochange"]["cochangetimes"] >= 3) {
                if (edge.coChangeTimes >= filter["cochange"]["cochangetimes"]) {
                    temp_edges.push(edge);
                }
            } else {
                alert("Cochange Times 需大于等于 3！");
            }
        }
    });
    data["edges"] = splitLinks(temp_edges);
    graph.data(data);
    graph.render();

    const edge_list = graph.getEdges();
    edge_list.forEach(function (item){
        if(item._cfg.model.inner_edge === 1){
            graph.updateItem(item, EDGE_INNER_MODEL);
        }
    });

    const nodes = graph.getNodes();
    nodes.forEach((node) => {
        node.toFront();
    });
    graph.paint();

    unreliable_nodes.forEach(item =>{
        graph.setItemState(item, 'unreliable', true);
    })

    loading_div.html("");
}

//拆分连线为三段
function splitLinks(links_data){
    let temp_nodes = data["nodes"];
    let temp_combos = data["combos"];
    let temp_edges = [];
    links_data.forEach(edge =>{
        const source_node = temp_nodes.find((n) => n.id === edge.source);
        const target_node = temp_nodes.find((n) => n.id === edge.target);

        let source_pienode = source_node["pienode"];
        let target_pienode = target_node["pienode"];

        let temp_source_pienode = source_pienode.find(n => (n.source === edge.source && n.target === edge.target));
        let temp_target_pienode = target_pienode.find(n => (n.source === edge.source && n.target === edge.target));

        if(typeof(temp_source_pienode) === "undefined"){
            let temp = {
                source: edge.source,
                target: edge.target,
                dependency: {}
            };

            temp.dependency[edge.link_type + "Degree"] = 1;
            source_pienode.push(temp);
        }else{
            temp_source_pienode.dependency[edge.link_type + "Degree"] = 1;
        }

        if(typeof(temp_target_pienode) === "undefined"){
            let temp = {
                source: edge.source,
                target: edge.target,
                dependency: {}
            };

            temp.dependency[edge.link_type + "Degree"] = 1;
            target_pienode.push(temp);
        }else{
            temp_target_pienode.dependency[edge.link_type + "Degree"] = 1;
        }

        if(edge.inner_edge === 0){
            let out_edge = temp_edges.find(n =>n.id === edge.source + "_" + edge.source_comboId + "_out");
            let in_edge = temp_edges.find(n =>n.id === edge.target_comboId + "_in" + "_" + edge.target);
            let in_combo = temp_combos.find((n) => n.id === edge.target_comboId);
            let out_combo = temp_combos.find((n) => n.id === edge.source_comboId);

            in_combo.fanIn += 1;
            out_combo.fanOut += 1;

            if(typeof(out_edge) === "undefined"){
                let out_edge_model = {
                    source: edge.source,
                    target: edge.source_comboId + "_out",
                    id: edge.source + "_" + edge.source_comboId + "_out",
                    inner_edge: edge.inner_edge,
                    visible: false,
                    style: {
                        endArrow: false,
                    },
                    children: [
                        {
                            edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                            source: edge.source,
                            target: edge.target,
                            source_name: edge.source_name,
                            target_name: edge.target_name,
                            link_type: edge.link_type,
                            group_type: edge.group_type,
                            inner_edge: edge.inner_edge,
                            visible: edge.visible,
                            label: edge.label,
                            source_comboId: edge.source_comboId,
                            target_comboId: edge.target_comboId,
                            split_edges: [
                                {
                                    id: edge.target_comboId + "_in" + "_" + edge.target,
                                    source: edge.target_comboId + "_in",
                                    target: edge.target,
                                },
                                {
                                    id: edge.source + "_" + edge.source_comboId + "_out",
                                    source: edge.source,
                                    target: edge.source_comboId + "_out",
                                },
                                {
                                    id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                                    source: edge["source_comboId"] + "_out",
                                    target: edge["target_comboId"] + "_in",
                                }
                            ]
                        }
                    ],
                };

                if(edge.visible){
                    out_edge_model.visible = true;
                }

                temp_edges.push(out_edge_model);
            }else{
                out_edge.children.push({
                    edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                    source: edge.source,
                    target: edge.target,
                    source_name: edge.source_name,
                    target_name: edge.target_name,
                    link_type: edge.link_type,
                    group_type: edge.group_type,
                    inner_edge: edge.inner_edge,
                    visible: edge.visible,
                    label: edge.label,
                    source_comboId: edge.source_comboId,
                    target_comboId: edge.target_comboId,
                    split_edges: [
                        {
                            id: edge.target_comboId + "_in" + "_" + edge.target,
                            source: edge.target_comboId + "_in",
                            target: edge.target,
                        },
                        {
                            id: edge.source + "_" + edge.source_comboId + "_out",
                            source: edge.source,
                            target: edge.source_comboId + "_out",
                        },
                        {
                            id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                            source: edge["source_comboId"] + "_out",
                            target: edge["target_comboId"] + "_in",
                        }
                    ]
                });
            }

            if(typeof(in_edge) === "undefined"){
                let in_edge_model = {
                    source: edge.target_comboId + "_in",
                    target: edge.target,
                    id: edge.target_comboId + "_in" + "_" + edge.target,
                    inner_edge: edge.inner_edge,
                    visible: false,
                    children: [{
                        edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                        source: edge.source,
                        target: edge.target,
                        source_name: edge.source_name,
                        target_name: edge.target_name,
                        link_type: edge.link_type,
                        group_type: edge.group_type,
                        inner_edge: edge.inner_edge,
                        visible: edge.visible,
                        label: edge.label,
                        source_comboId: edge.source_comboId,
                        target_comboId: edge.target_comboId,
                        split_edges: [
                            {
                                id: edge.target_comboId + "_in" + "_" + edge.target,
                                source: edge.target_comboId + "_in",
                                target: edge.target,
                            },
                            {
                                id: edge.source + "_" + edge.source_comboId + "_out",
                                source: edge.source,
                                target: edge.source_comboId + "_out",
                            },
                            {
                                id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                                source: edge["source_comboId"] + "_out",
                                target: edge["target_comboId"] + "_in",
                            }
                        ]
                    }],
                };

                if(edge.visible){
                    in_edge_model.visible = true;
                }

                temp_edges.push(in_edge_model);

                temp_edges.push();
            }else{
                in_edge.children.push({
                    edge_id: edge.source + "_" + edge.target + "_" + edge.link_type,
                    source: edge.source,
                    target: edge.target,
                    source_name: edge.source_name,
                    target_name: edge.target_name,
                    link_type: edge.link_type,
                    group_type: edge.group_type,
                    inner_edge: edge.inner_edge,
                    visible: edge.visible,
                    label: edge.label,
                    source_comboId: edge.source_comboId,
                    target_comboId: edge.target_comboId,
                    split_edges: [
                        {
                            id: edge.target_comboId + "_in" + "_" + edge.target,
                            source: edge.target_comboId + "_in",
                            target: edge.target,
                        },
                        {
                            id: edge.source + "_" + edge.source_comboId + "_out",
                            source: edge.source,
                            target: edge.source_comboId + "_out",
                        },
                        {
                            id: edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in",
                            source: edge["source_comboId"] + "_out",
                            target: edge["target_comboId"] + "_in",
                        }
                    ]
                });
            }

            if(typeof(temp_edges.find((n) => (n.target === (edge.target_comboId + "_in")
                && (n.source === edge.source_comboId + "_out")))) === "undefined"){
                let temp_edge = {};
                temp_edge["id"] = edge["source_comboId"] + "_out" + "_" + edge["target_comboId"] + "_in";
                temp_edge["source"] = edge["source_comboId"] + "_out";
                temp_edge["target"] = edge["target_comboId"] + "_in";
                temp_edge["visible"] = false;
                temp_edge["style"] = {
                    lineWidth: 4
                };

                if(edge.visible){
                    temp_edge.visible = true;
                }

                temp_edges.push(temp_edge);
            }
        }else{
            temp_edges.push(edge);
        }
    })

    return temp_edges;
}

function loadSmell(){
    deleteSmell();

    let smell_type_filter = $("input[name='smell_radio']:checked").val();
    smell_data_global.forEach(smell => {
        if(smell.smell_type === smell_type_filter) {
            smell.nodes.forEach(node_data => {
                const node = graph.findById(node_data.id.split("_")[1]);
                if(typeof(node) !== "undefined"){
                    node._cfg.model.smellId = smell.id;
                    node._cfg.model.smellType = smell.smell_type;
                    graph.setItemState(node, 'smell_normal', true);
                }
            })
        }
    })
}

function deleteSmell(){
    const nodes = graph.findAllByState('node', 'smell_normal');
    nodes.forEach((node) => {
        node._cfg.model.smellId = 0;
        node._cfg.model.smellType = "";
        graph.setItemState(node, 'smell_normal', false);
    });
}

//获取当前连线筛选条件
var GetFilterCondition = function(){
    var filter = {}
    var temp_dependson = {}
    var temp_clone = {}
    var temp_cochange = {}

    temp_dependson["checked"] = $("#dependsOn").prop("checked") ? 1 : 0;
    temp_clone["checked"] = $("#clone").prop("checked") ? 1 : 0;
    temp_cochange["checked"] = $("#coChange").prop("checked") ? 1 : 0;

    temp_dependson["dependsIntensity"] = $("#dependsIntensity").prop("checked") ? 1 : 0;
    temp_dependson["intensityCompareSelectBelow"] = $("#intensityCompareSelectBelow").val();
    temp_dependson["intensityCompareSelectHigh"] = $("#intensityCompareSelectHigh").val();
    temp_dependson["intensitybelow"] = $("#intensitybelow").val();
    temp_dependson["intensityhigh"] = $("#intensityhigh").val();

    temp_dependson["dependsOnTimes"] = $("#dependsOnTimes").prop("checked") ? 1 : 0;
    temp_dependson["dependencyTimes"] = $("#dependencyTimes").val();

    temp_dependson["dependsOnType"] = $("#dependsOnType").prop("checked") ? 1 : 0;
    // let value = $("#dependsTypeSelect").val();
    // if(value[0] === "IMPORT"){
    //     value.push("INCLUDE");
    // }
    temp_dependson["dependsTypeSelect"] = $("#dependsTypeSelect").val();

    temp_clone["cloneSimilarity"] = $("#cloneSimilarity").prop("checked") ? 1 : 0;
    temp_clone["similarityCompareSelectBelow"] = $("#similarityCompareSelectBelow").val();
    temp_clone["similarityCompareSelectHigh"] = $("#similarityCompareSelectHigh").val();
    temp_clone["similarityhigh"] = $("#similarityhigh").val();
    temp_clone["similaritybelow"] = $("#similaritybelow").val();

    temp_clone["cloneTimes"] = $("#cloneTimes").prop("checked") ? 1 : 0;
    temp_clone["clonetimes"] = $("#clonetimes").val();

    temp_cochange["cochangeTimes"] = $("#cochangeTimes").prop("checked") ? 1 : 0;
    temp_cochange["cochangetimes"] = $("#cochangetimes").val();

    filter["dependson"] = temp_dependson;
    filter["clone"] = temp_clone;
    filter["cochange"] = temp_cochange;

    return filter;
}

function updatePieNode(node){
    const model = {
        type: 'pie-node',
    };

    node.update(model);
}

function deletePieNode(node){
    const model = {
        type: 'node',
    };

    node.update(model);
}

//加载数据
var loadPageData = function () {
    var projectlist = [];

    $.ajax({
        type : "GET",
        url : "/project/all/name",
        success : function(result) {
            for(let i = 0; i < result.length; i++){
                let name_temp = {};
                name_temp["id"] = result[i].id;
                name_temp["name"] = result[i].name;
                projectlist.push(name_temp);

                let html = ""
                html += "<div class = \"combo_div\"><select id = \"multipleProjectSelect\" class=\"selectpicker\" multiple>";
                for(let i = 0; i < projectlist.length; i++) {
                    if (i === 0) {
                        html += "<option selected=\"selected\" value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                    } else {
                        html += "<option value=\"" + projectlist[i].id + "\"> " + projectlist[i].name + "</option>";
                    }
                }
                html += "</select>";
                html += "<br><button id = \"pckFilterButton\" type=\"button\" class='common_button' style='margin-top: 15px' onclick= showFilterWindow()>筛选项目</button>" +
                    "<button id = \"multipleProjectsButton\" type=\"button\" class='common_button' style='margin-top: 15px; margin-left: 30px' onclick= showMultipleButton()>加载项目</button>" +
                    "<button id = \"clearFilterButton\" type=\"button\" class='common_button' style='margin-top: 15px; margin-left: 30px' onclick= clearFilter()>重置筛选</button></div>";

                html += "<div class = \"combo_div\">"+
                    "<form role=\"form\">" +

                    "<p><label class = \"AttributionSelectTitle\" style = \"margin-right: 44px\">" +
                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOn\" onclick=\"CancelChildrenChecked('dependsOn')\">Dependency：" +
                    "</label>" +

                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsIntensity\" onclick=\"setParentChecked('dependsOn', 'dependsIntensity')\" name = \"dependsOn_children\">" +
                    "<input  class = \"AttributionSelectInput\" id=\"intensitybelow\" value=\"0.8\">" +

                    "<select class = \"AttributionSelectSingleSelect\" id=\"intensityCompareSelectBelow\">" +
                    "<option value=\"<=\" selected = \"selected\"><=</option>" +
                    "<option value=\"<\"><</option></select>" +

                    "<label class = \"AttributionSelectLabel\"> &nbsp;Intensity</label>" +

                    "<select class = \"AttributionSelectSingleSelect\" id=\"intensityCompareSelectHigh\">" +
                    "<option value=\"<=\"><=</option>" +
                    "<option value=\"<\" selected = \"selected\"><</option></select>" +

                    "<input  class = \"AttributionSelectInput\" id=\"intensityhigh\" value=\"1\">" +

                    "<label class = \"AttributionSelectLabel\" style = \"margin-left: 80px\">" +
                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOnTimes\" onclick=\"setParentChecked('dependsOn', 'dependsOnTimes')\" name = \"dependsOn_children\"> Times >= " +
                    "<input  id=\"dependencyTimes\" class = \"AttributionSelectInput\" style='margin-right: 80px' value=\"3\">" +
                    "</label>" +

                    "<label class = \"AttributionSelectLabel\" style = \"margin-right:10px;\">" +
                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"dependsOnType\" onclick=\"setParentChecked('dependsOn', 'dependsOnType')\" name = \"dependsOn_children\"> Dependency Type: " +
                    "</label>" +

                    "<select id = \"dependsTypeSelect\" class=\"selectpicker\" multiple>" +
                    "<option value=\"IMPORT\">IMPORT</option>" +
                    "<option value=\"INCLUDE\">INCLUDE</option>" +
                    "<option value=\"EXTENDS\">EXTENDS</option>" +
                    "<option value=\"IMPLEMENTS\">IMPLEMENTS</option>" +
                    "<option value=\"GLOBAL_VARIABLE\">GLOBAL_VARIABLE</option>" +
                    "<option value=\"MEMBER_VARIABLE\">MEMBER_VARIABLE</option>" +
                    "<option value=\"LOCAL_VARIABLE\">LOCAL_VARIABLE</option>" +
                    "<option value=\"CALL\">CALL</option>" +
                    "<option value=\"ANNOTATION\">ANNOTATION</option>" +
                    "<option value=\"CAST\">CAST</option>" +
                    "<option value=\"CREATE\">CREATE</option>" +
                    "<option value=\"USE\">USE</option>" +
                    "<option value=\"PARAMETER\">PARAMETER</option>" +
                    "<option value=\"THROW\">THROW</option>" +
                    "<option value=\"RETURN\">RETURN</option>" +
                    "<option value=\"IMPLEMENTS_C\">IMPLEMENTS_C</option>" +
                    "<option value=\"IMPLLINK\">IMPLLINK</option>" +
                    "</select>" +
                    "</p>";

                html += "<p><label class = \"AttributionSelectTitle\">" +
                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"clone\" onclick=\"CancelChildrenChecked('clone')\">Clone：" +
                    "</label>" +

                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cloneSimilarity\" onclick=\"setParentChecked('clone', 'cloneSimilarity')\" name = \"clone_children\">" +
                    "<input  class = \"AttributionSelectInput\" id=\"similaritybelow\" value=\"0.7\">" +

                    "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectBelow\">" +
                    "<option value=\"<=\" selected = \"selected\"><=</option>" +
                    "<option value=\"<\"><</option></select>" +

                    "<label class = \"AttributionSelectLabel\"> &nbsp;Clone Value</label>" +

                    "<select class = \"AttributionSelectSingleSelect\" id=\"similarityCompareSelectHigh\">" +
                    "<option value=\"<=\"><=</option>" +
                    "<option value=\"<\" selected = \"selected\"><</option></select>" +

                    "<input  class = \"AttributionSelectInput\" id=\"similarityhigh\" value=\"1\"></p>";

                html += "<p><label class = \"AttributionSelectTitle\">" +
                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"coChange\" onclick=\"CancelChildrenChecked('coChange')\">Co-change：" +
                    "</label>" +
                    "<label class = \"AttributionSelectLabel\">" +
                    "<input style = \"margin-right:10px;\" type=\"checkbox\" id=\"cochangeTimes\" onclick=\"setParentChecked('coChange', 'cochangeTimes')\" name = \"coChange_children\"> Times >= " +
                    "<input class = \"AttributionSelectInput\" id=\"cochangetimes\" value=\"3\">" +
                    "</label></p>";

                html += "<p><label class = \"combo_title\" style = \"margin-right: 30px\">Smell ：</label>" +

                    "<label class = \"combo_label\" >" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_Clone\" value='Clone'> Clone " +
                    "</label>" +

                    "<label class = \"combo_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_CyclicDependency\" value='CyclicDependency'> Cyclic Dependency " +
                    "</label>" +

                    "<label class = \"combo_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_HublikeDependency\" value='HubLikeDependency'> Hublike Dependency " +
                    "</label>" +

                    "<label class = \"combo_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnstableDependency\" value='UnstableDependency'> Unstable Dependency " +
                    "</label>" +

                    "<label class = \"combo_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_UnusedComponent\" value='UnusedComponent'> Unused Component " +
                    "</label>" +

                    "<label class = \"combo_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_ImplicitCrossModuleDependency\" value='ImplicitCrossModuleDependency'> Implicit Cross Module Dependency " +
                    "</label>" +

                    "<label class = \"combo_label\" style = \"margin-left: 40px\">" +
                    "<input name=\"smell_radio\" style = \"margin-right:4px;\" type=\"radio\" id=\"checkbox_GodComponent\" value='GodComponent'> God Component " +
                    "</label>" +

                    "</p>";

                html += "<p><div style=\"margin-top: 10px;\">" +
                    "<button class = \"common_button\" type=\"button\" onclick= filterLinks() >筛选连线</button>" +
                    "<button class = \"common_button\" type=\"button\" onclick= loadSmell() style = \"margin-left: 30px\">加载异味</button>" +
                    "<button class = \"common_button\" type=\"button\" onclick= deleteSmell() style = \"margin-left: 30px\">删除异味</button>" +
                    "<button id = \"unreliableDependencyFile\"class = \"common_button\" type=\"button\" onclick= loadUnreliableDependency() style = \"margin-left: 30px\">加载不可依赖关系</button>" +
                    "<input type=\"file\" accept=\".json\" id=\"unreliable_dependency_file\" onchange='setUnreliableDependency()' style=\"display:none\">" +
                    "</div></p>";

                html += "</form>" +
                    "</div>";

                $("#combo_util").html(html);
                $(".selectpicker").selectpicker({
                    actionsBox:true,
                    countSelectedText:"已选中{0}项",
                    selectedTextFormat:"count > 2"
                })
            }
        }
    })
}

//调用接口请求数据
var projectGraphAjax = function(projectIds){
    var projectList = {};
    var projectIds_array = [];

    for(var i = 0; i < projectIds.length; i++){
        var tempId = {};
        tempId["id"] = projectIds[i];
        projectIds_array.push(tempId);
    }

    projectList["projectIds"] = projectIds_array;

    $.ajax({
        type:"POST",
        url : "/project/has/combo",
        contentType: "application/json",
        dataType:"json",
        data:JSON.stringify(projectList),
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

//自动布局
function autoLayout(){
    let combo_list = data["combos"];
    let node_list = data["nodes"];
    let combo_num = combo_list.length;
    let radius = node_list.length * REGULAR_NODE_SIZE / 6.28 * 0.5;
    let cord = radius * 0.5;
    let left = [], right = [];
    let node_index = 0;

    combo_list.forEach((item, index) => {
        if(index < combo_list.length / 2){
            left.push(item);
        }else{
            right.push(item);
        }
    })

    left.sort(function(a,b){
        return a.fanOut / (a.fanIn + a.fanOut) - b.fanOut / (b.fanIn + b.fanOut);
    });
    right.sort(function(a,b){
        return b.fanOut / (b.fanIn + b.fanOut) - a.fanOut / (a.fanIn + a.fanOut);
    });

    let combo_list_sort = left.concat(right);
    data["combos"] = combo_list_sort;
    // console.log(combo_list_sort);

    combo_list.forEach((item, index) => {
        let combo_cord;
        let outerNodeLineIndex = 1;
        let innerNodeLineIndex = 1;
        let line_node_num = parseInt(Math.sqrt(item.node_num) * 1.5);
        let combo_width = REGULAR_NODE_SIZE * 1.5 * line_node_num;
        let combo_height = REGULAR_NODE_SIZE * 1.5 * line_node_num;
        let combo_radio = index / combo_num; //当前combo处于总布局圆圈的比例
        let model1x, model1y, model2x, model2y;

        if(index !== combo_num){
            let stretch_radio;
            if(combo_radio < 0.5){
                stretch_radio = (0.25 - combo_radio) / 0.25;
            }else{
                stretch_radio = (combo_radio - 0.75) / 0.25;
            }
            combo_cord = [radius * Math.cos(combo_radio * Math.PI * 2) - combo_width * 0.5 + (1 + stretch_radio * 1.6) * cord,
                (-radius) * Math.sin(combo_radio * Math.PI * 2) - combo_height * 0.5 + cord];
        }else{
            combo_cord = [radius - combo_width * 0.5 + cord,  -(combo_height * 0.5) + cord];
        }

        for(let i = 0; i < item.node_num; i++){
            // if(node_list[node_index].comboId === item.id){
            let temp_node = node_list[node_index];
            let innerLineIndex = Math.ceil(innerNodeLineIndex / line_node_num);
            let outerLineIndex = Math.ceil(outerNodeLineIndex / line_node_num);
            // console.log(innerLineIndex);
            // console.log(outerLineIndex);
            if(temp_node.outerNode === 0){ //内部节点
                if(combo_radio <= 0.5){//放置在上面
                    if(combo_radio <= 0.25){
                        temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * innerLineIndex - combo_height;
                        innerNodeLineIndex++;
                    }else{
                        temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * innerLineIndex - combo_height;
                        innerNodeLineIndex++;
                    }
                }else{
                    if(combo_radio <= 0.75){
                        temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * innerLineIndex + combo_height;
                        innerNodeLineIndex++;
                    }else{
                        temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (innerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * innerLineIndex + combo_height;
                        innerNodeLineIndex++;
                    }

                }
            }else{ //外部节点
                if(combo_radio <= 0.5) {
                    if(combo_radio <= 0.25) {
                        temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                        outerNodeLineIndex++;
                    }else{
                        temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] - REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                        outerNodeLineIndex++;
                    }
                }else{
                    if(combo_radio <= 0.75) {
                        temp_node["x"] = combo_cord[0] - REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                        outerNodeLineIndex++;
                    }else{
                        temp_node["x"] = combo_cord[0] + REGULAR_NODE_SIZE * 1.5 * (outerNodeLineIndex % line_node_num);
                        temp_node["y"] = combo_cord[1] + REGULAR_NODE_SIZE * 1.5 * outerLineIndex;
                        outerNodeLineIndex++;
                    }
                }
            }
            node_index++;
            // }
        }

        if(combo_radio <= 0.5){
            if(combo_radio <= 0.25){
                model1x = combo_cord[0] + combo_width * 0.5;
                model1y = combo_cord[1] + REGULAR_NODE_SIZE;
                model2x = combo_cord[0] - REGULAR_NODE_SIZE;
                model2y = combo_cord[1] - combo_height * 0.3;
            }else{
                model1x = combo_cord[0] - combo_width * 0.5;
                model1y = combo_cord[1] + REGULAR_NODE_SIZE;
                model2x = combo_cord[0] + REGULAR_NODE_SIZE;
                model2y = combo_cord[1] - combo_height * 0.3;
            }
        }else{
            if(combo_radio <= 0.75){
                model1x = combo_cord[0] - combo_width * 0.5;
                model1y = combo_cord[1] - REGULAR_NODE_SIZE;
                model2x = combo_cord[0] + REGULAR_NODE_SIZE;
                model2y = combo_cord[1] + combo_height * (1 / 3);
            }else{
                model1x = combo_cord[0] + combo_width * 0.5;
                model1y = combo_cord[1] - REGULAR_NODE_SIZE;
                model2x = combo_cord[0] - REGULAR_NODE_SIZE;
                model2y = combo_cord[1] + combo_height * (1 / 3);
            }
        }

        let model1 = {
            id: item.id + "_in",
            size: REGULAR_NODE_SIZE / 3,
            inOutNode: 1,
            comboId: item.id,
            x: model1x,
            y: model1y,
            style: {
                fill: "#f18c6f",
                stroke: "#f3623a"
            }
        }

        let model2 = {
            id: item.id + "_out",
            size: REGULAR_NODE_SIZE / 3,
            inOutNode: 1,
            comboId: item.id,
            x: model2x,
            y: model2y,
            style: {
                fill: "#f18c6f",
                stroke: "#f3623a"
            }
        }

        node_list.push(model1);
        node_list.push(model2);
        //
        // graph.addItem('node', model1);
        // graph.addItem('node', model2);
    })
}

function showMultipleButton(){
    let value = $('#multipleProjectSelect').val();
    $('#multipleProjectsButton').css('background-color', '#f84634');
    projectList_global = [];
    projectList_global = value;
    let html = "<div style=\"position:fixed;height:100%;width:100%;z-index:10000;background-color: #5a6268;opacity: 0.5\">" +
        "<div class='loading_window' id='Id_loading_window' style=\"left: " + (width - 215) / 2 + "px; top:" + (height - 61) / 2 + "px;\">调用数据接口...</div>" +
        "</div>";
    loading_div.html(html);
    projectGraphAjax(value);
}

//重置筛选
function clearFilter(){
    $.ajax({
        url: "/project/clearfilter",
        type: "GET",
        success: function (result) {
            if (result.result === "success") {
                alert("重置成功");
            } else {
                alert("重置失败！");
            }
        }
    });
}

//筛选项目
function showFilterWindow(){
    let html = "<div class=\"div_treeview\">" +
        "<h4>项目结构</h4><button class=\"btn pull-right\" id=\"buttonPackageFilter\">设置</button>" +
        "<p><i id='iconProject'></i></p>" +
        "<div class=\"div_treeview_content\">" +
        "<!-- 包括项目、项目的基本结构 -->" +
        "<ul id=\"treeProjects\" class=\"ztree\"></ul>" +
        "</div>" +
        "<div id=\"treeProjectsPage\" style=\"text-align: center;\">" +
        "</div>" +
        "</div>";

    let win = new Window({

        width: 800, //宽度
        height: 600, //高度
        title: '筛选项目', //标题
        content: html, //内容
        isMask: true, //是否遮罩
        isDrag: true, //是否移动
    });

    _project();

    //筛选文件目录按钮
    $("#buttonPackageFilter").click(function() {
        let projectZTreeObj = $.fn.zTree.getZTreeObj("treeProjects");
        let checkCount = projectZTreeObj.getCheckedNodes(true);
        let ids = [];
        let j = 0;
        for (let i = 0; i < checkCount.length; i++) {
            if (checkCount[i].type == "Package") {
                ids[j++] = {
                    type: "pck",
                    id: checkCount[i].id
                };
            } else if (checkCount[i].type == "FileList") {
                ids[j++] = {
                    type: "FileList",
                    id: checkCount[i].getParentNode().id
                };
            }else if(checkCount[i].type == "PckList"){
                ids[j++] = {
                    type: "PckList",
                    id: checkCount[i].getParentNode().id
                };
            }
        }
        $.ajax({
            url: "/project/pckfilter",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(ids),
            success: function (result) {
                if (result.result === "success") {
                    let paths = "";
                    for(let i = 0; i < result.length; i ++) {
                        paths += (i+1) + "." + result.path[i] + '\n';
                    }
                    alert("设置成功 " + result.length + " 个路径！\n" +
                        "分别为：\n" + paths);
                } else {
                    alert("设置失败！");
                }
            }
        });
    })
}

//加载不可依赖关系
function loadUnreliableDependency(){
    document.getElementById("unreliable_dependency_file").click();
}

//设置不可依赖关系
function setUnreliableDependency(){
    let resultFile = $('#unreliable_dependency_file')[0].files[0];
    let reader = new FileReader();
    reader.readAsText(resultFile, "utf8");
    reader.onload = function(){
        let temp_list = JSON.parse(this.result);
        temp_list.forEach(function (item){
            unreliable_dependency_list.push(item);
        });
        console.log(unreliable_dependency_list);
    }
}

//筛选项目框内项目树结构
function showZTree(nodes, container = $("#ztree")) {
    let setting = {
        check: {
            enable: true,
            chkStyle: "checkbox",
            chkboxType: {
                "Y":"","N":"s"
            }
        },
        data: {
            keep: {
                parent: true
            }
        },
        callback: {
            onClick: function(event, treeId, treeNode) {
                let id = treeNode.id;
                if(id <= 0) {
                    return ;
                }
                let type = treeNode.type;
                if(type == "Project") {
                    window.open("/project/index?id=" + id);
                }
            },
            onCheck: function(event, treeId, treeNode) {
                let type = treeNode.type;
                if(treeNode.checked == true){
                    if(type == "Package"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.setChkDisabled(child, true, false, true);
                        })
                        let pckList = treeNode.getParentNode();
                        if(pckList.getParentNode() != null){
                            treeObj.setChkDisabled(pckList.getParentNode(), true, true, false);
                        }
                    }else if(type == "PckList"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        treeObj.setChkDisabled(treeNode.getParentNode(), true, true, false);
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.checkNode(child, true, true, true);
                        })
                    }else if(type == "FileList"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        treeObj.setChkDisabled(treeNode.getParentNode(), true, true, false);
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.checkNode(child, true, true, false);
                        })
                    }
                }else{
                    if(type == "Package"){
                    let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                    let children = treeNode.children;
                    children.forEach(child =>{
                        treeObj.setChkDisabled(child, false, false, true);
                    });
                    let noCheckedChildren = true;
                    let cousins = treeNode.getParentNode().children;
                    cousins.forEach(cousin =>{
                        if(cousin.checked == true){
                            noCheckedChildren = false;
                        }
                    });
                    if(noCheckedChildren == true){
                        treeObj.setChkDisabled(treeNode.getParentNode(), false, true, false);
                    }
                    let pckListNode = treeNode.getParentNode();
                    if(pckListNode != null){
                        if(pckListNode.checked == true){
                            treeObj.checkNode(pckListNode, false, false, false);
                        }
                    }
                }else if(type == "PckList"){
                    let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                    let children = treeNode.children;
                    children.forEach(child =>{
                        treeObj.checkNode(child, false, true, true);
                    });
                    let noCheckedChildren = true;
                    let cousins = treeNode.getParentNode().children;
                    cousins.forEach(cousin =>{
                        if(cousin.checked == true){
                            noCheckedChildren = false;
                        }
                    });
                    if(noCheckedChildren == true){
                        treeObj.setChkDisabled(treeNode.getParentNode(), false, true, false);
                    }
                }else if(type == "FileList"){
                        let treeObj = $.fn.zTree.getZTreeObj("treeProjects");
                        let children = treeNode.children;
                        children.forEach(child =>{
                            treeObj.checkNode(child, false, true, true);
                        })
                        let noCheckedChildren = true;
                        let cousins = treeNode.getParentNode().children;
                        cousins.forEach(cousin =>{
                            if(cousin.checked == true){
                                noCheckedChildren = false;
                            }
                        });
                        if(noCheckedChildren == true){
                            treeObj.setChkDisabled(treeNode.getParentNode(), false, true, false);
                        }
                    }
                }

            }
        }

    };
    let zNodes = nodes;
    $.fn.zTree.init(container, setting, zNodes);
}
//项目树结构
function _project() {

    let showProjectZTree = function(projectIds) {
        $("#iconProject").text("搜索中...");

        $.ajax({
            type:"POST",
            url : "/project/all/ztree/project",
            contentType: "application/json",
            dataType:"json",
            data:JSON.stringify(projectIds),
            success : function(result) {
                if(result.result == "success") {
                    showZTree(result.values, $("#treeProjects"));
                    $("#iconProject").text("");
                }
            }
        });
    }

    let value = $('#multipleProjectSelect').val();
    showProjectZTree(value);
}

//筛选框子控件随着母控件一同取消点选
function CancelChildrenChecked(parent_name){
    if(!$("#" + parent_name).is(":checked")){
        $("input[name = '" + parent_name + "_children" + "']").prop("checked", false);
    }
}

//筛选框母控件随着子控件一同点选
function setParentChecked(parent_name, children_id){
    if($("#" + children_id).is(":checked") && !$("#" + parent_name).is(":checked")){
        $("#" + parent_name).prop("checked", true);
    }
}

if (typeof window !== 'undefined'){
    window.onresize = () => {
        if (!graph || graph.get('destroyed')) return;
        if (!container || !container.scrollWidth || !container.scrollHeight) return;
        graph.changeSize(container.scrollWidth, container.scrollHeight);
    };
}

// $('#unreliable_dependency_file').onchange = function(){
//     let resultFile = this.files[0];
//     console.log('文件信息---',resultFile)
//     // if (resultFile[0]) {
//     //     let reader = new FileReader();
//     //     reader.readAsDataURL(resultFile[0]);
//     //     reader.onload = function (e) {
//     //         $('.previewImg').attr('src',this.result);
//     //     }
//     // }
// };
