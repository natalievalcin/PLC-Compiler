package plc.homework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. A framework of the test structure 
 * is provided, you will fill in the remaining pieces.
 *
 * To run tests, either click the run icon on the left margin, which can be used
 * to run all tests or only a specific test. You should make sure your tests are
 * run through IntelliJ (File > Settings > Build, Execution, Deployment > Build
 * Tools > Gradle > Run tests using <em>IntelliJ IDEA</em>). This ensures the
 * name and inputs for the tests are displayed correctly in the run window.
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests (see above note if not working).
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above.
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Two Letter Top-Level Domain", "email@gmail.bz", true),
                Arguments.of("Duplicate Alphanumeric", "----@yahoo.com", true),
                Arguments.of("No Alphanumeric After @", "email@.com", true),
                Arguments.of("One Alphanumeric After @", "onedomain@g.com", true),
                Arguments.of("Dot In Email", "natalie.valcin@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Multiple Domain Dot", "multipledots@gmail..com", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),
                Arguments.of("No Alphanumeric", "@gmail.com", false),
                Arguments.of("Missing Top-Level Domain Name", "natalievalcin@ufl.", false),
                Arguments.of("One Letter Top-Level Domain Name", "natalievalcin@ufl.e", false),
                Arguments.of("More Than Three Alphanumeric Top-Level Domain", "natalievalcin@ufl.morethanthree", false),
                Arguments.of("Missing @ Symbol", "missingtheatsymbolgmail.com", false),
                Arguments.of("Upper Case Domain", "uppercasedomain@gmail.COM", false),
                Arguments.of("Symbols in the Domain", "email@symbols#$%@.com", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                //what has ten letters and starts with gas?
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("10 Symbols", "#$%^*{,~`>", true),
                Arguments.of("Spacing", "          ", true),
                Arguments.of("Inclusive", "20 character is incl", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("13 Characters", "i<3pancakes9!", false),
                Arguments.of("22 Characters Because It's Even", "BohemianRhapsodyLover", false),
                Arguments.of("0 Characters", "", false),
                Arguments.of("21 Characters", "i<3pancakestwentychar", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("No Elements", "[]", true),
                Arguments.of("Spaces", "[1, 2, 3]", true),
                Arguments.of("Mixed Spaces", "[1,2, 3]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Trailing Comma", "[1, 2, 3,]", false),
                Arguments.of("Negative Integer", "[-1, 2, 3]", false),
                Arguments.of("Different Container Symbol", "(1, 2, 3)", false),
                Arguments.of("Alphabet Elements", "[a, b, c]", false),
                Arguments.of("Symbols", "[&, 2, 3]", false),
                Arguments.of("Leading Comma", "[,1,2,3]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success); //TODO
    }

    public static Stream<Arguments> testNumberRegex() {
        return Stream.of(
                Arguments.of("Single Integer", "1", true),
                Arguments.of("Decimal Number", "123.456", true),
                Arguments.of("Negative Sign Preceding Decimal Number", "-1.0", true),
                Arguments.of("Leading Zero", "0.25", true),
                Arguments.of("Trailing Zero", "25.0", true),
                Arguments.of("Trailing Decimal", " 1.", false),
                Arguments.of("Leading Decimal", ".5", false),
                Arguments.of("Comma As Decimal Number", "123,456", false),
                Arguments.of("Symbol Instead Of Negative Sign", "_1.45", false),
                Arguments.of("Signs After The Integer", "2.3+", false)
        ); //TODO
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success); //TODO
    }

    public static Stream<Arguments> testStringRegex() {
        return Stream.of(
                Arguments.of("Empty String Literal", "\"\"", true),
                Arguments.of("String Literal", "\"Hello World!\"", true),
                Arguments.of("Escape Characters", "\"1\\t2\"", true),
                Arguments.of("Multiple Escape Characters", "\"\\t\\b\"", true),
                Arguments.of("Multiple Quotes", "\"She said, \'Hello!\'\"", true),
                Arguments.of("Unterminated String Literal", "\"unterminated", false),
                Arguments.of("Non String Literal - Not Quoted", "unquoted", false),
                Arguments.of("Invalid Escape Character", "\"invalid\\escape\"", false),
                Arguments.of("Missing Beginning Quote", "open\"", false),
                Arguments.of("Non-Existent Escape Character","\"1\\f2\"", false),
                Arguments.of("Wrong Quote", "\'Hey! I am not a double quote.\'",false)
        ); //TODO
    }

    /**
     * Asserts that the input matches the given pattern. This method doesn't do
     * much now, but you will see this concept in future assignments.
     */
    private static void test(String input, Pattern pattern, boolean success) {
        Assertions.assertEquals(success, pattern.matcher(input).matches());
    }

}
