const NODE_SIZE = 20;
const PADDING = 0;
const H_GAP = NODE_SIZE * 2;
const V_GAP = NODE_SIZE * 3;
let allEdges = [];
let rowOfBlocks = []
function calcAllNodesPos(response) {
    let allNodes = [];
    rowOfBlocks = response.blocks;
    allEdges = response["edges"];
    const rows = rowOfBlocks.length;
    for (let i = 0; i < rows; i++) {
        for (let j = 0; j < rowOfBlocks[i].length; j++) {
            const block = rowOfBlocks[i][j];
            rowOfBlocks[i][j]["X"] = block.width * NODE_SIZE + (block.width - 1) * (H_GAP + NODE_SIZE/2);
            rowOfBlocks[i][j]["Y"] = block.height * NODE_SIZE + (block.height - 1) * (V_GAP + NODE_SIZE/2);
        }
        // for (let [i, block] of blocks.entries()) {
        //     block["X"] = block.width * NODE_SIZE + (block.width - 1) * H_GAP;
        //     block["Y"] = block.height * NODE_SIZE + (block.height - 1) * block.width * V_GAP;
        //     console.log(block);
        // }
    }
    rowOfBlocks[0][0]["startY"] = PADDING;
    rowOfBlocks[0][0]["startX"] = PADDING;
    for (let i = 1; i < rowOfBlocks[0].length; i++) {
        rowOfBlocks[0][i]["startY"] = PADDING;
        rowOfBlocks[0][i]["startX"] = rowOfBlocks[0][i-1]["startX"] +  rowOfBlocks[0][i-1]["X"] + H_GAP ;
    }
    for (let i = 1; i < rows; i++) {
        rowOfBlocks[i][0]["startX"] = PADDING;
        rowOfBlocks[i][0]["startY"] = rowOfBlocks[i-1][0]["startY"] + rowOfBlocks[i-1][0]["Y"] +  V_GAP* 0.6;
    }
    for (let i = 1; i < rows; i++) {
        for (let j = 1; j < rowOfBlocks[i].length; j++) {
            rowOfBlocks[i][j]["startX"] = rowOfBlocks[i][j-1]["startX"] + rowOfBlocks[i][j-1]["X"] + H_GAP;
            rowOfBlocks[i][j]["startY"] = rowOfBlocks[i-1][0]["startY"] + rowOfBlocks[i-1][0]["Y"] + V_GAP * 0.6;
        }
    }

    // add rectangle
    // for (let i = 0; i < rowOfBlocks.length; i++) {
    //     for (let j = 0; j < rowOfBlocks[i].length; j++) {
    //         debugger
    //         if (rowOfBlocks[i][j].packages.length > 1) {
    //             const width = rowOfBlocks[i][j]["X"];
    //             const height = rowOfBlocks[i][j]["Y"];
    //             const x = rowOfBlocks[i][j]["startX"] + rowOfBlocks[i][j]["X"] / 2;
    //             const y = rowOfBlocks[i][j]["startY"] + rowOfBlocks[i][j]["Y"] / 2;
    //             let rect = {};
    //             rect.x = x;
    //             rect.y = y;
    //             rect.size = [width, height];
    //             rect.type = 'rect';
    //             rect.style = {
    //                 'fillOpacity': 1,
    //                 'fill': '#eee',
    //                 'stroke': '#00ff00'
    //             };
    //             allNodes.push(rect);
    //
    //         }
    //     }
    // }
    drawRects();
    for (let i = 0; i < rows; i++) {
        for (let j = 0; j < rowOfBlocks[i].length; j++) {
            allNodes = allNodes.concat(calcBlockNodesPos(rowOfBlocks[i][j]));
        }
    }
    return allNodes;
}

function drawRects() {
    // add rectangle node blocks
    let c = document.getElementById("coupling_chart").firstChild;
    let ctx = c.getContext("2d");
    ctx.lineWidth = "6";
    ctx.strokeStyle = "green";
    for (let i = 0; i < rowOfBlocks.length; i++) {
        for (let j = 0; j < rowOfBlocks[i].length; j++) {
            if (rowOfBlocks[i][j].packages.length > 1) {
                const width = rowOfBlocks[i][j]["X"];
                const height = rowOfBlocks[i][j]["Y"];
                const x = rowOfBlocks[i][j]["startX"];
                const y = rowOfBlocks[i][j]["startY"];
                const point = graph.getPointByCanvas(x, y);
                console.log(point);
                ctx.beginPath();
                ctx.rect(point.x, point.y, width, height);
                ctx.stroke();
            }
        }
    }
}

function calcBlockNodesPos2(block) {
    // let packages = block.packages;
    let blockNodes = [];
    for (let i = 0; i < block.packages.length; i++) {
        for (let j = 0; j < block.packages[i].length; j++) {
            block.packages[i][j]["x"] = block["startX"] + j * H_GAP;
            block.packages[i][j]["y"] = block["startY"] + i * V_GAP;
            blockNodes.push(block.packages[i][j]);
        }
    }
    // console.log(block.packages);
    return blockNodes;
}
function calcBlockNodesPos(block) {
    // let packages = block.packages;
    let blockNodes = [];
    for (let i = 0; i < block.packages.length; i++) {
        for (let j = 0; j < block.packages[i].length; j++) {
            block.packages[i][j]["x"] = block["startX"] + j * H_GAP;
            block.packages[i][j]["y"] = block["startY"] + i * V_GAP;
            // blockNodes.push(block.packages[i][j]);
        }
    }
    for(let i = 0; i < block.packages.length; i++) {
        if(block.packages[i].length !== 0) {
            const DELTA = V_GAP / block.packages[i].length;
            for (let j = 0; j < block.packages[i].length; j++) {
                for (let k = j+1; k < block.packages[i].length; k++) {
                    let node1 = block.packages[i][j];
                    let node2 = block.packages[i][k];
                    const edgeId1 = node1.id + '_' + node2.id;
                    const edgeId2 = node2.id + '_' + node1.id;
                    let edge1 = allEdges.find(e => e["id"] === edgeId1);
                    let edge2 = allEdges.find(e => e["id"] === edgeId2);
                    if (edge1 === undefined && edge2 === undefined) continue;
                    if (edge1 !== undefined && edge2 === undefined) {
                        node1["y"] -= DELTA;
                    }
                    if (edge2 !== undefined && edge1 === undefined) {
                        node2["y"] -= DELTA;
                    }
                    if (edge1 !== undefined && edge2 !== undefined) {
                        if (edge1["D"] > edge2["D"]) {
                            node1["y"] -= DELTA;
                            // let tmp = node1["level"];
                            // node1["level"] = node2["level"];
                            // node2["level"] = tmp;
                        } else {
                            node2["y"] -= DELTA;
                            // let tmp = node1["level"];
                            // node1["level"] = node2["level"];
                            // node2["level"] = tmp;
                        }
                    }
                    block.packages[i][j]["y"] = node1["y"];
                    block.packages[i][k]["y"] = node2["y"];
                }
            }
        }
    }
    for (let i = 0; i < block.packages.length; i++) {
        for (let j = 0; j < block.packages[i].length; j++) {
            blockNodes.push(block.packages[i][j]);
        }
    }
    return blockNodes;
}
function blockInsideLayout(block) {

    for(let i = 0; i < block.packages.length; i++) {
        if(block.packages[i].length !== 0) {
            const DELTA = V_GAP / block.packages[i].length;
            for (let j = 0; j < block.packages[i].length; j++) {
                for (let k = j+1; k < block.packages[i].length; k++) {
                    let node1 = block.packages[i][j];
                    let node2 = block.packages[i][k];
                    const edgeId1 = node1.id + '_' + node2.id;
                    const edgeId2 = node2.id + '_' + node1.id;
                    let edge1 = graph.findById(edgeId1);
                    let edge2 = graph.findById(edgeId2);
                    if (edge1 === undefined && edge2 === undefined) continue;
                    if (edge1 !== undefined && edge2 === undefined) {
                        node1.y -= DELTA;
                    }
                    if (edge2 !== undefined && edge1 === undefined) {
                        node2.y -= DELTA;
                    }
                    if (edge1 !== undefined && edge2 !== undefined) {
                        if (edge1._cfg.model['D'] > edge2._cfg.model['D']) {
                            node1.y -= DELTA;
                        } else {
                            node2.y -= DELTA;
                        }
                    }
                    block.packages[i][j] = node1;
                    block.packages[i][k] = node2;
                }
            }
        }
    }
    return block;
}