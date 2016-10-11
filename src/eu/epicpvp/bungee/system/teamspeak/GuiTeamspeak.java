package eu.epicpvp.bungee.system.teamspeak;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.listener.PlayerJoinListener;
import eu.epicpvp.bungee.system.gui.GuiUpdating;
import eu.epicpvp.bungee.system.item.ItemBuilder;
import eu.epicpvp.bungee.system.permission.Group;
import eu.epicpvp.bungee.system.permission.PermissionManager;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import eu.epicpvp.dataserver.protocoll.packets.PacketTeamspeakAction;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.StatsKey;
import eu.epicpvp.datenserver.definitions.gamestats.Statistic;
import eu.epicpvp.thread.ThreadFactory;

public class GuiTeamspeak extends GuiUpdating {
	private NBTTagCompound properties;
	private String identity;
	public GuiTeamspeak() {
		super(1, "§aTeamspeak Interface");
	}

	@Override
	public void build() {
		fill(ItemBuilder.create(160).durability(7).name("§7").build(), 0 , -1,true);
		inv.setItem(0, ItemBuilder.create(Material.BARRIER).name("§cSchließen").listener((Click c) -> c.getPlayer().closeInventory()).build());
		inv.setItem(4, ItemBuilder.create(Material.WATCH).name("§eLoading informations").glow().build());
		loadInformation();
	}

	private void loadInformation(){
		ThreadFactory.getFactory().createThread(new Runnable() {
			@Override
			public void run() {
				LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
				Statistic[] stats = lplayer.getStats(GameType.TEAMSPEAK).getSync();

				for(Statistic s : stats){
					if(s.getStatsKey() == StatsKey.TEAMSPEAK_PROPERTIES){
						String value = (String) s.getValue();
						if(value == null || value.length() == 0)
							properties = new NBTTagCompound();
						else
							try {
								properties = NBTCompressedStreamTools.read(value);
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
					else if(s.getStatsKey() == StatsKey.TEAMSPEAK_IDENTITY){
						identity = (String) s.getValue();
						if("".equalsIgnoreCase(identity))
							identity = null;
					}
				}
				buildIdentity();
				buildProperties();
			}
		}).start();
	}

	private void buildIdentity(){
		if(identity != null)
			inv.setItem(4, ItemBuilder.create(Material.WATCH).name("§aDu bist momentan mit der Indentität §e"+identity+" §averlinkt").build());
		else
			inv.setItem(4, ItemBuilder.create(Material.WATCH).name("§aDu bist mit keiner Identität verlinkt!").build());
	}

	private void buildProperties(){
		if(this.identity == null)
			return;
		inv.setItem(8, ItemBuilder.create(Material.LAVA_BUCKET).name("§cLösche diese Verlinkung!").listener(()->{
			LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
			Main.getDatenServer().getClient().writePacket(new PacketTeamspeakAction(lplayer.getPlayerId(), eu.epicpvp.dataserver.protocoll.packets.PacketTeamspeakAction.Action.UNLINK, null));
			getPlayer().closeInventory();
			getPlayer().sendMessage("§aDu hast deine Verlinkung aufgehoben.");
		}).build());
	}

	@Override
	public void updateInventory() {
		if(this.identity == null)
			return;
		buildProperies();
	}

	private void buildProperies(){
		if(properties == null)
			return;
		String time = null;
		if(properties.hasKey("update") && properties.getCompound("update").hasKey("group"))
			if(properties.getCompound("update").getLong("group")-System.currentTimeMillis() > 0)
				time = PlayerJoinListener.getDurationBreakdown(properties.getCompound("update").getLong("group")-System.currentTimeMillis(),"1 Sekunde");
		if(time != null && !PermissionManager.getManager().hasPermission(getPlayer(), "teamspeak.update.group.nocooldown")){
			inv.setItem(2, ItemBuilder.create(Material.DIAMOND_PICKAXE).name("§aUpdate deine Gruppen").lore("§cDu kannst diese Funktion erst").lore("§cwieder in "+time+" nutzen!").build());
		}
		else
		{
			inv.setItem(2, ItemBuilder.create(Material.DIAMOND_PICKAXE).name("§aUpdate deine Gruppen").lore("§aKlicke hier um deine Gruppen zu updaten.").listener(()->{
				getPlayer().closeInventory();
				getPlayer().sendMessage("§aDeine Gruppen werden geupdated!");
				if(!properties.hasKey("update"))
					properties.set("update", new NBTTagCompound());
				properties.getCompound("update").setLong("group", System.currentTimeMillis()+120*60*1000);
				LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
				try {
					lplayer.setStats(new PacketInStatsEdit.EditStats(GameType.TEAMSPEAK, Action.SET, StatsKey.TEAMSPEAK_PROPERTIES, NBTCompressedStreamTools.toString(properties)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				List<Group> groups = PermissionManager.getManager().getPlayer(lplayer.getPlayerId()).getGroups();
				Collections.sort(groups,new Comparator<Group>() {
					@Override
					public int compare(Group o1, Group o2) {
						return Integer.compare(o2.getImportance(), o1.getImportance());
					}
				});
				Main.getDatenServer().getClient().writePacket(new PacketTeamspeakAction(lplayer.getPlayerId(), eu.epicpvp.dataserver.protocoll.packets.PacketTeamspeakAction.Action.UPDATE_GROUPS, groups.get(0).getName()));
			}).build());
		}

		time = null;
		if(properties.hasKey("update") && properties.getCompound("update").hasKey("icon"))
			if(properties.getCompound("update").getLong("icon")-System.currentTimeMillis() > 0)
				time = PlayerJoinListener.getDurationBreakdown(properties.getCompound("update").getLong("icon")-System.currentTimeMillis(),"1 Sekunde");
		if(time != null && !PermissionManager.getManager().hasPermission(getPlayer(), "teamspeak.update.icon.nocooldown")){
			inv.setItem(6, ItemBuilder.create(Material.IRON_AXE).name("§aUpdate dein Icon").lore("§cDu kannst diese Funktion erst").lore("§cwieder in "+time+" nutzen!").build());
		}
		else
		{
			inv.setItem(6, ItemBuilder.create(Material.IRON_AXE).name("§aUpdate dein Icon").lore("§aKlicke hier um dein Icon zu updaten.").listener(()->{
				getPlayer().closeInventory();
				getPlayer().sendMessage("§aDein Icon wird geupdated!");
				if(!properties.hasKey("update"))
					properties.set("update", new NBTTagCompound());
				properties.getCompound("update").setLong("icon", System.currentTimeMillis()+120*60*1000);
				LoadedPlayer lplayer = Main.getDatenServer().getClient().getPlayerAndLoad(getPlayer().getName());
				try {
					lplayer.setStats(new PacketInStatsEdit.EditStats(GameType.TEAMSPEAK, Action.SET, StatsKey.TEAMSPEAK_PROPERTIES, NBTCompressedStreamTools.toString(properties)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				Main.getDatenServer().getClient().writePacket(new PacketTeamspeakAction(lplayer.getPlayerId(), eu.epicpvp.dataserver.protocoll.packets.PacketTeamspeakAction.Action.UPDATE_AVATAR, null));
			}).build());
		}
	}
}
