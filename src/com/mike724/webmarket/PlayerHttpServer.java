package com.mike724.webmarket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.mike724.webmarket.util.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class PlayerHttpServer {
	private Logger log;
	public void start() throws IOException  {
		InetSocketAddress addr = new InetSocketAddress(Settings.HTTPPORT);
		HttpServer server = HttpServer.create(addr, 0);
		server.createContext("/", new Handler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		log.info("WebMarket player HTTP server is listening on port " + Settings.HTTPPORT);
	}
}

class Handler implements HttpHandler {

	private static HashMap<String, String> GetPlayerData(String player) {
		HashMap<String, String> Data = new HashMap<String, String>();
		Data.put("name", player);
		Data.put("balance", ""+VaultManager.economy.getBalance(player));
		return Data;
	}

	private void OutputBalance(OutputStream responseBody, String player) {
		HashMap<String,String> Data = GetPlayerData(player);
		String Response = "";
		for (Iterator<Map.Entry<String, String>> i = Data.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, String> hm = i.next();
			Response += hm.getKey() + ":" + hm.getValue() + "\n";
		}
		try {
			responseBody.write(Response.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void handle(HttpExchange exchange) throws IOException {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, 0);
			OutputStream responseBody = exchange.getResponseBody();

			String[] args = exchange.getRequestURI().toASCIIString().split("/");
			args = Arrays.copyOfRange(args, 1, args.length);

			if(args[3].equalsIgnoreCase(Settings.SECRETKEY)) {
				if(args[1].equalsIgnoreCase("get")) OutputBalance(responseBody,args[0]);
				if(args[1].equalsIgnoreCase("add"))
				{
					VaultManager.economy.depositPlayer(args[0], Double.parseDouble(args[2]));
					OutputBalance(responseBody,args[0]);
				}
				if(args[1] .equalsIgnoreCase("subtract"))
				{
					VaultManager.economy.withdrawPlayer(args[0], Double.parseDouble(args[2]));
					OutputBalance(responseBody,args[0]);
				}
			}
			else responseBody.write("invalid".getBytes());
			responseBody.close();
	}

}