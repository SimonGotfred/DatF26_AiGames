package ai.game.demo.util;

public class Printer
{
    public static void Print(String string){System.out.print(string);}
    public static void Print(Object object){System.out.print(object);}
    public static void Println(String string){System.out.println(string);}
    public static void Println(Object object){System.out.println(object);}
    public static void Println(){System.out.println();}

    private static final String unModifier = "\033[0m";
    private String modifier = "";

    public void print(String string){System.out.print(modifier+string+unModifier);}
    public void println(String string){System.out.println(modifier+string+unModifier);}
    public void print(Object object){print(object.toString());}
    public void println(Object object){println(object.toString());}
    public void println(){System.out.println();}

}
