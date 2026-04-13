
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

let startWhite = false;
let isWhite = false;

function MakeBoard(startState){
    startState.map((_row) => {
        const row = document.createElement("tr")
        table.appendChild(row)

        board.push([])
        _row.map((_cell) => {
            const cell = document.createElement("th")
            row.appendChild(cell)

            const space = document.createElement("div")
            cell.appendChild(space)
            cell.className = "Space"
            space.innerText += _cell ? _cell : ""

            space.addEventListener("click", () => clickSpace(space))

            if(startWhite && isWhite || !startWhite && !isWhite)
                cell.className += " White"
            else{
                cell.className += " Black"
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
            board[i][k].innerText = state[i][k]
        }
    }
}

function HighlightSquare(row, cell){
    board[row][cell].className += " Highlight"
}
function RemoveHiglight(row, cell){
    board[row][cell].className.replace("Highlight", "")
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

HighlightSquare(2,3)
HighlightSquare(4,7)