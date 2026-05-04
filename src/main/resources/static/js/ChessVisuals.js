
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
    ["♜","♞","♝","♚","♛","♝","♞","♜"],]

//let dd = String.fromCharCode("♕".charCodeAt(0) + 6)
let startWhite = false;
let isWhite = false;

let blackChars = ["♖","♘","♗","♕","♔","♙"]

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
            piece.innerText += _cell ? _cell : ""
            SetBlackOrWhite(piece)

            cell.addEventListener("click",() => pressCell(cell))

            console.log(board.length -1 + cell.piece.innerText)


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
            board[i][k].piece.innerText = state[i][k]
            SetBlackOrWhite(board[i][k].piece)
        }
    }
}
function SetBlackOrWhite(piece){
    piece.className.replace("BlackPiece", "")
    blackChars.map((char) => {
        if(piece.innerText === char){
            piece.innerText = String.fromCharCode(char.charCodeAt(0) + 6)
            piece.className += " BlackPiece"
        }
    })
}

function HighlightSquare(row, cell){
    board[row][cell].className += " Highlight"
}
function RemoveHiglight(row, cell){
    board[row][cell].className.replace("Highlight", "")
}

function pressCell(cell){
    console.log("pressed cell")
}

MakeBoard(startState)
const testState = [
    ["♖","♘","♗","♕","♔","♗","♘","♖"],
    ["♙","♙","♙","♙","","♙","♙","♙"],
    ["","","","","","","",""],
    ["","","","","♙","","",""],
    ["","","","♟","","","",""],
    ["","","","","","","",""],
    ["♟","♟","♟","","♟","♟","♟","♟",],
    ["♜","♞","♝","♚","♛","♝","♞","♜"],]
ChangeBoard(testState)
console.log(board)

HighlightSquare(3,4)
HighlightSquare(4,6)