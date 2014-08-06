package arithmetic.chc;

public class T {
    public int val;
  
  public static void main(String[] args) {
    int i = Integer.valueOf(args[0]).intValue();
    int j = Integer.valueOf(args[1]).intValue();
    System.out.println("Before: i = " + i + "   j = " + j);
    T ii = new T(i);
    T jj = new T(j);
    add(ii, jj);
    System.out.println("After : i = " + i + "   j = " + j);
    System.out.println("After : i = " + ii.destroy() + 
                            "   j = " + jj.val);
    System.out.println("After : i = " + ii.val + 
                            "   j = " + jj.val);
    
  }

  T(int a) {
    val = a;
  }
  
  public T destroy() {
    return null;
  }
  
  public static void add(T iii, T jjj) {
    iii.val = (iii.val + jjj.val);
  }
  
} 
