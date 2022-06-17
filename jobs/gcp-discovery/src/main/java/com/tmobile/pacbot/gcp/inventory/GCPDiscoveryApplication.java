package com.tmobile.pacbot.gcp.inventory;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan
public class GCPDiscoveryApplication {

	public static Map<String, Object> collect(String[] args) {
		ApplicationContext context = new AnnotationConfigApplicationContext(GCPDiscoveryApplication.class);
		GCPFetchOrchestrator orchestrator = context.getBean(GCPFetchOrchestrator.class);
		return orchestrator.orchestrate();
	}
}

