package com.esgi.studyBuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.esgi.studyBuddy")
public class StudyBuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudyBuddyApplication.class, args);
	}

}
