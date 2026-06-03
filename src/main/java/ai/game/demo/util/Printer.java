package ai.game.demo.util;

public class Printer
{
    public static void Print(String string){System.out.print(string);}
    public static void Print(Object object){System.out.print(object);}
    public static void Println(String string){System.out.println(string);}
    public static void Println(Object object){System.out.println(object);}
    public static void Println(){System.out.println();}

    private static final String error   = "\033[31;1;4m";
    private static final String warning = "\033[33;3m";
    private static final String unModifier = "\033[0m";

    public static void PrintError  (String string){System.out.println(error+   string +unModifier);}
    public static void PrintWarning(String string){System.out.println(warning+ string +unModifier);}

    private String modifier = "";

    public Printer(){}
    public Printer(String modifier){this.modifier=modifier;} // todo: ensure correct format

    public void print(String string){System.out.print(modifier+string+unModifier);}
    public void println(String string){System.out.println(modifier+string+unModifier);}
    public void print(Object object){print(object.toString());}
    public void println(Object object){println(object.toString());}
    public void println(){System.out.println();}
}
