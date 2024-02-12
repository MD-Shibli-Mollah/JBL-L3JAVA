package com.temenos.t24;

/**
 * TODO: Document me!
 *
 * @author MD Shibli Mollah
 *
 */
public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String myName = "MD TANVIR AHMED SHIBLI MOLLAH SON OF MD SADEK ALI MOLLAH AND LOVELY AK";
        Integer myNameLen = myName.length();
        System.out.println(myNameLen);
        if (myNameLen > 35){
            String myName35 = myName.substring(0, 35); //Total 35 characters excluding 35th number.
            String myName36 = myName.substring(35, myNameLen);
            
            System.out.println(myName35);
            System.out.println(myName36);
        }
        else
        {
            System.out.println(myName);
        }
        
    }

}
