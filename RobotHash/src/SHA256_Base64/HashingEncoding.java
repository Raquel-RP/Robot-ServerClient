package SHA256_Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/**
 *
 * @author Raquel Romero Pedraza
 * 
 */

public class HashingEncoding {

    public static String sha256(final String base) {
        try{
            
            // String to bytes the hash it with SHA-256
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes("UTF-8"));  
            final StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) 
                  hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(UnsupportedEncodingException | NoSuchAlgorithmException ex){
           throw new RuntimeException(ex);
        }
    }
    
    public static String base64 (final String hash) {
        String encodedString = Base64.getEncoder().encodeToString(hash.getBytes());
        
        return encodedString;
    }
    
}
