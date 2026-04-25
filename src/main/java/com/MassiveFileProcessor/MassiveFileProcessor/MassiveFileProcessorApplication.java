package com.MassiveFileProcessor.MassiveFileProcessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MassiveFileProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MassiveFileProcessorApplication.class, args);
		System.out.println("=== MassiveFileProcessor iniciado ===");
        System.out.println("Endpoints disponibles:");
        System.out.println("  POST /api/upload - Subir archivo");
        System.out.println("  GET /api/status/{id} - Ver progreso");
        System.out.println("  GET /api/result/{id} - Obtener resultado");
        System.out.println("  POST /api/benchmark - Ejecutar benchmark");
	}

}
