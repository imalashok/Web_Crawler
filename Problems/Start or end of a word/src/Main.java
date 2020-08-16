import java.util.*;
import java.util.regex.*;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String part = scanner.nextLine();
        String line = scanner.nextLine();

        Pattern pattern1 = Pattern.compile("\\b" + part, Pattern.CASE_INSENSITIVE);
        Pattern pattern2 = Pattern.compile(part + "\\b", Pattern.CASE_INSENSITIVE);

        Matcher matcher1 = pattern1.matcher(line);
        Matcher matcher2 = pattern2.matcher(line);

        System.out.println(matcher1.find() || matcher2.find() ? "YES" : "NO");
    }
}