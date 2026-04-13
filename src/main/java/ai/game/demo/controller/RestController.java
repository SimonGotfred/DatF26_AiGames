package ai.game.demo.controller;

import ai.game.demo.agent.Agent;
import ai.game.demo.chess.Board;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
        session.setAttribute("Agent",new Agent<>(board));
        return ResponseEntity.ok(board.raw());
    }

    @GetMapping
    public ResponseEntity<List<char[]>> moves(@RequestParam String position, HttpServletRequest request)
    {
        List<char[]> moves = getAgent(request).getCurrentState().getPiece().moves().toList();
        return ResponseEntity.ok(moves);
    }

    @PatchMapping
    public ResponseEntity<char[][]> move(@RequestParam String move, HttpServletRequest request)
    {
        Agent<Board> agent = getAgent(request);
        agent.updateState(agent.getCurrentState().move(move));
        return ResponseEntity.accepted().build();
    }
}
