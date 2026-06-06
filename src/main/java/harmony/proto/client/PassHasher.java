package harmony.proto.client;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Scanner;

public class PassHasher {

    public static void main(String[] argv){
        Scanner scanner = new Scanner(System.in);

        while(true) {
            String username = scanner.nextLine();
            if(username.equals("bye")) {
                break;
            }
            System.out.println("username: " + username);
            String password = scanner.nextLine();
            System.out.println("password: " + password);
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            System.out.println("hash: " + hash);
        }

    }
}


