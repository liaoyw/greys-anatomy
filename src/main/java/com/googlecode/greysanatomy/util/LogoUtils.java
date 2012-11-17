package com.googlecode.greysanatomy.util;

import java.util.Scanner;

public class LogoUtils {

	private static final String LOGO_PATH = "/com/googlecode/greysanatomy/res/logo.txt";
	
	public static String logo() {
		
		final StringBuilder logoSB = new StringBuilder();
		final Scanner scanner = new Scanner(Object.class.getResourceAsStream(LOGO_PATH));
		while( scanner.hasNextLine() ) {
			logoSB.append(scanner.nextLine()).append("\n");
		}
		
		return logoSB.toString();
		
	}
	
	public static void main(String... args) {
		
		System.out.println( logo() );
		
	}
	
}
