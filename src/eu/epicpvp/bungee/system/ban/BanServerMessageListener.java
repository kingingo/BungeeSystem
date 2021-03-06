package eu.epicpvp.bungee.system.ban;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.BungeeUtil.Player;
import dev.wolveringer.maps.CachedHashMap;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenclient.client.PacketHandleErrorException;
import eu.epicpvp.datenclient.client.ProgressFuture;
import eu.epicpvp.datenclient.client.futures.BaseProgressFuture;
import eu.epicpvp.datenclient.client.futures.InstandProgressFuture;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.ban.BanEntity;
import eu.epicpvp.datenserver.definitions.dataserver.player.Setting;
import eu.epicpvp.datenserver.definitions.dataserver.protocoll.DataBuffer;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BanServerMessageListener implements Listener{
	@Getter
	private static BanServerMessageListener instance;

	private HashMap<Integer, Boolean> responses = new CachedHashMap<>(1, TimeUnit.MINUTES);

	public BanServerMessageListener() {
		instance = this;
	}

	@EventHandler
	public void a(ServerMessageEvent e){
		if(e.getChannel().equalsIgnoreCase("ban")){
			try{
				int playerId = e.getBuffer().readInt();
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayer(playerId);
				if(player == null)
					return;
				updateBan(player, e.getBuffer().readBoolean());
				String callbackServer = e.getBuffer().readString();
				Main.getDatenServer().getClient().sendServerMessage(callbackServer, "bancallback", new DataBuffer().writeInt(playerId).writeBoolean(true));
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		else if(e.getChannel().equalsIgnoreCase("bancallback")){
			responses.put(e.getBuffer().readInt(), e.getBuffer().readBoolean());
		}
	}

	public void updateBan(LoadedPlayer player,boolean active){
		List<BanEntity> entries = player.getBanStats(player.getSettings(Setting.CURRUNT_IP).getSync()[0].getValue(),1).getSync();
		Player plr = (Player) BungeeCord.getInstance().getPlayer(player.getName());
		if (plr != null && plr.isConnected()) {
			if (!entries.isEmpty() && entries.get(0).isActive()) {
				BannedServerManager.getInstance().joinServer(plr, entries.get(0));
			} else if (BannedServerManager.getInstance().isBanned(plr))
				BannedServerManager.getInstance().unban(plr);
		}
	}

	public ProgressFuture<Boolean> movePlayer(LoadedPlayer player,boolean active){
		if(BungeeCord.getInstance().getPlayer(player.getName()) != null){
			 updateBan(player, active);
			 return new InstandProgressFuture<Boolean>() {
				@Override
				public Boolean get() {
					return true;
				}
			};
		}
		final int playerId = player.getPlayerId();
		Main.getDatenServer().getClient().sendServerMessage(ClientType.BUNGEECORD, "ban", new DataBuffer().writeInt(player.getPlayerId()).writeBoolean(active).writeString(Main.getInstance().getServerId()));
		return new BaseProgressFuture<Boolean>() {
			public Boolean getSyncSave(int timeout) throws PacketHandleErrorException {
				long start = System.currentTimeMillis();
				while (!responses.containsKey(playerId)) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (start + timeout < System.currentTimeMillis()) {
						throw new RuntimeException("Timeout");
					}
				}
				return responses.get(playerId);
			}
		};
	}
}
