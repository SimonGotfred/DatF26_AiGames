package ai.game.demo;

import ai.game.demo.chess.FenReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication
{

    public static void main(String[] args)
    {
        FenReader fenReader = new FenReader();
        fenReader.read("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQ - 0 1");
        SpringApplication.run(DemoApplication.class, args);

    }

}
