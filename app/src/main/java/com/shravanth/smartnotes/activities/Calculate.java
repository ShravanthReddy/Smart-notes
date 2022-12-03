package com.shravanth.smartnotes.activities;
import com.shravanth.smartnotes.activities.numberProperties;
import java.util.Stack;

class Calculation {

    //string to store the text received
    public String text;

    //constructor
    public Calculation(String text) {
        this.text = text;
    }

    //do calculation function
    public String doCalculation() {
        //calling paranthesis function
        this.text = paranthesis(text);
        return text;

    }

    //paranthesis check function
    private String paranthesis(String text) {
        //creating a stack to store the open brackets
        Stack <Integer> openBracketStack = new Stack<>();

        //keeping a count of the open brackets
        int openBracketCount = 0;
        //a string to store the result
        String result;
        int i = 0;

        //looping until the end of the string
        while (i < text.length()) {
            //getting the char at the ith position
            char x = text.charAt(i);
            //checking if the char is a open bracket
            if (x == '(') {
                //pushing the index into the stack
                openBracketStack.push(i);
                //incrementing the counter
                openBracketCount++;

                //if the char is a closed bracket
            } else if(x == ')') {
                //if a open bracket is present
                if (openBracketCount > 0){
                    //getting the index of the recently opened bracket
                    int index = openBracketStack.pop();
                    openBracketCount--;
                    //calling the exponents check func with the substring and storing the result
                    result = exponents(text.substring(index + 1, i));

                    //if the returned string is "W" returning "W"
                    if (result == "W") {
                        return "W";

                    }
                    //concatenating the substring to the original string
                    text = text.substring(0, index) + result + text.substring(i + 1, text.length());

                    //changing the i as the string length is less than before
                    i = (index - 1) + result.length();

                    //if no open bracket is present, returning "W"
                } else {
                    return "W";

                }

            }
            //incrementing i value
            i++;
        }

        //if openbracket stack isempty calling exponents func and returning the output
        if(openBracketStack.isEmpty()) {
            return exponents(text);

            //else return "W"
        } else {
            return "W";

        }


    }

    //exponent check function
    private String exponents(String text) {
        //getting the index of the exponent
        int exponentIndex = text.indexOf("^");

        //result variable to store the result
        Double result = -1.0;

        //numberone and number two variable of number properties type
        numberProperties numberOne = new numberProperties();
        numberProperties numberTwo = new numberProperties();

        //if exponent in the string
        if (exponentIndex != -1) {

            //getting number one and number two var using two functions
            try {
                numberOne = getNumberOne(exponentIndex - 1, text);
                numberTwo = getNumberTwo(exponentIndex + 1, text);

                result = Math.pow(numberOne.getNum(), numberTwo.getNum());

            //try catch for null pointer exception
            } catch (NullPointerException e){
                return "W";

            }

            //creating a string answer
            String answer;

            //checking if the result contains any value after decimal
            if (result % 1.0 != 0){
                answer = String.format("%s", result);

            } else {
                answer = String.format("%.0f", result);

            }

            //creating a new string by replacing the original one with result
            String newText = replaceString(text, numberOne.getIndex(), numberTwo.getIndex(), answer);
            //recursive call to check if any other exponent symbols found
            return exponents(newText);

            //if no exponents found calling mul or div and returning the output
        } else {
            return mulOrDiv(text);

        }

    }

    //multiplication or division check function
    private String mulOrDiv(String text) {

        //getting the index of the multiplication and division index in the string
        int multiplicationOperatorIndex = text.indexOf("*");
        int DivisionOperatorIndex = text.indexOf("/");

        //result variable to store the result
        Double result = -1.0;

        //numberone and number two variable of number properties type
        numberProperties numberOne = new numberProperties();
        numberProperties numberTwo = new numberProperties();

        //if atleast one symbol is present in the string
        if (multiplicationOperatorIndex != -1 || DivisionOperatorIndex != -1) {

            //if either of the symbols are present at the end of the string returning "W"
            if ((multiplicationOperatorIndex == text.length() - 1 || DivisionOperatorIndex == text.length() - 1 )){
                return "W";
            }

            //if multiplication index is before division or division is not present in the equation
            if ((multiplicationOperatorIndex < DivisionOperatorIndex & multiplicationOperatorIndex != -1) || DivisionOperatorIndex == -1) {
                try {
                    numberOne = getNumberOne(multiplicationOperatorIndex - 1, text);
                    numberTwo = getNumberTwo(multiplicationOperatorIndex + 1, text);

                    result = Double.valueOf((numberOne.getNum() * numberTwo.getNum()));

                } catch (NullPointerException e){
                    return "W";

                }

                //if division index is before multiplication or multiplication is not present in the equation
            } else if ((multiplicationOperatorIndex > DivisionOperatorIndex & DivisionOperatorIndex != -1) || multiplicationOperatorIndex == -1) {
                try {
                    numberOne = getNumberOne(DivisionOperatorIndex - 1, text);
                    numberTwo = getNumberTwo(DivisionOperatorIndex + 1, text);

                    if (numberTwo.getNum() > 0) {
                        result = Double.valueOf((numberOne.getNum() / numberTwo.getNum()));

                    } else {
                        return "W";

                    }

                } catch (NullPointerException e){
                    return "W";

                }
            }

            //checking if the result contains any value after decimal
            String answer;
            if (result % 1.0 != 0){
                answer = String.format("%s", result);

            } else {
                answer = String.format("%.0f", result);

            }

            //creating a new string by replacing the original one with result
            String newText = replaceString(text, numberOne.getIndex(), numberTwo.getIndex(), answer);
            //recursive call to check if any other mul or div symbols found
            return mulOrDiv(newText);

            //if no exponents found calling mul or div and returning the output
        } else {
            return addOrSub(text);

        }

    }

    private String addOrSub(String text) {
        int i = 0;
        numberProperties numberOne = new numberProperties();
        numberProperties numberTwo = new numberProperties();

        Double result = -1.0;

        while (i < text.length()) {
            char x = text.charAt(i);
            if ((x == '+' || x == '-' ) & (i == text.length() - 1)){
                return "W";
            }
            if (i == 0 & x == '-') {

            } else if(x == '-' & i == 1) {
                return "W";

            } else if(x == '+' || x == '-') {
                try {
                    numberOne = getNumberOne(i - 1, text);
                    numberTwo = getNumberTwo(i + 1, text);

                    if (x == '+') {
                        result = Double.valueOf(( numberOne.getNum() + numberTwo.getNum() ));

                    } else {
                        result = Double.valueOf(( numberOne.getNum() - numberTwo.getNum() ));

                    }

                } catch (NullPointerException e){
                    return "W";

                }

                String answer;
                if (result % 1.0 != 0){
                    answer = String.format("%s", result);

                } else {
                    answer = String.format("%.0f", result);

                }

                text = text.substring(0, numberOne.getIndex()) + answer + text.substring(numberTwo.getIndex(), text.length());

                i = numberOne.getIndex() + answer.length() - 1;

            }
            i++;
        }

        return text;

    }

    //function to get the left number
    private numberProperties getNumberOne(int index, String text) {

        //two boolean vars
        boolean isNegative = false;
        boolean isNumberFound = false;
        boolean isDecimal = false;

        numberProperties result = new numberProperties();

        //if whitespace is present at the index
        if (Character.isWhitespace(text.charAt(index))) {
            //if index is not 0
            if (index != 0) {
                //recursive call to skip the whitespace
                return getNumberOne(index - 1, text);

                //whitespace at index returning null
            } else {
                return null;

            }

        }

        //moving index variable
        int movingIndex = index;

        //looping until number found
        while (!isNumberFound) {
            try {
                //if moving index is at the beginning of the string
                if (movingIndex == 0) {
                    //converting the char to integer
                    Integer.parseInt(text.substring(movingIndex, movingIndex + 1));
                    //changing number found to true
                    isNumberFound = true;

                } else {
                    //converting the char to integer
                    Integer.parseInt(text.substring(movingIndex, movingIndex + 1));
                    //decrementing moving index
                    movingIndex -= 1;
                }

                //to catch when the character is not an integer
            } catch(NumberFormatException e) {
                //getting the char at moving index
                char ch = text.charAt(movingIndex);

                //checking if it's a decimal value
                if ( ch == '.') {
                    //if decimal value is not found before
                    if(!isDecimal) {
                        //decrementing moving index
                        movingIndex -= 1;
                        isDecimal = true;

                        //if decimal value found before
                    } else {
                        return null;
                    }
                }
                //checking if the character is a subtraction symbol and is not found before
                //and if movingindex and index are not same
                else if ((ch == '-' & !isNegative) & (movingIndex != index)) {
                    isNegative = true;
                    isNumberFound = true;

                } else {
                    movingIndex += 1;
                    isNumberFound = true;

                }

            }
        }

        //setting the value and index to result object and returning result
        try {
            result.setNum(Double.parseDouble(text.substring(movingIndex, index + 1)));
            result.setIndex(movingIndex);
            return result;

            //if num format exception occured returning null
        } catch (NumberFormatException e) {
            return null;
        }

    }

    private numberProperties getNumberTwo(int index, String text) {

        boolean isNegative = false;
        boolean isNumberFound = false;
        numberProperties result = new numberProperties();

        if (Character.isWhitespace(text.charAt(index))) {
            if (index != text.length() - 1) {
                return getNumberTwo(index + 1, text);


            } else {
                return null;

            }

        }

        int movingIndex = index;

        while (!isNumberFound) {
            try {
                if (movingIndex == text.length() - 1) {
                    Integer.parseInt(text.substring(movingIndex, movingIndex + 1));
                    isNumberFound = true;
                    movingIndex += 1;

                } else {
                    Integer.parseInt(text.substring(movingIndex, movingIndex + 1));
                    movingIndex += 1;
                }

            } catch(NumberFormatException e) {
                char ch = text.charAt(movingIndex);
                if ( ch == '.') {
                    movingIndex += 1;

                } else if ((ch == '-' & !isNegative) & (movingIndex == index)) {
                    movingIndex += 1;
                    isNegative = true;

                } else if(ch == '-' & movingIndex == text.length() - 1) {
                    return null;

                } else {
                    isNumberFound = true;

                }

            }
        }

        try {

            result.setNum(Double.parseDouble(text.substring(index, movingIndex)));
            result.setIndex(movingIndex);
            return result;

        } catch (NumberFormatException e) {
            return null;
        }

    }

    private String replaceString(String text, int startIndex, int endIndex, String result) {
        String replacedText;
        String leftSubString, rightSubString;

        leftSubString = text.substring(0, startIndex);
        rightSubString = text.substring(endIndex, text.length());

        replacedText = leftSubString + (result) + rightSubString;
        return replacedText;

    }

}
