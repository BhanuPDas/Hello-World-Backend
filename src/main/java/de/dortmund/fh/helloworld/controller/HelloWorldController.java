package de.dortmund.fh.helloworld.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(allowedHeaders = "*")
public class HelloWorldController {

	@Value("${color}")
	private String color;

	@GetMapping("/greeting")
	public ResponseEntity<String> getMessage() {
		return ResponseEntity.status(HttpStatus.OK).body("<H1 style=\"color:" + color
				+ ";text-align:center;font-size:100px;padding-top:300px;padding-right:50px;padding-bottom:100px;padding-left:50px;\">Hello World!!</H1>");
	}

}
