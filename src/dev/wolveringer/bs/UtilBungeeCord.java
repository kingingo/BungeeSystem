package dev.wolveringer.bs;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class UtilBungeeCord {
	/*
	@Getter
	@Setter
	public static LagMeter lagMeter;
	
	public static LagMeter createLagMeter(){
		if(lagMeter==null)setLagMeter(new LagMeter(instance));;
		return lagMeter;
	}
	*/
	public static void DebugLog(long time,String[] Reason,String c){
		System.err.println("[DebugMode]: Class: "+c);
		for(String r : Reason){
			System.err.println("[DebugMode]: Reason: "+r);
		}
		System.err.println("[DebugMode]: Zeit: "+ ((System.currentTimeMillis()-time) / 1000.0D) + " Seconds");
	}
	
	public static void DebugLog(long time,String Reason,String c){
		System.err.println("[DebugMode]: Class: "+c);
		System.err.println("[DebugMode]: Reason: "+Reason);
		System.err.println("[DebugMode]: Zeit: "+ ((System.currentTimeMillis()-time) / 1000.0D) + " Seconds");
	}
	
	public static void DebugLog(long time,String c){
		System.err.println("[DebugMode]: Class: "+c);
		System.err.println("[DebugMode]: Zeit: "+ ((System.currentTimeMillis()-time) / 1000.0D) + " Seconds");
	}
	
	public static void DebugLog(String m){
		System.err.println("[DebugMode]: "+m);
	}
	
	public static void restart(){
		if(new File("start.sh").exists()){
			Thread hook = new Thread(){
				public void run(){
					try {
						Runtime.getRuntime().exec("./start.sh");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			hook.setDaemon(true);
			Runtime.getRuntime().addShutdownHook(hook);
			BungeeCord.getInstance().stop();
		}
	}
	
}
