package ai.game.demo.controller;

import ai.game.demo.agent.Agent;
import ai.game.demo.chess.Board;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@CrossOrigin @RequestMapping("play")
@org.springframework.web.bind.annotation.RestController
public class RestController
{
    private Agent<Board> getAgent(HttpServletRequest request) {return (Agent<Board>) request.getSession().getAttribute("Agent");}

    @PutMapping
    public ResponseEntity<char[][]> newGame(HttpServletRequest request) throws IOException
    {
        HttpSession session = request.getSession();
        Board board = new Board();
        Agent<Board> agent = new Agent<>(board);
        session.setAttribute("Agent",agent);
        agent.start();
        return ResponseEntity.ok(board.raw());
    }

    @GetMapping
    public ResponseEntity<List<char[]>> possibleMoves(@RequestParam int[] position, HttpServletRequest request)
    throws IOException
    {
        if (request.getSession(false)==null) newGame(request);
        List<char[]> moves = new java.util.ArrayList<>(getAgent(request).getCurrentState().getPiece((char)position[0],(char)position[1]).moves().toList());
//        moves.replaceAll(move -> Board.letterize(move).toCharArray());
        return ResponseEntity.ok(moves);
    }

    @PostMapping
    public ResponseEntity<char[][]> playerMove(@RequestParam char[] from, @RequestParam char[] to, HttpServletRequest request)
    {
        Agent<Board> agent = getAgent(request);
        agent.updateState(agent.getCurrentState().move(from,to));
        return ResponseEntity.accepted().build();
    }

    @PatchMapping
    public ResponseEntity<char[][]> agentMove(HttpServletRequest request)
    {
        Agent<Board> agent = getAgent(request);
        return ResponseEntity.ok(agent.updateState(agent.getCurrentState().doBlack()).raw());
    }

    @DeleteMapping
    public ResponseEntity<String> delete(HttpServletRequest request)
    {
        if (request.getSession(false)!=null)
        {
            getAgent(request).Stop();
            request.getSession().removeAttribute("Agent");
            request.getSession().invalidate();
        }
        return ResponseEntity.ok().build();
    }
}
