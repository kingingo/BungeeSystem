package dev.wolveringer.bs.message;

import java.util.ArrayList;
import java.util.Iterator;

import dev.wolveringer.bs.Main;
import me.kingingo.kBungeeCord.Language.Language;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MessageManager {
	
	
	private ArrayList<String> titles = new ArrayList<>();
	private ArrayList<String> messages = new ArrayList<>();
	private Language lang;
	
	public void playTitles(ProxiedPlayer player){
		BungeeCord.getInstance().getScheduler().runAsync(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				Iterator<String> left = titles.iterator();
				while (left.hasNext()) {
					String next = left.next();
					Title title = BungeeCord.getInstance().createTitle();
					title.fadeIn(0);
					title.fadeOut(30);
					title.stay(30);
					title.title(TextComponent.fromLegacyText("§6§lEPICPVP.EU"));
					title.subTitle(TextComponent.fromLegacyText(next));
					try {
						Thread.sleep(2*1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
