package battleships_ex.gdx.data;

import java.security.SecureRandom;

/**
 * Generates unique 6-character alphanumeric room codes for lobby sessions.
 * Uses only uppercase letters and digits, excluding ambiguous characters (O, 0, I, 1, L).
 */
public class RoomCodeGenerator {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALLOWED_CHARS.charAt(random.nextInt(ALLOWED_CHARS.length())));
        }
        return code.toString();
    }
}
