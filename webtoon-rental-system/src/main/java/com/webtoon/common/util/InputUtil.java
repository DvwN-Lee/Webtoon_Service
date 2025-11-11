package com.webtoon.common.util;

import java.util.Scanner;

public class InputUtil {

    private static final Scanner scanner = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                String input = readLine(prompt);
                if (input.isEmpty()) {
                    System.out.println("오류: 입력값이 비어있습니다.");
                    continue;
                }
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("오류: 유효한 숫자를 입력해주세요.");
            }
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value < min || value > max) {
                System.out.println("오류: " + min + "에서 " + max + " 사이의 숫자를 입력해주세요.");
                continue;
            }
            return value;
        }
    }

    public static boolean confirm(String message) {
        while (true) {
            String input = readLine(message + " (y/n): ").toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("오류: 'y' 또는 'n'을 입력해주세요.");
        }
    }

    public static void pause() {
        readLine("\n계속하려면 Enter 키를 누르세요...");
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printSeparator() {
        System.out.println("=====================================");
    }

    public static void printHeader(String title) {
        printSeparator();
        System.out.println("  " + title);
        printSeparator();
    }
}
