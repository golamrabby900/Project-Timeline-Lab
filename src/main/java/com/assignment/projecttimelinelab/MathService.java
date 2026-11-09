package com.assignment.projecttimelinelab;

public class MathService {

    // 1. PRIME NUMBER

    public static int isPrime(int number) {

        if (number < 2) {
            return 0;
        }

        for (int i = 2; i <= Math.sqrt(number); i++) {

            if (number % i == 0) {
                return 0;
            }
        }

        return 1;
    }


    // 2. MODULUS

    public static int calculateMod(int a, int b) {

        if (b == 0) {
            throw new IllegalArgumentException(
                    "Divisor cannot be zero."
            );
        }

        return a % b;
    }


    // 3. SINE

    public static double calculateSin(double degree) {

        double radians = Math.toRadians(degree);

        return Math.sin(radians);
    }


    // 4. COSINE

    public static double calculateCos(double degree) {

        double radians = Math.toRadians(degree);

        return Math.cos(radians);
    }


    // 5. TANGENT

    public static double calculateTan(double degree) {

        double radians = Math.toRadians(degree);

        if (Math.abs(Math.cos(radians)) < 1E-10) {
            throw new IllegalArgumentException(
                    "Tangent is undefined for this angle."
            );
        }

        return Math.tan(radians);
    }


    // 6. DECIMAL ANSWER COMPARISON

    public static boolean compareAnswers(
            double correctAnswer,
            double userAnswer) {

        return Math.abs(correctAnswer - userAnswer) < 0.001;
    }

}