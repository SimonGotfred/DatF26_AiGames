
import {newGame, getPossibleMoves, makeMove, getAiMove} from "./ChessFrontend.js"

let table = document.createElement("table")
document.body.appendChild(table)
table.className = "Table"

let board = []

const startState = [
    ["♖","♘","♗","♕","♔","♗","♘","♖"],
    ["♙","♙","♙","♙","♙","♙","♙","♙"],
    ["","","","","","","",""],
    ["","","","","","","",""],
    ["","","","","","","",""],
    ["","","","","","","",""],
    ["♟","♟","♟","♟","♟","♟","♟","♟",],
    ["♜","♞","♝","♛","♚","♝","♞","♜"],]

//let dd = String.fromCharCode("♕".charCodeAt(0) + 6)
let startWhite = false;
let isWhite = false;

let blackChars = ["♖","♘","♗","♕","♔","♙"]

let chosenCell = null;

let isWaitingForAi = false;

function MakeBoard(startState){
    startState.map((_row) => {
        const row = document.createElement("tr")
        table.appendChild(row)

        board.push([])
        _row.map((_cell) => {
            const cell = document.createElement("th")
            row.appendChild(cell)

            const piece = document.createElement("div")
            cell.appendChild(piece)
            piece.innerText = ""
            cell.piece = piece;

            piece.className = "Piece"
            piece.chessPiece = _cell ? _cell : ""
            SetBlackOrWhite(piece)

            cell.addEventListener("click",() => pressCell(cell))

            //console.log(board.length -1 + cell.piece.innerText)


            //space.addEventListener("click", () => clickSpace(space))

            if(startWhite && isWhite || !startWhite && !isWhite)
                cell.className += " WhiteSpace"
            else{
                cell.className += " BlackSpace"
            }

            isWhite = !isWhite
            board[board.length -1].push(cell)
        })
        startWhite = !startWhite
    })

}

function ChangeBoard(state){
    for(let i = 0; i < state.length; i++){
        for(let k = 0; k < state[i].length; k++){
            //board[i][k].piece.innerText = state[i][k]
            if(state[i][k] !== undefined && board[i][k].piece !== undefined)
                board[i][k].piece.chessPiece = state[i][k]

            SetBlackOrWhite(board[i][k].piece)
        }
    }
}
function SetBlackOrWhite(piece){
    let isBlackPiece = false;
    blackChars.map((char) => {
        if(piece.chessPiece === char){
            isBlackPiece = true;
            piece.innerText = String.fromCharCode(char.charCodeAt(0) + 6)
            if(!piece.className.includes("BlackPiece"))
                piece.className += " BlackPiece"
        }
    })
    if(!isBlackPiece){
        piece.innerText = piece.chessPiece;
        piece.className = piece.className.replace("BlackPiece", "")
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
            LegalMoves.map((legalMove) => {
                if (board[legalMove[0]][legalMove[1]] === cell) {
                    cell.piece.chessPiece = chosenCell.piece.chessPiece
                    SetBlackOrWhite(cell.piece)
                    chosenCell.piece.chessPiece = ""
                    SetBlackOrWhite(chosenCell.piece)

                    console.log("row " + legalMove[0] + "piece " + cell.piece.chessPiece)
                    if((legalMove[0] === 0 || legalMove[0] === 7) && (cell.piece.chessPiece === "♙" || cell.piece.chessPiece === "♟")){
                        //alert("you can now promote")

                        //make them choose piece
                        const choosePiecePopUp = document.createElement("div")

                        choosePiecePopUp.className = "popUp"
                        document.body.appendChild(choosePiecePopUp)

                        const choosePiecePopUpContent = document.createElement("div")
                        choosePiecePopUpContent.className = "popUp-content"
                        choosePiecePopUp.appendChild(choosePiecePopUpContent)


                        for (let i = 0; i < promotablePieces.length; i++){
                            const choosablePiece = document.createElement("div")
                            choosePiecePopUpContent.appendChild(choosablePiece)
                            choosablePiece.innerText = promotablePieces[i]
                            choosablePiece.className = "popUpPiece"
                            choosablePiece.addEventListener("click", () => choosePromotionPiece(cell, promotablePieces[i], choosePiecePopUp))
                        }
                    }
                    else {
                        makeMove(cellToCharArray(chosenCell), cellToCharArray(cell))//.then(waitForAiMove)
                    }



                }
            })
        }
        RemoveHiglight(chosenCell)
    }

}

function choosePromotionPiece(cell, pieceType, popUp){
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
    await delay(15000);
    const response =  await getAiMove()
    const newBoard = []
    response.map((row) => {
        newBoard.push(row.split(''))
    })
    console.log(newBoard)
    ChangeBoard(newBoard)
};

MakeBoard(startState)
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
console.log(board)

/*HighlightSquare(3,4)
HighlightSquare(4,6)*/