package com.tosim.wxbot;

import com.tosim.wxbot.wxapi.handler.DefaultMessageHandler;
import com.tosim.wxbot.wxapi.WxEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAutoConfiguration
@SpringBootApplication
public class WeixinApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeixinApplication.class, args);
    }
}
