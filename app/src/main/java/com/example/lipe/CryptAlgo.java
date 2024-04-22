package com.example.lipe;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class CryptAlgo {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String s = scan.nextLine();

        String code = crypt(s);

        System.out.println(code);

        String res = encrypt(code);

        System.out.println(res);
    }

    public static String encrypt(String s) {

        StringBuilder res = new StringBuilder();

        ArrayList<Integer> num = new ArrayList<>();

        char[] symbols = {
                'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м',
                'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ',
                'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З',
                'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х',
                'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+',
                '-', '=', '[', ']', '{', '}', '|', ';', ':', ',', '.', '<', '>', '?',
                '/', '~', ' ', '»', '«', '—', '"', '\''
        };

        for(int i = 1; i < s.length(); i++) {
            boolean fir = searchChar(s.charAt(i));
            boolean sec = searchChar(s.charAt(i + 1));
            boolean thir = searchChar(s.charAt(i + 2));
            boolean four = searchChar(s.charAt(i + 3));

            if((fir && thir && !sec) || (fir && !thir && !sec && !four)) {
                num.add(Integer.parseInt(String.valueOf(s.charAt(i))));
                i+=1;
            } else if(fir && !sec && !thir && four) {
                String curRes = String.valueOf(s.charAt(i)) +  String.valueOf(s.charAt(i + 3));
                num.add(Integer.parseInt(curRes));
                i+=4;
            } else if(fir && sec && thir) {
                String curRes = String.valueOf(s.charAt(i)) + String.valueOf(s.charAt(i + 1)) + String.valueOf(s.charAt(i + 2));
                num.add(Integer.parseInt(curRes));
                i+=3;
            } else if(!fir && !sec && !thir && !four) {
                break;
            }
        }

        for(int i = num.size() - 1; i >= 0; i--) {
            res.append(symbols[num.get(i)]);
        }

        return res.toString();
    }
    public static String crypt(String s) {

        StringBuilder res = new StringBuilder();

        ArrayList<Integer> num = new ArrayList<>();

        char[] engLetters = {
                'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z'
        };

        char[] engFig = {
                'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9'
        };

        char[] symbols = {
                'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м',
                'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ',
                'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З',
                'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х',
                'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd',
                'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+',
                '-', '=', '[', ']', '{', '}', '|', ';', ':', ',', '.', '<', '>', '?',
                '/', '~', ' ', '»', '«', '—', '"', '\''
        };

        for(int i = 0; i < s.length(); i++) {
            int idRus = searchLetter(symbols, s.charAt(i));
            if(idRus != -1) {
                num.add(idRus);
            }
        }

        Random random = new Random();

        res.append(engFig[random.nextInt(61)]);

        //crypt
        for(int i = num.size() - 1; i >= 0; i--) {
            int ch = num.get(i);
            String ch1 = String.valueOf(ch);
            int idEnd = random.nextInt(51);

            if(ch1.length() == 1) {
                int id = random.nextInt(61);
                res.append(ch1);
            } else if(ch1.length() == 2) {
                int id = random.nextInt(51);
                int id1 = random.nextInt(51);
                res.append(ch1.charAt(0));
                res.append(engLetters[id]);
                res.append(engLetters[id1]);
                res.append(ch1.charAt(1));
            } else if(ch1.length() == 3) {
                res.append(ch1.charAt(0));
                res.append(ch1.charAt(1));
                res.append(ch1.charAt(2));
            }
            res.append(engLetters[idEnd]);
        }

        if(res.length() < 62) {
            res.append(engLetters[random.nextInt(51)]);
            res.append(engLetters[random.nextInt(51)]);
            res.append(engLetters[random.nextInt(51)]);
            res.append(engLetters[random.nextInt(51)]);
        }

        if(res.length() < 64) {
            while(res.length() < 64) {
                res.append(engFig[random.nextInt(61)]);
            }
        }

        return res.toString();
    }


    private static Boolean searchChar(char searchChar) {

        char[] fig = {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9'
        };

        for(int i = 0; i < fig.length; i++) {
            if (fig[i] == searchChar) {
                return true;
            }
        }
        return false;
    }

    private static int searchLetter(char[] array, char searchChar) {
        for(int i = 0; i < array.length; i++) {
            if (array[i] == searchChar) {
                return i;
            }
        }
        return -1;
    }
}
