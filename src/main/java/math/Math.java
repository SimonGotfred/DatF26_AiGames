package math;

public class Math
{
    static double power(double base, int exponent)
    {
        if (exponent == 0) return 1;
        if (exponent <  0) return base / power(base, ++exponent);
        else               return base * power(base, --exponent);
    }

    static int factorial(int n)
    {
        if (n == 0) return 1;
        if (n <  0) return n * factorial(n + 1);
        return n * factorial(n - 1);
    }

    static int denominator(int a, int b)
    {
        if (a < b) return denominator(a-b, b);
        if (a > b) return denominator(a, b-a);
        return a;
    }

    static int fibbonachi(int n)
    {
        if (n > 2) return fibbonachi(n - 1) + fibbonachi(n - 2);
        return 1;
    }
}
