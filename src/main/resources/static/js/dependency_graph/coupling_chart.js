let show_panel = true;
let show_edge_label = true;
let show_panel_btm = false;
let is_ntb_loaded = false;
let max_level = -1;
let CHART_MODE = "package";
const loading_div = $("#loading_div");
let NOF = 0;
let LOC = 0;
let CList = [];
let Csum = 0.0;
let Cmax = 0.0;
let Cmin = 10000.0;
let IList = [];
let Isum = 0.0;
let Imax = 0.0;
let Imin = 10000.0;
let current_panel = 'D';
let C90 = 0.0;
let data = {};
let selected_packages = [];
let present_packages = [];
let data_stack = [];
let search_node_stack = [];
let edge_label_map = new Map();
let edge_table1_data = [];
let node_table1_data = [];
const INSTABILITY_COLOR1 = "#642924";
const INSTABILITY_COLOR2 = "#8B3830";
const INSTABILITY_COLOR3 = "#B2463C";
const INSTABILITY_COLOR4 = "#C86259";
const INSTABILITY_COLOR5 = "#D6857E";
const COLOR_LINK_NORMAL = '#8e99c7';
const COLOR_LINK_HIGH_INTENSITY = '#3657cd';
const COLOR_LINK_LOW_INTENSITY = '#b5c2f1';
const COLOR_LINK_SPECIAL = '#f25f3f';
const COLOR_LINK_EXTENDS_AND_IMPLEMENTS = '#4393ee';

const container = document.getElementById('coupling_chart');
const width = container.scrollWidth;
const height = container.scrollHeight || 500;
const FILE_NODE_TABLE_COLS = [[
    {type:'checkbox'}
    ,{field:'id', title: 'ID', sort: true}
    ,{field:'name', title: '名称'}
    ,{field:'LOC', title: '代码行', sort: true}
    ,{field:'nodeType', title: '类型', sort: true}
]];
const PKG_NODE_TABLE_COLS = [[
    {type:'checkbox'}
    ,{field:'id', title: 'ID', sort: true}
    ,{field:'name', title: '名称'}
    ,{field:'NOF', title: '文件', sort: true}
    ,{field:'LOC', title: '代码行', sort: true}
    ,{field:'nodeType', title: '类型', sort: true}
    ,{field:'I', title: 'I-90分位值', sort: true}
    ,{field:'D', title: 'D-90分位值', sort: true}
    ,{field:'C', title: 'C-90分位值', sort: true}
    ,{field:'unfoldable', title: '可展开', sort: true}
]];
const LOW_INTENSITY_EDGE_MODEL = {
    style:{
        stroke: COLOR_LINK_LOW_INTENSITY,
        lineWidth: 0.7,
    }
}
const HIGH_INTENSITY_EDGE_MODEL = {
    style:{
        stroke: COLOR_LINK_HIGH_INTENSITY,
        lineWidth: 1.8,
    }
}

const tooltip = new G6.Tooltip({
    offsetX: 10,
    offsetY: 10,
    itemTypes: ['node', 'edge'],
    getContent: (e) => {
        const outDiv = document.createElement('div');
        outDiv.style.width = 'fit-content';
        if(e.item._cfg.type === "node"){
            if(e.item._cfg.model.nodeType === "file"){
                outDiv.innerHTML = `
              <h4>${e.item._cfg.id}</h4>
              <ul>
                <li>name: ${e.item.getModel().name}</li>
              </ul>
              <ul>
                <li>path: ${e.item.getModel().path}</li>
              </ul>
              <ul>
                <li>level: ${e.item.getModel().level}</li>
              </ul>
              <ul>
                <li>instability: ${e.item.getModel().instability}</li>
              </ul>`;
            }else if(e.item._cfg.model.nodeType === "package"){
                outDiv.innerHTML = `
              <h4>${e.item._cfg.id}</h4>
              <ul>
                <li>name: ${e.item.getModel().name}</li>
              </ul>
              <ul>
                <li>path: ${e.item.getModel().path}</li>
              </ul>
              <ul>
                <li>files num: ${e.item.getModel().NOF}</li>
              </ul>
              <ul>
                <li>instability: ${e.item.getModel().instability}</li>
              </ul>
              <ul>
                <li>Loose Degree: ${e.item.getModel().LooseDegree}</li>
              </ul>`;
            }
        }else if(e.item._cfg.type === "edge"){
            let selectedEdge = e.item._cfg.model;
            const sourceNodeType = e.item._cfg.sourceNode._cfg.model.nodeType;
            if (sourceNodeType === 'package') {
                outDiv.innerHTML = `
                      <h4><b>tag</b>: ${selectedEdge.source}_${selectedEdge.target}</h4>
                      <ul>
                        <li><b>依赖面耦合度(C)</b>: ${selectedEdge.C}</li>
                      </ul>
                      <ul>
                        <li><b>依赖实例数(D(logD))</b>: ${selectedEdge.D}(${selectedEdge.logD})</li>
                      </ul>`;
            } else {
                outDiv.innerHTML = `
                      <h4><b>tag</b>: ${selectedEdge.source}_${selectedEdge.target}</h4>
                      <ul>
                        <li><b>dependsOnTypes</b>: ${selectedEdge.dependsOnTypes}</li>
                      </ul>
                      <ul>
                        <li><b>依赖面耦合度(C)</b>: ${selectedEdge.C}</li>
                      </ul>   
                      <ul>
                        <li><b>依赖实例数(D)</b>: ${selectedEdge.D}</li>
                      </ul>`;
            }
            // outDiv.innerHTML = `
            //   <h4>${e.item.getModel().source}_${e.item.getModel().target}</h4>
            //   <ul>
            //     <li>dependsOnTypes: ${e.item.getModel().dependsOnTypes}</li>
            //   </ul>
            //   <ul>
            //     <li>耦合强度(I): ${e.item.getModel().I}</li>
            //   </ul>
            //   <ul>
            //     <li>fileNumHMean: ${e.item.getModel().fileNumHMean}</li>
            //   </ul>
            //   <ul>
            //     <li>D: ${e.item.getModel().D}</li>
            //   </ul>
            //   <ul>
            //     <li>dist: ${e.item.getModel().dist}</li>
            //   </ul>`;
        }
        return outDiv;
    },
});
const contextMenu = new G6.Menu({
    getContent(evt) {
        let item = evt.item._cfg;
        let html = `<ul class="combo_ul">${item.model.id}_${item.model.name}</ul>`;
        if(CHART_MODE === 'package'){
            html += `<ul class="combo_li">
            <a class="combo_a" href="/hierarchical_clustering/package/${item.model.id}" target="_blank">打开包聚类详情</a>
            </ul>`;
        }
        return html;
    },
    offsetX: 0,
    offsetY: 0,
    itemTypes: ['node'],
});
const toolbar = new G6.ToolBar({
    // container: tc,
    className: 'g6-toolbar-ul',
    getContent: () => {
        return `
      <ul>
        <li code='back'>返回上一层</li>
        <li code='choose'>选择</li>
        <li code='unfold'>展开</li>
        <li code='unfoldFile'>展开到文件页面</li>
      </ul>
    `;
    },
    handleClick: (code, graph) => {
        if (code === 'unfold') {
            unfoldPkg();
        }else if(code === 'choose'){
            if(selected_packages.length === 0){
                layer.msg("当前未选中节点！");
            }else{
                if(graph.save().nodes.length !== 1) {
                    data_stack.push(graph.save());
                }
                present_packages.length = 0;
                let deleteNodes = [];
                // present_packages = selected_packages;

                selected_packages.forEach(node =>{
                    present_packages.push(node);
                    graph.clearItemStates(node);
                })

                graph.getNodes().forEach(node => {
                    let flag = true;
                    present_packages.forEach(node2 => {
                        if(node._cfg.id === node2._cfg.id){
                            flag = false;
                        }
                    })

                    if(flag){
                        let edges = node.getEdges();
                        deleteNodes.push(node);

                        edges.forEach(edge => {
                            graph.removeItem(edge, false);
                        })
                    }
                })

                deleteNodes.forEach(node => {
                    graph.removeItem(node, false);
                })

                selected_packages.length = 0;

                loadPanel(false);
            }
        }else if(code === 'unfoldFile'){
            let json = {};
            let pckIds = [];
            CHART_MODE = "file";

            selected_packages.forEach(node => {
                if (node._cfg.model.nodeType === "package") {
                    pckIds.push({
                        "id": node._cfg.id
                    })
                }
            })
            if (pckIds.length === 0) {
                layer.msg("当前未选中节点！");
                return;
            }
            showLoadingWindow("加载中...");

            json["pckIds"] = pckIds;
            $.ajax({
                url: "/coupling/group/files_of_packages",
                type: "POST",
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(json),
                success: function (result) {
                    data_stack.push(graph.save());
                    data = result;
                    const nodes = data["nodes"];
                    max_level = nodes[nodes.length-1].level;
                    loadGraph();
                }
            });
        }else if (code === 'back') {
            if (data_stack.length !== 0) {
                data = data_stack.pop();
                loadGraph();
            }
        }
    },
});

const graph = new G6.Graph({
    container: 'coupling_chart',
    width,
    height,
    fitView: true,
    modes: {
        default: ['drag-canvas', 'drag-node', 'zoom-canvas', 'click-select', {type: 'brush-select', trigger: 'ctrl', includeEdges: false}, 'activate-relations', { type: "zoom-canvas", enableOptimize: true }],
    },
    // layout: {
    //     type: 'dagre',
    //     sortByCombo: true,
    //     ranksep: 10,
    //     nodesep: 20,
    // },
    defaultNode: {
        type: 'circle',
        size: 20,
        style: {
            cursor: "pointer",
            // fill: "#cce9f8",
            // stroke: "#a0d6f4",
            fill: "rgb(129,236,236)",
            stroke: "rgb(113,151,234)",

        },
        labelCfg: {
            style: {
                fill: '#1890ff',
                fontSize: 5,
                background: {
                    fill: '#ffffff',
                    stroke: '#9EC9FF',
                    padding: [2, 2, 2, 2],
                    radius: 2,
                },
            },
            position: 'bottom',
        }
    },
    defaultEdge: {
        type: 'quadratic',
        labelCfg: {
            autoRotate: true,
            style: {
                fill: 'red',
                fontSize: 5,
                stroke: 'yellow',
                textBaseline: 'bottom'
            }
        },
        // type: 'cubic-vertical',
        // size: 1,
        // labelCfg: {
        //     style: {
        //         fontSize: 5,
        //     },
        // },
        style: {
            stroke: COLOR_LINK_NORMAL,
            lineWidth: 1,
            endArrow: {
                path: G6.Arrow.vee(5, 8, 3),
                d: 3,
                fill: COLOR_LINK_NORMAL,
            },
            cursor: "pointer"
        },
    },
    defaultCombo: {
        type: 'rect',
        style: {
            fill: "#C4E3B2",
            stroke: "#C4E3B2",
            fillOpacity: 0.1,
        },
    },
    edgeStateStyles: {
        highlight: {
            stroke: '#bc2704',
        },
        reverse: {
            stroke: COLOR_LINK_SPECIAL,
        },
    },
    nodeStateStyles: {
        highlight: {
            opacity: 1
        },
        dark: {
            opacity: 0.1
        }
    },
    plugins: [tooltip, toolbar, contextMenu],
    minZoom: 0.05,
});

function handleEdgeLabelDisplay() {
    if (graph.getEdges().length === 0) {
        layer.msg(" 图中没有边！");
        return;
    }
    if (show_edge_label) {
        show_edge_label = false;
        graph.getEdges().forEach(function (edge) {
            if(edge._cfg.model.label !== undefined) {
                edge_label_map.set(edge._cfg.id, edge._cfg.model.label);
                edge._cfg.model.label = '';
                graph.refreshItem(edge);
            }
        });
    } else {
        graph.getEdges().forEach(function (edge){
            edge._cfg.model.label = edge_label_map.get(edge._cfg.id);
            graph.refreshItem(edge);
        });
        show_edge_label = true;
    }
}
function handleResetSearchFile() {
    if (search_node_stack.length !== 0) {
        const nodeData = search_node_stack.pop();
        let targetNode = graph.findById(nodeData.id);
        let model = targetNode._cfg.model;
        model.style.fill = nodeData.style;
        graph.updateItem(targetNode, model, false);
        document.getElementById("file_name").value = '';
    }
}
function handleSearchFile() {
    const fileName = document.getElementById("file_name").value.trim();
    const targetNode = graph.find('node', (node) => {
        return node.get('model').label === fileName && node.get('visible') === true;
    });
    if (targetNode === undefined) {
        layer.msg("未找到文件"+fileName);
        return;
    }
    graph.focusItem(targetNode, true);
    // search_node_stack.push(graph.save());
    search_node_stack.push({id: targetNode._cfg.id, style: targetNode._cfg.model.style.fill});
    let model = targetNode._cfg.model;
    model.style.fill = "#ffffff";
    graph.updateItem(targetNode, model, false);
}
function handleLeftPanelBtn() {
    if (show_panel) {
        let panelContainer = document.getElementById("data_panel_container");
        let chartContainer = document.getElementById("chart_container");
        panelContainer.style.display = "none";
        chartContainer.style.left = "0";
        graph.changeSize(300+graph.getWidth(), graph.getHeight());
        show_panel = false;
        document.getElementById("panel_btn_icon")
            .className = "layui-icon layui-icon-left";
    } else {
        let panelContainer = document.getElementById("data_panel_container");
        let chartContainer = document.getElementById("chart_container");
        panelContainer.style.display = "block";
        chartContainer.style.left = "300px";
        graph.changeSize(graph.getWidth()-300, graph.getHeight());
        show_panel = true;
        document.getElementById("panel_btn_icon")
            .className = "layui-icon layui-icon-right";
    }
}
function calcAbsGComplexity(k, w) {
    let edgeSet = new Set(graph.getEdges());
    let finalW = 0.0;
    graph.getEdges().forEach(edge => {
        if (!isNaN(edge._cfg.model[w])) {
            const reverseId = edge._cfg.model.target + "_" + edge._cfg.model.source;
            const reverseEdge = graph.findById(reverseId);
            if (reverseEdge !== undefined && edgeSet.has(reverseEdge) && edgeSet.has(edge)) {
                const forwardW = Math.max(edge._cfg.model[w], reverseEdge._cfg.model[w]);
                const backwardW = Math.min(edge._cfg.model[w], reverseEdge._cfg.model[w]);
                finalW += forwardW + backwardW * k;
                edgeSet.delete(reverseEdge);
            } else if (reverseEdge === undefined && edgeSet.has(edge)) {
                finalW += parseFloat(edge._cfg.model[w]);
            }
            edgeSet.delete(edge);
        }

    });
    // special relations
    const r = 0.5;
    const c = 0.5;
    const theta = 0.5 * graph.getHeight();
    let reverseW = 0.0;
    let crossW = 0.0;
    graph.getEdges().forEach(edge => {
       if (edge._cfg.states.length !== 0) {
           reverseW += edge._cfg.model[w] * r;
       }
       const startLevel = edge._cfg.source._cfg.model.y;
       const endLevel = edge._cfg.target._cfg.model.y;
       if (endLevel - startLevel > theta) {
           crossW += edge._cfg.model[w] * c;
       }
    });
    finalW += reverseW + crossW;
    return finalW;
}
function hideBottomPanel() {
    let panelContainer = document.getElementById("btm_panel");
    let chartContainer = document.getElementById("chart_container");
    chartContainer.style.paddingBottom = "44px";
    chartContainer.style.marginBottom = "0";
    panelContainer.style.height = "44px";
    show_panel_btm = false;
    document.getElementById("panel_btn_icon_btm")
        .className = "layui-icon layui-icon-down";
    var element = layui.element;
    element.tabChange('btm_tab', 'edge_tab');
}
function handleBottomPanelBtn() {
    if (show_panel_btm) {
        hideBottomPanel();
    } else {
        let panelContainer = document.getElementById("btm_panel");
        let chartContainer = document.getElementById("chart_container");
        panelContainer.style.height = "400px";
        panelContainer.style.zIndex = "901";
        chartContainer.style.paddingBottom = "0";
        chartContainer.style.marginBottom = "400px";
        show_panel_btm = true;
        document.getElementById("panel_btn_icon_btm").className = "layui-icon layui-icon-up";
    }
}

function main() {
    return {
        init : function() {
            showLoadingWindow("加载中...");
            instabilityAjax();
        }
    }
}
function getNodeOneStepDetail(node) {
    let statistics = {
        I: 0,
        D: 0,
        C: 0,
    };
    let json = {};
    let unfoldPcks = [];
    let otherPcks = [];
    if (node._cfg.model.nodeType === "package") {
        unfoldPcks.push({
            "id": node._cfg.id,
            "instability": node._cfg.model.instability
        })
    }
    json["unfoldPcks"] = unfoldPcks;
    json["otherPcks"] = otherPcks;
    $.ajax({
        async: false,
        url: "/coupling/group/one_step_child_packages_no_layout",
        type: "POST",
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify(json),

        success: function (result) {
            if(result["code"] === 200){
                const edges = result["edges"];
                let IList = [];
                let DList = [];
                let CList = [];
                edges.forEach(edge => {
                    if (edge.I !== -1) {
                        IList.push(edge.I);
                    }
                    DList.push(edge.D);
                    CList.push(parseFloat(edge.C));
                })
                if (edges.length === 0) {
                    statistics.I = 0;
                    statistics.D = 0;
                    statistics.C = 0;
                    return statistics;
                }
                IList.sort(function(a,b){return a - b});
                DList.sort(function(a,b){return a - b});
                CList.sort(function(a,b){return a - b});
                const DP90 = DList[parseInt(DList.length * 0.9)].toFixed(3);
                const IP90 = IList[parseInt(IList.length * 0.9)].toFixed(3);
                const CP90 = CList[parseInt(CList.length * 0.9)].toFixed(3);
                statistics.I = parseFloat(IP90);
                statistics.D = parseFloat(DP90);
                statistics.C = parseFloat(CP90);
            }
        }
    });
    return statistics;
}
function unfoldPkg() {
    if(selected_packages.length === 0){
        layer.msg("当前未选中节点！");
    }else{
        let json = {};
        let unfoldPcks = [];
        let otherPcks = [];
        selected_packages.forEach(node => {
            if (node._cfg.model.nodeType === "package") {
                unfoldPcks.push({
                    "id": node._cfg.id,
                    "instability": node._cfg.model.instability
                })
            }
        })
        if (unfoldPcks.length === 0) {
            layer.msg("错误！已无法再展开！");
            return;
        }
        present_packages.forEach(node => {
            let flag = true;
            selected_packages.forEach(node2 => {
                if(node._cfg.id === node2._cfg.id){
                    flag = false;
                }
            })
            if(flag){
                otherPcks.push({
                    "id": node._cfg.id,
                    "instability": node._cfg.model.instability
                })
            }
        })

        json["unfoldPcks"] = unfoldPcks;
        json["otherPcks"] = otherPcks;
        showLoadingWindow("加载中...");
        $.ajax({
            url: "/coupling/group/one_step_child_packages",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(json),

            success: function (result) {
                if(result["code"] === 200){
                    data_stack.push(graph.save())
                    data = result;
                    const nodes = data["nodes"];
                    max_level = nodes[nodes.length-1].level;
                    loadGraph();
                }else if(result["code"] === -1){
                    layer.msg("错误！\n" + result["pck"]["directoryPath"] + "\n已无法再展开！");
                    closeLoadingWindow();
                }
            }
        });
    }
}
function instabilityAjax(){
    $.ajax({
        type : "GET",
        url : "/coupling/group/top_level_packages",
        success : function(result) {
            data = result;
            const nodes = data["nodes"];
            max_level = nodes[nodes.length-1].level;
            // let nodes_data = json_data["nodes"];
            // let edges_data = json_data["edges"];
            //
            // data["nodes"] = json_data["nodes"];
            // data["edges"] = json_data["edges"];

            loadGraph();
        }
    })
}

function levelLayout(){
    let nodelist = graph.getNodes();
    let list1 = [], list2 = [], list3 = [], list4 = [], list5 = [], allList = [];
    nodelist.forEach(node =>{
        node._cfg.model.gradation = node._cfg.model.instability;

        if(node._cfg.model.instability >= 0.8){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR1
                }
            });
            list1.push(node);
        }else if(node._cfg.model.instability >= 0.6){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR2
                }
            });
            list2.push(node);
        }else if(node._cfg.model.instability >= 0.4){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR3
                }
            });
            list3.push(node);
        }else if(node._cfg.model.instability >= 0.2){
            node.update({
                style:{
                    fill:INSTABILITY_COLOR4
                }
            });
            list4.push(node);
        }else{
            node.update({
                style:{
                    fill:INSTABILITY_COLOR5
                }
            });
            list5.push(node);
        }
    })
    allList.push(list1);
    allList.push(list2);
    allList.push(list3);
    allList.push(list4);
    allList.push(list5);

    let startIndex = 0;
    let minParentPckId = "0";
    for(let i = 0; i < allList.length; i++){
        if(allList[i].length !== 0){
            minParentPckId = allList[i][0]._cfg.model.parentPckId;
            allList[i].sort(function(a,b){return a._cfg.model.parentPckId - b._cfg.model.parentPckId});
            allList[i].forEach((node, index) =>{
                minParentPckId = Math.min(minParentPckId, node._cfg.model.parentPckId);
                node.updatePosition({
                    // x: (index - (list1.length / 2)) * 70,
                    x: index * 70,
                    y: 70 * (i + 1) - ((node._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
                });
            })
            startIndex = i + 1;
            break;
        }
    }

    if(startIndex < allList.length){
        for(let i = startIndex; i < allList.length; i++){
            let sortedList = [];
            let nullList = [];

            allList[i].forEach(node =>{
                let parentPosX = 0.0;
                let parentPosSum = 0.0;
                let neighbors = node.getNeighbors();

                neighbors.forEach(neighbor =>{
                    if(neighbor._cfg.model.instability > node._cfg.model.instability){
                        parentPosX += neighbor._cfg.model.x;
                        parentPosSum += 1;
                    }
                })

                if(parentPosSum === 0){
                    if(neighbors.length > 0){
                        node._cfg.model.barycenter = 0;
                        sortedList.push(node);
                    }else{
                        nullList.push(node);
                    }
                }else{
                    node._cfg.model.barycenter = parentPosX / parentPosSum;
                    sortedList.push(node);
                }
            })

            sortedList.sort(function(a,b){return a._cfg.model.barycenter - b._cfg.model.barycenter});


            sortedList.forEach((node2, index) =>{
                let offset = i % 2 === 0 ? -35 : 0;
                node2.updatePosition({
                    // x: (index - (sortedList.length / 2)) * 70,
                    x: -100 + offset + index * 70,
                    y: 70 * (i + 1) - ((node2._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
                });
            })

            nullList.forEach((nullNode, index) => {
                nullNode.updatePosition({
                    // x: (index - (sortedList.length / 2)) * 70,
                    x: (index - Math.floor(index / 15) * 15 - 3) * 60,
                    y: 70 * (i + 2 + Math.floor(index / 15)) - ((nullNode._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
                });
            })
        }
    }

    graph.refresh();
    graph.fitCenter();
    graph.fitView();
}
function levelLayout2(){

    let nodelist = graph.getNodes();
    let allList = [];
    let levelGaps = [];
    for (let i = 0; i <= max_level; i++) {
        let curr_level = [];
        nodelist.forEach(node => {
            if (node._cfg.model.level === i) {
                curr_level.push(node);
            }
        })
        allList.push(curr_level);
        levelGaps[i] = curr_level.length * 5;
    }
    for(let i = 0; i < allList.length; i++) {

        if(allList[i].length !== 0) {
            allList[i].sort(function(a,b){return parseInt(a._cfg.model.parentPckId)
                - parseInt(b._cfg.model.parentPckId)});
            allList[i].forEach((node, index) =>{
                node.updatePosition({
                    // x: (index - (list1.length / 2)) * 70,
                    x: index * 70,
                    y: (i+1) * 70
                });
            })
            const DELTA = 70 / allList[i].length;
            for (let j = 0; j < allList[i].length; j++) {
                for (let k = j+1; k < allList[i].length; k++) {
                    let node1 = allList[i][j];
                    let node2 = allList[i][k];
                    const edgeId1 = node1._cfg.id + '_' + node2._cfg.id;
                    const edgeId2 = node2._cfg.id + '_' + node1._cfg.id;
                    let edge1 = graph.findById(edgeId1);
                    let edge2 = graph.findById(edgeId2);
                    if (edge1 === undefined && edge2 === undefined) continue;
                    if (edge1 !== undefined && edge2 === undefined) {
                        node1.updatePosition({
                            // x: (index - (list1.length / 2)) * 70,
                            x: node1._cfg.model.x,
                            y: node1._cfg.model.y - DELTA
                        });
                    }
                    if (edge2 !== undefined && edge1 === undefined) {
                        node2.updatePosition({
                            // x: (index - (list1.length / 2)) * 70,
                            x: node2._cfg.model.x,
                            y: node2._cfg.model.y - DELTA
                        });
                    }
                    if (edge1 !== undefined && edge2 !== undefined) {
                        if (edge1._cfg.model['D'] > edge2._cfg.model['D']) {
                            node1.updatePosition({
                                // x: (index - (list1.length / 2)) * 70,
                                x: node1._cfg.model.x,
                                y: node1._cfg.model.y - DELTA
                            });
                        } else {
                            node2.updatePosition({
                                // x: (index - (list1.length / 2)) * 70,
                                x: node2._cfg.model.x,
                                y: node2._cfg.model.y - DELTA
                            });
                        }


                    }
                }
            }
        }
    }

    graph.refresh();
    graph.fitCenter();
    graph.fitView();
}
// function levelLayout2(){
//     let nodelist = graph.getNodes();
//     let list1 = [], list2 = [], list3 = [], list4 = [], list5 = [], allList = [];
//     const L1 = max_level * 0.25;
//     const L2 = max_level * 0.5;
//     const L3 = max_level * 0.75;
//     const L4 = max_level;
//     nodelist.forEach(node =>{
//         if (node._cfg.model.instability >= L4) {
//             node.update({
//                 style:{
//                     fill:INSTABILITY_COLOR5
//                 }
//             });
//             list5.push(node);
//         } else if (node._cfg.model.instability >= L3) {
//             node.update({
//                 style:{
//                     fill:INSTABILITY_COLOR4
//                 }
//             });
//             list4.push(node);
//         } else if (node._cfg.model.instability >= L2) {
//             node.update({
//                 style:{
//                     fill:INSTABILITY_COLOR3
//                 }
//             });
//             list3.push(node);
//         } else if (node._cfg.model.instability >= L1) {
//             node.update({
//                 style:{
//                     fill:INSTABILITY_COLOR2
//                 }
//             });
//             list2.push(node);
//         } else {
//             node.update({
//                 style:{
//                     fill:INSTABILITY_COLOR1
//                 }
//             });
//             list1.push(node);
//         }
//     })
//
//     allList.push(list1);
//     allList.push(list2);
//     allList.push(list3);
//     allList.push(list4);
//     allList.push(list5);
//
//     let startIndex = 0;
//     let minParentPckId = "0";
//     for(let i = 0; i < allList.length; i++){
//         if(allList[i].length !== 0){
//             minParentPckId = allList[i][0]._cfg.model.parentPckId;
//             allList[i].sort(function(a,b){return a._cfg.model.parentPckId - b._cfg.model.parentPckId});
//             allList[i].forEach((node, index) =>{
//                 minParentPckId = Math.min(minParentPckId, node._cfg.model.parentPckId);
//                 node.updatePosition({
//                     // x: (index - (list1.length / 2)) * 70,
//                     x: index * 70,
//                     y: 70 * (i + 1) - ((node._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
//                 });
//             })
//             startIndex = i + 1;
//             break;
//         }
//     }
//
//     if(startIndex < allList.length){
//         for(let i = startIndex; i < allList.length; i++){
//             let sortedList = [];
//             let nullList = [];
//
//             allList[i].forEach(node =>{
//                 let parentPosX = 0.0;
//                 let parentPosSum = 0.0;
//                 let neighbors = node.getNeighbors();
//
//                 neighbors.forEach(neighbor =>{
//                     if(neighbor._cfg.model.instability > node._cfg.model.instability){
//                         parentPosX += neighbor._cfg.model.x;
//                         parentPosSum += 1;
//                     }
//                 })
//
//                 if(parentPosSum === 0){
//                     if(neighbors.length > 0){
//                         node._cfg.model.barycenter = 0;
//                         sortedList.push(node);
//                     }else{
//                         nullList.push(node);
//                     }
//                 }else{
//                     node._cfg.model.barycenter = parentPosX / parentPosSum;
//                     sortedList.push(node);
//                 }
//             })
//
//             sortedList.sort(function(a,b){return a._cfg.model.barycenter - b._cfg.model.barycenter});
//
//
//             sortedList.forEach((node2, index) =>{
//                 let offset = i % 2 === 0 ? -35 : 0;
//                 node2.updatePosition({
//                     // x: (index - (sortedList.length / 2)) * 70,
//                     x: -100 + offset + index * 70,
//                     y: 70 * (i + 1) - ((node2._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
//                 });
//             })
//
//             nullList.forEach((nullNode, index) => {
//                 nullNode.updatePosition({
//                     // x: (index - (sortedList.length / 2)) * 70,
//                     x: (index - Math.floor(index / 15) * 15 - 3) * 60,
//                     y: 70 * (i + 2 + Math.floor(index / 15)) - ((nullNode._cfg.model.instability - (0.8 - i * 0.2)) / 0.2) * 70,
//                 });
//             })
//         }
//     }
//
//     graph.refresh();
//     graph.fitCenter();
//     graph.fitView();
// }

function isEdgeReversed(edge) {
    let result = false;
    const startLevel = edge._cfg.source._cfg.model.y;
    const endLevel = edge._cfg.target._cfg.model.y;
    if (startLevel > endLevel) {
        result = !edge._cfg.model.isExtendOrImplements;
    }
    return result;
}
function handleReverseEdgesAndExtends(){
    let edges = graph.getEdges();
    let nodes = graph.getNodes();

    edges.forEach(edge =>{
        let startLevel = 0;
        let endLevel = 0;

        nodes.forEach(node =>{
            if(node._cfg.id === edge._cfg.model.source){
                startLevel = node._cfg.model.y;
            }
            if(node._cfg.id === edge._cfg.model.target){
                endLevel = node._cfg.model.y;
            }
        })

        if(startLevel > endLevel){
            if(edge._cfg.model.isExtendOrImplements){
                let model = {
                    style:{
                        endArrow: {
                            path: G6.Arrow.triangleRect(10, 10, 10, 2, 4),
                            fill: COLOR_LINK_EXTENDS_AND_IMPLEMENTS,
                        }
                    }
                }
                edge.update(model);
            }else{
                edge.setState('reverse', true);
            }
        }else{
            if(edge._cfg.model.isTwoWayDependsOn){
                let tmpedge = graph.findById(edge._cfg.model.target + "_" + edge._cfg.model.source);
                if(tmpedge._cfg.model.isExtendOrImplements){
                    edge.setState('reverse', true);
                }
            }
        }
    })
}

function levelLayoutAdjust(){
    let reverseEdgesList = graph.findAllByState('edge', 'reverse');
    let reverseNodePairs = [];
    let reverseNodes = new Set();

    reverseEdgesList.forEach(edge => {
        let tmpList = [];
        tmpList[0] = edge.getSource();
        tmpList[1] = edge.getTarget();
        reverseNodePairs.push(tmpList);

        reverseNodes.add(edge.getSource());
        reverseNodes.add(edge.getTarget());
    })

    reverseNodePairs.sort(function(a,b){
        return Math.abs(a[0]._cfg.model.instability - a[1]._cfg.model.instability)
            - Math.abs(b[0]._cfg.model.instability - b[1]._cfg.model.instability)
    });

    for(let i = 0; i < reverseNodePairs.length; i++){
        let lowerNode = reverseNodePairs[i][0];
        let upperNode = reverseNodePairs[i][1];
        let edge = graph.findById(lowerNode._cfg.id + "_" + upperNode._cfg.id);
        let flag = false;

        lowerNode._cfg.edges.forEach(edge =>{
            if(edge._cfg.id === upperNode._cfg.id + "_" + lowerNode._cfg.id){
                flag = true;
            }
        })

        if(flag) continue;

        let lowerInEdges = lowerNode.getInEdges();
        let upperInEdges = upperNode.getInEdges();
        let lowerNodesLeastInstability = 2.0;
        let upperNodesLeastInstability = 2.0;

        lowerInEdges.sort(function(a,b){return a.getSource()._cfg.model.instability
            - b.getSource()._cfg.model.instability});

        lowerInEdges.forEach(edge => {
            if(!reverseNodes.has(edge.getSource())){
                lowerNodesLeastInstability  =
                    Math.min(lowerNodesLeastInstability, edge.getSource()._cfg.model.instability);
            }
        })

        // upperInEdges.sort(function(a,b){return a.getSource()._cfg.model.instability
        //     - b.getSource()._cfg.model.instability});
        //
        // upperInEdges.forEach(edge => {
        //     if(!reverseNodes.has(edge.getSource())){
        //         upperNodesLeastInstability  =
        //             Math.min(upperNodesLeastInstability, edge.getSource()._cfg.model.instability);
        //     }
        // })

        if(lowerNodesLeastInstability > upperNode._cfg.model.instability){
            lowerNode.updatePosition({
                y : upperNode._cfg.model.y + 3.5
            });
        }
        graph.refresh();
        edge.clearStates();
    }
}
layui.use('element', function(){
    var element = layui.element;

    //一些事件触发
    element.on('tab(btm_tab)', function(data) {
        if (data.index === 1) {
            if (!is_ntb_loaded) {
                var index = layer.load(1);
                loadNodeTable1();
                is_ntb_loaded = true;
                layer.close(index);
            }
        }
    });
});
function savePresentNodes(){
    present_packages.length = 0;
    graph.getNodes().forEach(node => {
        present_packages.push(node);
    })
}

function loadEdgeTable1() {
    edge_table1_data.splice(0, edge_table1_data.length);
    graph.getEdges().forEach(edge => {
        edge_table1_data.push({
            id: edge._cfg.id,
            obj1: edge._cfg.source._cfg.model.name,
            obj2: edge._cfg.target._cfg.model.name,
            D: parseFloat(edge._cfg.model.D),
            C: parseFloat(edge._cfg.model.C),
            reversed: isEdgeReversed(edge)
        });
    });
    layui.use('table', function(){
        const table = layui.table;
        table.render({
            elem: '#edge_table1'
            ,defaultToolbar: []
            ,toolbar: '#toolbarDemo'
            ,autoSort: false
            ,lineStyle: 'height:auto'
            ,cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
            ,cols: [[
                {type:'checkbox'}
                ,{field:'id', title: 'ID', sort: true}
                ,{field:'obj1', title: '对象1'}
                ,{field:'obj2', title: '对象2'}
                ,{field:'C', title: '依赖面耦合度(C)', sort: true}
                ,{field:'D', title: '依赖实例数(D)', sort: true}
                ,{field:'reversed',  title: '是否逆向', sort: true}
            ]]
            ,data: edge_table1_data
            ,page: {
                layout: ['limit', 'count', 'prev', 'page', 'next', 'skip'], //自定义分页布局
                limit: 10
            }
        });
        table.on('sort(edge1)', function (obj){
            if (obj.type === 'asc') {
                edge_table1_data.sort((a, b) => a[obj.field] - b[obj.field]);
            } else if (obj.type === 'desc') {
                edge_table1_data.sort((a, b) => b[obj.field] - a[obj.field]);
            } else {

            }
            table.reload('edge_table1', {
                data: edge_table1_data
            });
        });
        table.on('toolbar(edge1)', function (obj){
            let checkStatus = table.checkStatus(obj.config.id);
            if (obj.event === 'filterCheckEdges') {
                selected_packages = [];
                present_packages = [];
                const data = checkStatus.data;
                if (data.length !== 0) {
                    let showEdgesSet = new Set();
                    let showNodesSet = new Set();
                    data.forEach(item => {
                        showEdgesSet.add(item.id);
                        const sourceNode = graph.findById(item.id)._cfg.source;
                        const targetNode = graph.findById(item.id)._cfg.target;
                        selected_packages.push(sourceNode, targetNode);
                        showNodesSet.add(sourceNode._cfg.id);
                        showNodesSet.add(targetNode._cfg.id);
                    });
                    graph.getEdges().forEach(edge => {
                        if (!showEdgesSet.has(edge._cfg.id)) {
                            edge.hide();
                        } else {

                        }
                    });
                    graph.getNodes().forEach(node => {
                        if (!showNodesSet.has(node._cfg.id))
                            node.hide();
                    });
                    graph.paint();
                    layer.msg("操作完成");
                }
            }
            if (obj.event === 'reset') {
                graphShowAll();
            }
        });
    });
}
function loadNodeTable1() {
    node_table1_data.splice(0, node_table1_data.length);
    graph.getNodes().forEach(node => {
        if (node._cfg.model.nodeType === 'package') {
            let nof = 0;
            nof = node._cfg.model.NOF;
            let res = getNodeOneStepDetail(node);
            node_table1_data.push({
                id: node._cfg.id,
                name: node._cfg.model.name,
                NOF: nof,
                LOC: node._cfg.model.LOC,
                nodeType: node._cfg.model.nodeType,
                I: res["I"],
                D: res["D"],
                C: res["C"],
                unfoldable: node._cfg.model["unfoldable"],
            });
            // if (!res["unfoldable"]) {
            //     let model = node._cfg.model;
            //     model.style.stroke = 'rgb(0,128,0)';
            //     model.style.lineWidth = 2.0;
            //     graph.updateItem(node, model, false);
            // }
        } else {
            node_table1_data.push({
                id: node._cfg.id,
                name: node._cfg.model.name,
                NOF: NaN,
                LOC: node._cfg.model.LOC,
                nodeType: node._cfg.model.nodeType,
                I: NaN,
                D: NaN,
                C: NaN,
                unfoldable: false,
            });
        }
    });
    layui.use('table', function(){
        const table = layui.table;
        if (CHART_MODE === 'package') {
            table.render({
                elem: '#node_table1'
                ,defaultToolbar: []
                ,toolbar: '#toolbarDemo'
                ,autoSort: false
                ,lineStyle: 'height:auto'
                ,cellMinWidth: 50 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
                ,cols: PKG_NODE_TABLE_COLS
                ,data: node_table1_data
                ,page: {
                    layout: ['limit', 'count', 'prev', 'page', 'next', 'skip'], //自定义分页布局
                    limit: 10
                }
            });
        } else {
            table.render({
                elem: '#node_table1'
                ,defaultToolbar: []
                ,toolbar: '#toolbarDemo'
                ,autoSort: false
                ,lineStyle: 'height:auto'
                ,cellMinWidth: 50 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
                ,cols: FILE_NODE_TABLE_COLS
                ,data: node_table1_data
                ,page: {
                    layout: ['limit', 'count', 'prev', 'page', 'next', 'skip'], //自定义分页布局
                    limit: 10
                }
            });
        }

        table.on('sort(node1)', function (obj){
            if (obj.type === 'asc') {
                node_table1_data.sort((a, b) => a[obj.field] - b[obj.field]);
            } else if (obj.type === 'desc') {
                node_table1_data.sort((a, b) => b[obj.field] - a[obj.field]);
            } else {

            }
            table.reload('node_table1', {
                data: node_table1_data
            });
        });
        table.on('toolbar(node1)', function (obj){
            let checkStatus = table.checkStatus(obj.config.id);
            if (obj.event === 'filterCheckEdges') {
                const data = checkStatus.data;
                selected_packages = [];
                present_packages = [];
                if (data.length !== 0) {
                    let showNodesSet = new Set();
                    data.forEach(item => {
                        const node = graph.findById(item.id);
                        selected_packages.push(node);
                        const neighbors = graph.getNeighbors(node);
                        showNodesSet.add(item.id);
                        neighbors.forEach(n => {
                           showNodesSet.add(n._cfg.model.id);
                        });
                    });
                    // console.log("after filtering nodes:");
                    // console.log(selected_packages);
                    graph.getEdges().forEach(edge => {
                        if (!showNodesSet.has(edge._cfg.source._cfg.id)
                            || !showNodesSet.has(edge._cfg.target._cfg.id)) {
                            edge.hide();
                        }
                    });
                    graph.getNodes().forEach(node => {
                        if (!showNodesSet.has(node._cfg.id))
                            node.hide();
                    });
                    graph.paint();
                    layer.msg("操作完成");
                }
            }
            if (obj.event === 'reset') {
                graphShowAll();
            }
        });
    });
}
function graphShowAll() {
    graph.getEdges().forEach(edge => {
        edge.show();
    });
    graph.getNodes().forEach(node => {
        node.show();
    });
    selected_packages.splice(0, selected_packages.length);
    present_packages.splice(0, present_packages.length);
    layer.msg("操作完成");
}
function loadPanel(loadBtmTables){
    let html0 = "";
    let html1 = "";
    let html2 = "";
    let nodes = graph.getNodes();
    let edges = graph.getEdges();
    let NOF = 0;
    let LOC = 0;
    CList = [];
    Csum = 0.0;
    Cmax = 0.0;
    Cmin = 10000.0;
    IList = [];
    Isum = 0.0;
    Imax = 0.0;
    Imin = 10000.0;
    let node_set = new Set();
    nodes.forEach(node => {
        NOF += node._cfg.model.NOF;
        LOC += node._cfg.model.LOC;
        node_set.add(node._cfg.model.id);
    })
    const N = nodes.length;
    const gAbsComplexity = calcAbsGComplexity(2, "C");
    const gRComplexity = gAbsComplexity * 2 / (N * (N - 1));
    if (CHART_MODE === "package") {
        html0 += "<p>包数：" + nodes.length + "</p>";
        html0 += "<p>文件数：" + NOF + "</p>";
    } else if(CHART_MODE === "file") {
        html0 += "<p>文件数：" + nodes.length + "</p>";
    }
    html0 += "<p>图绝对复杂度：" + gAbsComplexity.toFixed(2) + "</p>";
    html0 += "<p>图相对复杂度：" + gRComplexity.toFixed(2) + "</p>";
    html0 += "<p>代码行数：" + LOC + "</p>";
    html0 += "<br />";
    let tmpMap = new Map();
    let tmpSet = new Set();
    if(edges.length > 0){
        edges.forEach(edge=>{
            if (!isNaN(edge._cfg.model.C)) {
                tmpMap.set(edge._cfg.model.id, edge._cfg.model.C);
            }
        })
        edges.forEach(edge => {
            // edge_table1_data.push({
            //     id: edge._cfg.id,
            //     obj1: edge._cfg.source._cfg.model.name,
            //     obj2: edge._cfg.target._cfg.model.name,
            //     I: parseFloat(edge._cfg.model.I),
            //     D: parseFloat(edge._cfg.model.D),
            //     C: parseFloat(edge._cfg.model.C),
            //     reversed: isEdgeReversed(edge)
            // });
            if (edge._cfg.model.I !== -1) {
                IList.push(edge._cfg.model.I);
                Isum += edge._cfg.model.I;
                Imax = Math.max(Imax, edge._cfg.model.I);
                Imin = Math.min(Imin, edge._cfg.model.I);
            }
            if (!isNaN(edge._cfg.model.C)) {
                const reverseId = edge._cfg.model.target + "_" + edge._cfg.model.source;
                if (!tmpMap.has(reverseId)) {
                    CList.push(edge._cfg.model.C);
                    Csum += edge._cfg.model.C;
                    Cmax = Math.max(Cmax, edge._cfg.model.C);
                    Cmin = Math.min(Cmin, edge._cfg.model.C);
                    tmpSet.add(edge._cfg.model.id);
                } else {
                    if (!tmpSet.has(edge._cfg.model.id)) {
                        let reverseC = tmpMap.get(reverseId);
                        let C = Math.sqrt(Math.pow(edge._cfg.model.C,2)+Math.pow(reverseC,2));
                        CList.push(C);
                        Csum += C;
                        Cmax = Math.max(Cmax, C);
                        Cmin = Math.min(Cmin, C);
                        tmpSet.add(edge._cfg.model.id);
                        tmpSet.add(reverseId);
                    }
                }
            }

        })
        IList.sort(function(a,b){return a - b});
        CList.sort(function(a,b){return a - b});
        C90 = CList[parseInt(CList.length * 0.9)];

        // html += "<p>耦合强度(I) 平均值：" + (Isum / edges.length).toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) max：" + Imax.toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) 90分位值：" + IList[parseInt(IList.length * 0.9)].toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) 85分位值：" + IList[parseInt(IList.length * 0.85)].toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) 80分位值：" + IList[parseInt(IList.length * 0.8)].toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) 75分位值(Q3)：" + IList[parseInt(IList.length * 0.75)].toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) 中位值(Q2)：" + IList[parseInt(IList.length * 0.5)].toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) 25分位值(Q1)：" + IList[parseInt(IList.length * 0.25)].toFixed(3) + "</p>";
        // html += "<p>耦合强度(I) min：" + Imin.toFixed(3) + "</p>";
        // html += "<br />";
        if (CList.length !== 0) {
            html1 += "<p>依赖面耦合度(C) 平均值：" + (Csum / edges.length).toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) max：" + Cmax.toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) 90分位值：" + CList[parseInt(CList.length * 0.9)].toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) 85分位值：" + CList[parseInt(CList.length * 0.85)].toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) 80分位值：" + CList[parseInt(CList.length * 0.8)].toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) 75分位值(Q3)：" + CList[parseInt(CList.length * 0.75)].toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) 中位值(Q2)：" + CList[parseInt(CList.length * 0.5)].toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) 25分位值(Q1)：" + CList[parseInt(CList.length * 0.25)].toFixed(3) + "</p>";
            html1 += "<p>依赖面耦合度(C) min：" + Cmin.toFixed(3) + "</p>";
            html1 += "<br />";
        }
        let reverseNum = graph.findAllByState("edge", "reverse").length;
        html2 += "<p>逆向依赖数：" + reverseNum + "</p>";
        html2 += "<p>总依赖数：" + edges.length + "</p>";
        html2 += "<p>逆向依赖数 / 总依赖数：" + (reverseNum / edges.length).toFixed(3) + "</p>";
    }
    $("#data_panel0").html(html0);
    $("#data_panel1").html(html1);
    $("#data_panel2").html(html2);
    if (loadBtmTables) {
        loadEdgeTable1();
        // loadNodeTable1();
    }
}

function switchDataPanel() {
    let html = "";
    const len = graph.getEdges().length;
    if (current_panel === 'D') {
        if (IList.length !== 0) {
            html += "<p>耦合强度(I) 平均值：" + (Isum / len).toFixed(3) + "</p>";
            html += "<p>耦合强度(I) max：" + Imax.toFixed(3) + "</p>";
            html += "<p>耦合强度(I) 90分位值：" + IList[parseInt(IList.length * 0.9)].toFixed(3) + "</p>";
            html += "<p>耦合强度(I) 85分位值：" + IList[parseInt(IList.length * 0.85)].toFixed(3) + "</p>";
            html += "<p>耦合强度(I) 80分位值：" + IList[parseInt(IList.length * 0.8)].toFixed(3) + "</p>";
            html += "<p>耦合强度(I) 75分位值(Q3)：" + IList[parseInt(IList.length * 0.75)].toFixed(3) + "</p>";
            html += "<p>耦合强度(I) 中位值(Q2)：" + IList[parseInt(IList.length * 0.5)].toFixed(3) + "</p>";
            html += "<p>耦合强度(I) 25分位值(Q1)：" + IList[parseInt(IList.length * 0.25)].toFixed(3) + "</p>";
            html += "<p>耦合强度(I) min：" + Imin.toFixed(3) + "</p>";
            html += "<br />";
            current_panel = 'I';
        }
    } else {
        if (CList.length !== 0) {
            html += "<p>依赖面耦合度(C) 平均值：" + (Csum / len).toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) max：" + Cmax.toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) 90分位值：" + CList[parseInt(CList.length * 0.9)].toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) 85分位值：" + CList[parseInt(CList.length * 0.85)].toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) 80分位值：" + CList[parseInt(CList.length * 0.8)].toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) 75分位值(Q3)：" + CList[parseInt(CList.length * 0.75)].toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) 中位值(Q2)：" + CList[parseInt(CList.length * 0.5)].toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) 25分位值(Q1)：" + CList[parseInt(CList.length * 0.25)].toFixed(3) + "</p>";
            html += "<p>依赖面耦合度(C) min：" + Cmin.toFixed(3) + "</p>";
            html += "<br />";
            current_panel = 'D';
        }
    }
    $("#data_panel1").html(html);
}
function handleEdgesWidth(){
    graph.getEdges().forEach(edge => {
        if(edge._cfg.model.C >= C90){
            edge.update(HIGH_INTENSITY_EDGE_MODEL);
        }else {
            edge.update(LOW_INTENSITY_EDGE_MODEL);
        }
    })
}
function handleNodeStroke() {
    graph.getNodes().forEach(node => {
        let model = node._cfg.model;
        if (model['unfoldable'] === false) {
            model.style.stroke = 'rgb(0,128,0)';
            model.style.lineWidth = 2.0;
            graph.updateItem(node, model, false);
        }

    });

}
function loadGraph(){
    is_ntb_loaded = false;
    if (show_panel_btm) {
        hideBottomPanel();
    }
    graph.data(data);
    graph.render();
    // levelLayout();
    if (max_level === 0)
        levelLayout();
    else
        levelLayout2();
    handleNodeStroke();
    handleReverseEdgesAndExtends();
    // levelLayoutAdjust();
    savePresentNodes();
    loadPanel(true);
    handleEdgesWidth();
    closeLoadingWindow();
}

//加载弹窗
function showLoadingWindow(tip){
    let html = "<div style=\"position:fixed;height:100%;width:100%;z-index:10000;background-color: #5a6268;opacity: 0.5\">" +
        "<div class='loading_window' id='Id_loading_window' " +
        "style=\"left: " + (width - 215) / 2 + "px; top:" + (height - 61) / 2 + "px;\">" + tip + "</div>" +
        "</div>";
    loading_div.html(html);
}

//关闭加载弹窗
function closeLoadingWindow(){
    loading_div.html("");
}

graph.on('edge:click', (e) => {
    // 选择了一个edge，在左侧panel展示edge信息
    let selectedEdge = e.item._cfg.model;
    const sourceNodeType = e.item._cfg.sourceNode._cfg.model.nodeType;
    let outDiv = document.getElementById("detail_panel");
    outDiv.className = "layui-colla-content layui-show";
    if (sourceNodeType === 'package') {
        outDiv.innerHTML = `
                      <h4><b>tag</b>: ${selectedEdge.source}_${selectedEdge.target}</h4>
                      <ul>
                        <li><b>依赖面耦合度(C)</b>: ${selectedEdge.C}</li>
                      </ul>
                      <ul>
                        <li><b>依赖实例数(D(logD))</b>: ${selectedEdge.D}(${selectedEdge.logD})</li>
                      </ul>`;
    } else {
        outDiv.innerHTML = `
                      <h4><b>tag</b>: ${selectedEdge.source}_${selectedEdge.target}</h4>
                      <ul>
                        <li><b>dependsOnTypes</b>: ${selectedEdge.dependsOnTypes}</li>
                      </ul>
                      <ul>
                        <li><b>依赖面耦合度(C)</b>: ${selectedEdge.C}</li>
                      </ul>                        
                      <ul>
                        <li><b>依赖实例数(D)</b>: ${selectedEdge.D}</li>
                      </ul>`;
    }

})

graph.on('node:dblclick', (e) => {
    let selectedNode = e.item;
    if (selectedNode._cfg.model.nodeType === "file") {
        if (navigator.clipboard) {
            // clipboard api 复制
            navigator.clipboard.writeText(selectedNode._cfg.model.path).then(() => {
                layer.msg("路径已复制到剪切板！");
            });
        }

        return;
    }
    selected_packages = [];
    selected_packages.push(selectedNode);
    unfoldPkg();
})
graph.on('nodeselectchange', (e) => {
    // 选择了一个node，在左侧panel展示node信息
    if (e.selectedItems.nodes.length === 1) {
        if (e.select) {
            let outDiv = document.getElementById("detail_panel");
            outDiv.className = "layui-colla-content layui-show";
            // let selectedNode = e.target._cfg.model;
            let selectedNode = e.selectedItems.nodes[0]._cfg.model;
            if (selectedNode.nodeType === "file") {
                outDiv.innerHTML = `
              <h4><b>id</b>: ${selectedNode.id}</h4>
              <ul>
                <li><b>name</b>: ${selectedNode.name}</li>
              </ul>
              <ul>
                <li><b>path</b>: ${selectedNode.path}</li>
              </ul>
              <ul>
                <li><b>instability</b>: ${selectedNode.instability}</li>
              </ul>`;
            } else if (selectedNode.nodeType === "package") {
                outDiv.innerHTML = `
              <h4><b>id</b>: ${selectedNode.id}</h4>
              <ul>
                <li><b>name</b>: ${selectedNode.name}</li>
              </ul>
              <ul>
                <li><b>path</b>: ${selectedNode.path}</li>
              </ul>
              <ul>
                <li><b>files num</b>: ${selectedNode.NOF}</li>
              </ul>
              <ul>
                <li><b>instability</b>: ${selectedNode.instability}</li>
              </ul>`;
            }
        }
    }
    selected_packages.length = 0;
    e.selectedItems.nodes.forEach(node =>{
        selected_packages.push(node);
    })
});

graph.on('node:dragend', evt => {
    let edges = evt.item.getEdges();

    edges.forEach(edge =>{
        edge.clearStates('reverse');
        if(edge._cfg.source._cfg.model.y > edge._cfg.target._cfg.model.y){
            if(!edge._cfg.model.isExtendOrImplements)
                edge.setState('reverse', true);
        }else{
            if(edge._cfg.model.isTwoWayDependsOn){
                let edgeTmp = graph.findById(edge._cfg.model.target + "_" + edge._cfg.model.source);
                if(edgeTmp._cfg.model.isExtendOrImplements){
                    edge.setState('reverse', true);
                }
            }
        }
    })
    // loadPanel(false);
})

// graph.on('nodeselectchange', (e) => {
//     // selected_packages = e.selectedItems.nodes;
//     selected_packages.length = 0;
//     e.selectedItems.nodes.forEach(node =>{
//         selected_packages.push(node);
//     })
// });

if (typeof window !== 'undefined')
    window.onresize = () => {
        if (!graph || graph.get('destroyed')) return;
        if (!container || !container.scrollWidth || !container.scrollHeight) return;
        graph.changeSize(container.scrollWidth, container.scrollHeight);
    };

