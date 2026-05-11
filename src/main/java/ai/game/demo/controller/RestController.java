package ai.game.demo.controller;

import ai.game.demo.agent.Agent;
import ai.game.demo.chess.Board;
import ai.game.demo.chess.Type;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@CrossOrigin @RequestMapping("play")
@org.springframework.web.bind.annotation.RestController
public class RestController
{
    private Agent<Board> getAgent(HttpServletRequest request) {return (Agent<Board>) request.getSession().getAttribute("Agent");}

    @PutMapping
    public ResponseEntity<char[][]> newGame(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        if (session.getAttribute("Agent")!=null)
        {
            getAgent(request).Stop();
            session.removeAttribute("Agent");
        }
        Board board = new Board();
        Agent<Board> agent = new Agent<>(board);
        session.setAttribute("Agent",agent);
        agent.start();
        return ResponseEntity.ok(board.raw());
    }

    @GetMapping
    public ResponseEntity<List<Object>> possibleMoves(HttpServletRequest request, @RequestParam int[] position)
    {
        if (request.getSession(false)==null) newGame(request);
        Board board = getAgent(request).getCurrentState();
        Color color = Type.color(board.at(position));

        List<Object> moves = List.of(board.movesFor(position).filter(m -> Type.color(board.at(m))!=color).toArray());
//        moves.replaceAll(move -> Board.letterize(move).toCharArray());
//        moves.replaceAll(move -> new int[]{(int) ((char[])move)[0], (int) ((char[])move)[1]});
        return ResponseEntity.ok(moves);
    }

    @PostMapping
    public ResponseEntity<char[][]> playerMove(HttpServletRequest request,
                                               @RequestParam(required=false) Character promote,
                                               @RequestParam int[] from,
                                               @RequestParam int[] to)
    {
        Agent<Board> agent = getAgent(request);
        agent.updateState(agent.getCurrentState().move(from,to));
        return ResponseEntity.ok(agent.getCurrentState().raw());
    }

    @PatchMapping
    public ResponseEntity<char[][]> agentMove(HttpServletRequest request)
    {
        Agent<Board> agent = getAgent(request);
        return ResponseEntity.ok(agent.act(true).raw());
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
