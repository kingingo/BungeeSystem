package eu.epicpvp.bungee.system.bs;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.BungeeCord;

public class UtilBungeeCord {

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
		}
		else
			BungeeCord.getInstance().getConsole().sendMessage("§cDid not found the restart file... cant restart");
		BungeeCord.getInstance().stop();
	}

}
