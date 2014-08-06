package arithmetic.chc;

import java.io.*;

public class TestFile {

  public static File file;
 
  public static void display() throws IOException {
    System.out.println(file.getAbsolutePath());
    System.out.println(file.getCanonicalPath());
    System.out.println(file.getName());
    System.out.println(file.getParent());
    System.out.println(file.toString());
  }

  public static void main(String[] args) throws IOException {
    file = new File(args[0]);
    display();
  }
}
