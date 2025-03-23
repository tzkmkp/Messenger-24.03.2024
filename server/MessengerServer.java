package com.server;

import java.net.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MessengerServer {
	private static final Logger logger = Logger.getLogger(MessengerServer.class.getName());

	public static void main(String[] args) {

		try (ServerSocket serverSocket = new ServerSocket(10001)) {
			new Thread(new com.server.Dispatcher()).start();

			logger.info("Waiting for incoming connection");

			ExecutorService executor = Executors.newCachedThreadPool();

			// Shutdown hook для корректного завершения работы сервера
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					logger.info("Server is shutting down.");
					executor.shutdown();
					serverSocket.close();
				} catch (IOException e) {
					logger.severe("Error during server shutdown: " + e.getMessage());
				}
			}));

			while (true) {
				Socket socket = serverSocket.accept();
				logger.info("New connection accepted");
				executor.submit(new com.server.Session(socket));
			}

		} catch (Exception ex) {
			logger.severe("Problem when starting server: " + ex.getMessage());
		}
	}


}
