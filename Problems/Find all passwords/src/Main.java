import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String text = scanner.nextLine();

        Pattern pattern = Pattern.compile("password[:\\s]*\\w*", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text);
        int cnt = 0;

        while (matcher.find()) {
            String[] pass = matcher.group().split("[:\\s]");
            System.out.println(pass[pass.length - 1]);
            cnt++;
        }
        if (cnt == 0) {
            System.out.println("No passwords found.");
        }
    }
}