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
    final static boolean runAgent = true;

    @PutMapping
    public ResponseEntity<Type[][]> newGame(HttpServletRequest request,
                                            @RequestParam(required=false) int[] from,
                                            @RequestParam(required=false) int[] to)
    {
        HttpSession session = request.getSession();
        if (session.getAttribute("Agent")!=null)
        {
            Agent<Board> agent = getAgent(request);
            if (from==null||to==null)
            {
                agent.Stop();
                session.removeAttribute("Agent");
            }
            else
            {
                return ResponseEntity.ok(agent.updateState(agent.getCurrentState().move(from,to),true).raw());
            }
        }
        Board board = new Board();
        Agent<Board> agent = new Agent<>(board);
        session.setAttribute("Agent",agent);
        agent.start(runAgent);if(!runAgent)agent.Stop();
        return ResponseEntity.ok(board.raw());
    }

    @GetMapping
    public ResponseEntity<List<Object>> possibleMoves(HttpServletRequest request, @RequestParam int[] position)
    {
        if (request.getSession(false)==null) newGame(request,null,null);
        Board board = getAgent(request).getCurrentState();
        Color color = board.at(position).color;

        List<Object> moves = List.of(board.movesFor(position).filter(m -> board.at(m).color!=color).toArray());
//        moves.replaceAll(move -> Board.letterize(move).toCharArray());
//        moves.replaceAll(move -> new int[]{(int) ((char[])move)[0], (int) ((char[])move)[1]});
        return ResponseEntity.ok(moves);
    }

    @PostMapping
    public ResponseEntity<Type[][]> playerMove(HttpServletRequest request,
                                               @RequestParam(required=false) Character promote,
                                               @RequestParam int[] from,
                                               @RequestParam int[] to)
    {
        Agent<Board> agent = getAgent(request);
        Board board = agent.getCurrentState();
        agent.updateState(board.move(from,board.isLegalMove(from,to)),!runAgent);
        return ResponseEntity.ok(agent.getCurrentState().raw());
    }

    @PatchMapping
    public ResponseEntity<Type[][]> agentMove(HttpServletRequest request)
    {
        Agent<Board> agent = getAgent(request);
        return ResponseEntity.ok(agent.act(!runAgent).raw());
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
