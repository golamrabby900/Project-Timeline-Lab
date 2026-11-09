package com.assignment.projecttimelinelab;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@WebServlet("/calculator")
public class CalculatorServlet extends HttpServlet {

    private static final MathContext MC =
            new MathContext(12, RoundingMode.HALF_UP);

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        String contextPath = request.getContextPath();

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Basic Calculator</title>

                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">

                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            background: white;
                            font-family: Arial, sans-serif;
                        }

                        .calculator {
                            width: 340px;
                            padding: 15px;
                            border: 2px solid black;
                        }

                        .display {
                            width: 100%;
                            height: 85px;
                            border: 2px solid black;
                            margin-bottom: 12px;
                            padding: 12px;
                            font-size: 32px;
                            text-align: right;
                            background: white;
                            color: black;
                        }

                        .buttons {
                            display: grid;
                            grid-template-columns: repeat(4, 1fr);
                            gap: 7px;
                        }

                        button {
                            height: 65px;
                            border: 1px solid black;
                            background: white;
                            color: black;
                            font-size: 22px;
                            cursor: pointer;
                        }

                        button:hover {
                            background: #eeeeee;
                        }

                        .equals {
                            background: black;
                            color: white;
                        }

                        .equals:hover {
                            background: #333333;
                        }

                    </style>
                </head>

                <body>

                    <div class="calculator">

                        <input type="text"
                               id="display"
                               class="display"
                               value="0"
                               readonly>

                        <div class="buttons">

                            <button onclick="clearAll()">AC</button>
                            <button onclick="clearEntry()">C</button>
                            <button onclick="toggleSign()">±</button>
                            <button onclick="chooseOperator('/')">÷</button>

                            <button onclick="inputNumber('7')">7</button>
                            <button onclick="inputNumber('8')">8</button>
                            <button onclick="inputNumber('9')">9</button>
                            <button onclick="chooseOperator('*')">×</button>

                            <button onclick="inputNumber('4')">4</button>
                            <button onclick="inputNumber('5')">5</button>
                            <button onclick="inputNumber('6')">6</button>
                            <button onclick="chooseOperator('-')">−</button>

                            <button onclick="inputNumber('1')">1</button>
                            <button onclick="inputNumber('2')">2</button>
                            <button onclick="inputNumber('3')">3</button>
                            <button onclick="chooseOperator('+')">+</button>

                            <button onclick="inputNumber('0')">0</button>
                            <button onclick="inputDecimal()">.</button>
                            <button onclick="percentage()">%</button>
                            <button class="equals" onclick="calculate()">=</button>

                        </div>
                    </div>

                    <script>

                        const display =
                            document.getElementById("display");

                        const calculatorURL =
                            "__CONTEXT_PATH__/calculator";

                        let currentNumber = "0";
                        let firstNumber = null;
                        let operator = null;
                        let resetDisplay = false;

                        function updateDisplay() {
                            display.value = currentNumber;
                        }

                        function inputNumber(number) {

                            if (resetDisplay) {
                                currentNumber = number;
                                resetDisplay = false;
                            } else if (currentNumber === "0") {
                                currentNumber = number;
                            } else if (currentNumber === "-0") {
                                currentNumber = "-" + number;
                            } else {
                                currentNumber += number;
                            }

                            updateDisplay();
                        }

                        function inputDecimal() {

                            if (resetDisplay) {
                                currentNumber = "0";
                                resetDisplay = false;
                            }

                            if (!currentNumber.includes(".")) {
                                currentNumber += ".";
                            }

                            updateDisplay();
                        }

                        function clearAll() {

                            currentNumber = "0";
                            firstNumber = null;
                            operator = null;
                            resetDisplay = false;

                            updateDisplay();
                        }

                        function clearEntry() {

                            currentNumber = "0";
                            updateDisplay();
                        }

                        function toggleSign() {

                            if (currentNumber.startsWith("-")) {
                                currentNumber =
                                    currentNumber.substring(1);
                            } else if (currentNumber !== "0") {
                                currentNumber =
                                    "-" + currentNumber;
                            }

                            updateDisplay();
                        }

                        function percentage() {

                            let number =
                                parseFloat(currentNumber);

                            if (!Number.isFinite(number)) {
                                return;
                            }

                            currentNumber =
                                String(number / 100);

                            updateDisplay();
                        }

                        function chooseOperator(selectedOperator) {

                            if (operator !== null &&
                                !resetDisplay) {
                                calculate();
                            }

                            if (currentNumber === "Error") {
                                return;
                            }

                            firstNumber = currentNumber;
                            operator = selectedOperator;
                            resetDisplay = true;
                        }

                        async function calculate() {

                            if (firstNumber === null ||
                                operator === null ||
                                resetDisplay) {
                                return;
                            }

                            const secondNumber = currentNumber;

                            const formData = new URLSearchParams();

                            formData.append("first", firstNumber);
                            formData.append("second", secondNumber);
                            formData.append("operation", operator);

                            try {

                                const result = await fetch(
                                    calculatorURL,
                                    {
                                        method: "POST",
                                        headers: {
                                            "Content-Type":
                                                "application/x-www-form-urlencoded"
                                        },
                                        body: formData
                                    }
                                );

                                const answer = await result.text();

                                if (!result.ok) {
                                    currentNumber = "Error";
                                    display.value = answer;
                                } else {
                                    currentNumber = answer;
                                    updateDisplay();
                                }

                            } catch (error) {

                                currentNumber = "Error";
                                updateDisplay();
                            }

                            firstNumber = null;
                            operator = null;
                            resetDisplay = true;
                        }

                    </script>

                </body>
                </html>
                """;

        html = html.replace(
                "__CONTEXT_PATH__",
                contextPath
        );

        response.getWriter().write(html);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/plain;charset=UTF-8");

        try {

            BigDecimal first = new BigDecimal(
                    request.getParameter("first")
            );

            BigDecimal second = new BigDecimal(
                    request.getParameter("second")
            );

            String operation =
                    request.getParameter("operation");

            BigDecimal result;

            switch (operation) {

                case "+":
                    result = first.add(second, MC);
                    break;

                case "-":
                    result = first.subtract(second, MC);
                    break;

                case "*":
                    result = first.multiply(second, MC);
                    break;

                case "/":

                    if (second.compareTo(BigDecimal.ZERO) == 0) {
                        throw new ArithmeticException(
                                "Cannot divide by zero"
                        );
                    }

                    result = first.divide(second, MC);
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Invalid mathematical operation"
                    );
            }

            response.getWriter().write(
                    result.stripTrailingZeros().toPlainString()
            );

        } catch (ArithmeticException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(e.getMessage());

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "Invalid calculation"
            );
        }
    }
}