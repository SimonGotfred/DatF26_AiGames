
import {newGame, getPossibleMoves, makeMove, getAiMove} from "./ChessFrontend.js"

let table = document.createElement("table")
document.body.appendChild(table)
table.className = "Table"

let board = []

/*const startState = [
    ["♖","♘","♗","♕","♔","♗","♘","♖"],
    ["♙","♙","♙","♙","♙","♙","♙","♙"],
    ["","","","","","","",""],
    ["","","","","","","",""],
    ["","","","","","","",""],
    ["","","","","","","",""],
    ["♟","♟","♟","♟","♟","♟","♟","♟",],
    ["♜","♞","♝","♛","♚","♝","♞","♜"],]*/

//let dd = String.fromCharCode("♕".charCodeAt(0) + 6)
let startWhite = false;
let isWhite = false;

let blackChars = ["♖","♘","♗","♕","♔","♙"]

let chosenCell = null;

let isWaitingForAi = false;

const alphabet = ["a","b","c","d","e","f","g","h"]

async function MakeBoard(){

    const startState = await newGame()
    console.log("startstate: " + startState)
    startState.map((_row, rowIndex) => {
        const row = document.createElement("tr")
        table.appendChild(row)

        board.push([])
        _row.map((_cell, cellIndex) => {
            const cell = document.createElement("th")
            row.appendChild(cell)

            let cornorTextList = []
            if(cellIndex === 0){
                const cornorText = document.createElement("p")
                cell.appendChild(cornorText)
                cornorText.innerText = 8-rowIndex.toString()
                cornorText.className = "CornorText TopLeft"
                cornorTextList.push(cornorText)
            }
            if(rowIndex === 7){
                const cornorText = document.createElement("p")
                cell.appendChild(cornorText)
                cornorText.innerText = alphabet[cellIndex]
                cornorText.className = "CornorText BottomRight"
                cornorTextList.push(cornorText)
            }


            const piece = document.createElement("img")
            cell.appendChild(piece)
            cell.piece = piece;

            piece.className = "Piece"
            piece.chessPiece = _cell ? _cell : ""
            SetBlackOrWhite(piece)

            cell.addEventListener("click",() => pressCell(cell))

            //console.log(board.length -1 + cell.piece.innerText)


            //space.addEventListener("click", () => clickSpace(space))

            if(startWhite && isWhite || !startWhite && !isWhite){
                cell.className += " WhiteSpace"
                cornorTextList.map((cornorText) => {
                    cornorText.className += " WhiteSpace"
                })
            }
            else{
                cell.className += " BlackSpace"
                cornorTextList.map((cornorText) => {
                    cornorText.className += " BlackSpace"
                })
            }

            isWhite = !isWhite
            board[board.length -1].push(cell)
        })
        startWhite = !startWhite
    })

}

function ChangeBoard(newBoard){


    let whiteKingExists = false;
    let blackKingExists = false;
    for(let i = 0; i < newBoard.length; i++){
        for(let k = 0; k < newBoard[i].length; k++){
            //board[i][k].piece.innerText = state[i][k]
                board[i][k].piece.chessPiece = newBoard[i][k]

            SetBlackOrWhite(board[i][k].piece)

            if(newBoard[i][k] === "♔")
                blackKingExists = true
            else if(newBoard[i][k] === "♚")
                whiteKingExists = true
        }
    }
    if(!blackKingExists){
        alert("White has won")
    }
    else if(!whiteKingExists){
        alert("Black has won")
    }

}

const piecesList = ["♖","♘","♗","♕","♔","♙","♜","♞","♝","♛","♚","♟"]
const pieceImagesList = ["Black_Tower","Black_Knight","Black_Bishop","Black_Queen","Black_King","Black_Pawn",
"White_Tower","White_Knight","White_Bishop","White_Queen","White_King","White_Pawn"]
function SetBlackOrWhite(piece){
   /* let isBlackPiece = false;
    blackChars.map((char) => {
        if(piece.chessPiece === char){
            isBlackPiece = true;
            //piece.innerText = String.fromCharCode(char.charCodeAt(0) + 6)
            //piece.src = "../images/Black_Pawn.png"
            if(!piece.className.includes("BlackPiece"))
                piece.className += " BlackPiece"
        }
    })*/
    /*if(!isBlackPiece){
        //piece.src = "../images/Black_Pawn.png"
        //piece.innerText = piece.chessPiece;
        piece.className = piece.className.replace("BlackPiece", "")
    }*/
    if(piece.chessPiece === "ㅤ" || piece.chessPiece === ""){
        piece.src = "../images/Empty.png"
    }
    else {
        for (let i = 0; i < piecesList.length; i++){
            if(piecesList[i] === piece.chessPiece){
                piece.src = `../images/${pieceImagesList[i]}.png`
            }
        }
    }
}

function HighlightSquare(cell){
    cell.className += " Highlight"
}
function RemoveHiglight(cell){
    cell.className =  cell.className.replace("Highlight", "")
    chosenCell = null;
    if(LegalMoves !== null) {
        LegalMoves.map((legalMove) => {
            board[legalMove[0]][legalMove[1]].className = board[legalMove[0]][legalMove[1]].className.replace("Dot", "")
        })
    }
}

function cellToCharArray(cell){
    for (let i = 0; i < board.length; i++){
        for (let k = 0; k < board[i].length; k++){
            if(board[i][k] === cell){
                return [i, k]
            }
        }
    }
}

let LegalMoves = null
const promotablePieces = ["♛","♝","♞","♜"]

async function pressCell(cell){

    if(chosenCell === null){
        chosenCell = cell
        HighlightSquare(cell)
        //do backend call to check where you can put piece, and show it with dots
        /*LegalMoves = [
            [0,0], [1,1]
        ]*/
        LegalMoves = await getPossibleMoves(cellToCharArray(cell)) ?? null
        console.log(LegalMoves)
        if(LegalMoves !== null){
            LegalMoves.map((legalMove) => {
                board[legalMove[0]][legalMove[1]].className += " Dot"
            })
        }


    }
    else if(cell === chosenCell){
        RemoveHiglight(chosenCell)

    }
    else {
        //check if you can take piece to there, else do nothing or unhighlight

        if(LegalMoves !== null) {
            LegalMoves.map(async (legalMove) => {
                if (board[legalMove[0]][legalMove[1]] === cell) {
                    //cell.piece.chessPiece = chosenCell.piece.chessPiece
                    //SetBlackOrWhite(cell.piece)
                    //chosenCell.piece.chessPiece = ""
                    //SetBlackOrWhite(chosenCell.piece)

                    console.log("row " + legalMove[0] + "piece " + cell.piece.chessPiece)
                    if ((legalMove[0] === 0 || legalMove[0] === 7) && (cell.piece.chessPiece === "♙" || cell.piece.chessPiece === "♟")) {
                        //alert("you can now promote")

                        //make them choose piece
                        const choosePiecePopUp = document.createElement("div")

                        choosePiecePopUp.className = "popUp"
                        document.body.appendChild(choosePiecePopUp)

                        const choosePiecePopUpContent = document.createElement("div")
                        choosePiecePopUpContent.className = "popUp-content"
                        choosePiecePopUp.appendChild(choosePiecePopUpContent)


                        for (let i = 0; i < promotablePieces.length; i++) {
                            const choosablePiece = document.createElement("div")
                            choosePiecePopUpContent.appendChild(choosablePiece)
                            choosablePiece.innerText = promotablePieces[i]
                            choosablePiece.className = "popUpPiece"
                            choosablePiece.addEventListener("click", () => choosePromotionPiece(cell, promotablePieces[i], choosePiecePopUp))
                        }
                    } else {
                        const response = await makeMove(cellToCharArray(chosenCell), cellToCharArray(cell))
                        ChangeBoard(response)
                        const PlayAi = document.getElementById("PlayAi")
                        if(PlayAi.checked)
                            waitForAiMove()
                    }


                }
            })
        }
        RemoveHiglight(chosenCell)
    }

}

function choosePromotionPiece(cell, pieceType, popUp){

    if(cell.piece.className.includes("BlackPiece")){
        pieceType = String.fromCharCode(pieceType.charCodeAt(0) - 6)
    }
    cell.piece.chessPiece = pieceType
    SetBlackOrWhite(cell.piece)

    popUp.remove()
    console.log("chose to promote to " + pieceType)
    //replace it with the piece and send with makeMove
        //makeMove(cellToCharArray(chosenCell), cellToCharArray(cell))//.then(waitForAiMove)
}

const delay = ms => new Promise(res => setTimeout(res, ms));
const waitForAiMove = async () => {
    console.log("started waiting")

    const waitForAiPopUp = document.createElement("div")
    waitForAiPopUp.className = "popUp"
    document.body.appendChild(waitForAiPopUp)

    const loadingBar = document.createElement("img")
    loadingBar.src = "../images/LoadingBar.gif"
    loadingBar.className = "LoadingBar"
    waitForAiPopUp.appendChild(loadingBar)


    const AiTime = document.getElementById("AiTime")
    await delay(AiTime.value * 1000);
    const response =  await getAiMove()
    /*const newBoard = []
    response.map((row) => {
        newBoard.push(row.split(''))
    })*/
    ChangeBoard(response)
    waitForAiPopUp.remove()
};



/*const testState = [
    ["♖","♘","♗","♕","♔","♗","♘","♖"],
    ["♙","♙","♙","♙","","♙","♙","♙"],
    ["","","","","","","",""],
    ["","","","","♙","","",""],
    ["","","","♟","","","",""],
    ["","","","","","","",""],
    ["♟","♟","♟","","♟","♟","♟","♟",],
    ["♜","♞","♝","♚","♛","♝","♞","♜"],]
ChangeBoard(testState)*/
MakeBoard().then(() => {
    const RestartButton = document.getElementById("RestartButton")
    RestartButton.addEventListener("click", async () => {
        const response = await newGame()
        ChangeBoard(response)
    })
})

/*HighlightSquare(3,4)
HighlightSquare(4,6)*/