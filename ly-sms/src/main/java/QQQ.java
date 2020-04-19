public class QQQ{
    public static void main(String[] ags){
        try{
            myException0();
            System.out.println(myException1());
            myException2();
        }catch(Exception e){
            try {
                System.out.println(myException3());
            } catch (Exception e1) {
                System.out.println(e1.getMessage());
            }
        }
    }

    public static void myException0(){
        System.out.println("0");
    }

    @SuppressWarnings("unused")
    public static String myException1() throws Exception{
        try{
            int i= 1/0;
            System.out.println("1");
        }catch(Exception e){
            return "len1";
        }
        return "len0";
    }

    @SuppressWarnings("unused")
    public static void myException2(){
        int i= 1/0;
        System.out.println("2");
    }

    @SuppressWarnings("unused")
    public static String myException3() throws Exception{
        try{
            int i= 1/0;
            System.out.println("3");
        }catch(Exception e){
            throw new Exception("throw new Exception");
        }
        return "len3";
    }
}
