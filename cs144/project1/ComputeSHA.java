import java.io.*;
import java.security.*;

public class ComputeSHA{
  public static void main(String args[]) throws IOException{

    // get the file from the args passed in
    File in = new File(args[0]);
    FileInputStream file = new FileInputStream(args[0]);
    byte[] data = new byte[(int) in.length()];

    // read the file and store it in data variable
    file.read(data);    

    try{
      // set up the MessageDigest
      MessageDigest msg = MessageDigest.getInstance("SHA-256");

      // use update and digest the data
      msg.update(data);
      byte[] bData = msg.digest();

      // this is copied from TA discussion slides
      StringBuilder out = new StringBuilder();
      for (byte b : bData){
        int dec = (int) b & 0xff;
        String hex = Integer.toHexString(dec);

        if (hex.length() % 2 == 1){
          hex = "0" + hex;
        }
        out.append(hex);
      }

      System.out.println(out.toString());
      
    }
    catch(NoSuchAlgorithmException e){
      System.out.println(e);
    } 
  }
}
