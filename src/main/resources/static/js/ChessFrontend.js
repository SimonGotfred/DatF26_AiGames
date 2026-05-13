import {getBackend, putBackend, postBackend, patchBackend} from "./fetch.js"

export async function newGame(){
    const response = await putBackend("play")
    const json = await response.json()
    console.log("new game: ", json)
    return json;
}
export async function getPossibleMoves(position){
    const response = await getBackend(`play?position=${position[1]},${position[0]}`)
    const json = await response.json()
    let fixedArray = []
    json.map((array) => {

        fixedArray.push([array[1], array[0]])
    })
    return fixedArray
}
export async function makeMove(fromPosition, toPosition, promote){
    const response = await postBackend("play?" + (promote ? `promote=${promote}&` : "") + `from=${fromPosition[1]},${fromPosition[0]}&to=${toPosition[1]},${toPosition[0]}`)
    //const json = await response.json()

    const json = await response.json()
    console.log("response: ", json)
    return json
}
export async function getAiMove(){
    const response = await patchBackend("play")
    const json = await response.json()
    console.log("ai move response: ", json)
    return json
}

