import {getBackend, putBackend} from "./fetch.js"

async function newGame(){
    const response = await putBackend("play").then()
    const json = await response.json();
    console.log("response: ", json)
}

newGame()