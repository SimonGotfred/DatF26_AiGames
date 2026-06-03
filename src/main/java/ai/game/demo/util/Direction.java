package ai.game.demo.util;

public enum Direction
{
    NORTH ('↑',+0x10),
    N_EAST('↗',+0x11),
    EAST  ('→',+0x01),
    S_EAST('↘',-0x0F),
    SOUTH ('↓',-0x10),
    S_WEST('↙',-0x11),
    WEST  ('←',-0x01),
    N_WEST('↖',+0x0F),
    ;

    public  final    int    x88;
    public  final   char   icon;
    public  final String   type;
    private final String string, s_icon;

    Direction(char icon, int x88)
    {
        this.   x88 =  x88;
        this.  icon = icon;
        this.s_icon = icon+"";
        this.string = name().toLowerCase()
                            .replace("n_","north ")
                            .replace("s_","south ");
        this.type = icon<'↖'?"orthogonal":"diagonal";
    }

    public int numVal(){return x88;}

    public String icon    (){return s_icon;}
    public String toString(){return string;}
}
