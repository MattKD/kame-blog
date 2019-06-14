package blog;

import java.util.Date;
import java.util.Calendar;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Security {
  private static java.util.Random random = new java.util.Random();

  // Hash input using sha-256 into a 64 lengh hex string
  public static String hash(String input) {
    try {
      MessageDigest sha = MessageDigest.getInstance("SHA-256");
      byte[] hashedBytes = sha.digest(input.getBytes("UTF-8"));
      return bytesToHexString(hashedBytes);
    } catch (Exception e) {
      throw new RuntimeException("kame.Security.hash: SHA-256 unsupported");
    }
  }

  public static String hmac(String key, String data) {
    final String HMAC_SHA256 = "HmacSHA256";
    try {
        Mac sha512_hmac = Mac.getInstance(HMAC_SHA256);      
        byte[] key_bytes = key.getBytes("UTF-8");
        SecretKeySpec key_spec = new SecretKeySpec(key_bytes, HMAC_SHA256);
        sha512_hmac.init(key_spec);
        byte[] mac_data = sha512_hmac.doFinal(data.getBytes("UTF-8"));
        return bytesToHexString(mac_data);
    } catch (Exception e) {
      throw new RuntimeException("kame.Security.hmac: SHA-256 unsupported");
    }
  }

  // Create 1-64 length random hex string
  public static String genToken(int len) {
    int n = random.nextInt(); // 0 to 2^32-1
    String s = Integer.toHexString(n);
    s = hash(s); // 64 length hex string
    return s.substring(0, len);
  }

  // Convert byte array to a string of hex chars (0 to F)
  public static String bytesToHexString(byte[] bytes) {
    // fast lookup of hex chars
    char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
                    'A', 'B','C','D','E','F' };
    char[] cstr = new char[bytes.length * 2]; // hex is 2 chars per byte
    for (int i = 0; i < bytes.length; i++) {
      int val = bytes[i] & 0xFF;
      cstr[i * 2] = chars[val >>> 4]; // convert higher 4 bits to char
      cstr[i * 2 + 1] = chars[val & 0x0F]; // convert lower 4 bits
    }
    return new String(cstr);
  }

  public static UserTokenJson genSession(String key, int id, int days_expire) {
    var token = new UserTokenJson();
    token.id = id;

    var cal = Calendar.getInstance();
    cal.add(Calendar.DATE, days_expire);
    token.expires = cal.getTime();

    String data = "" + id + "-" + token.expires.getTime() + "-";
    token.token = data + hmac(key, data);
    return token;
  }

  public static UserTokenJson checkSession(String key, String session) {
    int id_end_idx = session.indexOf('-');
    if (id_end_idx == -1) {
      return null;
    }

    int date_end_idx = session.indexOf('-', id_end_idx + 1);
    if (date_end_idx == -1) {
      return null;
    }

    String data = session.substring(0, date_end_idx + 1);
    String hmac_data = session.substring(date_end_idx + 1);
    if (!hmac_data.equals(hmac(key, data))) {
      return null;
    }

    var token = new UserTokenJson();
    token.token = session;
    try {
      token.id = Integer.parseInt(session.substring(0, id_end_idx));
      String expires_str = session.substring(id_end_idx + 1, date_end_idx);
      token.expires = new Date(Long.parseLong(expires_str));
    } catch (Exception e) {
      return null;
    }
    
    return token;
  }
}
