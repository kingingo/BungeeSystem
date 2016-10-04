package eu.epicpvp.bungee.system.permission;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.BungeeUtil.Player;
import eu.epicpvp.bungee.system.ban.BannedServerManager;
import eu.epicpvp.bungee.system.bs.Main;
import eu.epicpvp.bungee.system.bs.client.event.ServerMessageEvent;
import eu.epicpvp.bungee.system.bs.login.LoginManager;
import dev.wolveringer.bukkit.permissions.GroupTyp;
import dev.wolveringer.bukkit.permissions.PermissionType;
import eu.epicpvp.datenclient.client.Callback;
import eu.epicpvp.datenclient.client.LoadedPlayer;
import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus;
import eu.epicpvp.bungee.system.mysql.MySQL;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PermissionManager implements Listener {
	private static PermissionManager manager;

	public static PermissionManager getManager() {
		return manager;
	}

	public static void setManager(PermissionManager manager) {
		PermissionManager.manager = manager;
	}

	@Getter
	private ArrayList<Group> groups = new ArrayList<>();
	private HashMap<Integer, PermissionPlayer> user = new HashMap<>();

	public PermissionManager() {
		if (BungeeCord.getInstance() != null) { //Testing Bungee=Null
			BungeeCord.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
			BungeeCord.getInstance().registerChannel("permission");
		}
	}

	public void loadGroups() {
		ArrayList<String[]> qurry = MySQL.getInstance().querySync("SELECT DISTINCT `pgroup` FROM `game_perm` WHERE `pgroup`!='none' AND `playerId`='-2' ", -1);
		long start = System.currentTimeMillis();
		System.out.println("Loading permission groups...");
		for (String[] var : qurry)
			groups.add(new Group(this, var[0]));
		for (Group g : groups)
			g.initPerms();
		System.out.println("Done (" + (System.currentTimeMillis() - start) + ")");
	}

	public void loadPlayer(Integer player) {
		if (!user.containsKey(player))
			user.put(player, new PermissionPlayer(this, player));
	}

	public void loadPlayer(UUID player) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		loadPlayer(p.getPlayerId());
	}

	public PermissionPlayer getPlayer(Integer player) {
		if (user.get(player) == null)
			loadPlayer(player);
		return user.get(player);
	}

	public PermissionPlayer getPlayer(UUID player) {
		LoadedPlayer p = Main.getDatenServer().getClient().getPlayerAndLoad(player);
		return getPlayer(p.getPlayerId());
	}

	public boolean hasPermission(ProxiedPlayer player, String permission) {
		return hasPermission(player.getName(), permission);
	}

	public boolean hasPermission(ProxiedPlayer player, PermissionType teamMessage) {
		return hasPermission(player.getName(), teamMessage.getPermissionToString());
	}

	public boolean hasPermission(ProxiedPlayer player, PermissionType teamMessage, boolean message) {
		return hasPermission(player, teamMessage.getPermissionToString(), message);
	}

	public boolean hasPermission(CommandSender player, String permission) {
		return hasPermission(player, permission, false);
	}

	public boolean hasPermission(CommandSender player, String permission, boolean message) {
		if (player instanceof ProxiedPlayer)
			return hasPermission((ProxiedPlayer) player, permission, message);
		return true;
	}

	public boolean hasPermission(CommandSender player, PermissionType teamMessage, boolean message) {
		if (player instanceof ProxiedPlayer)
			return hasPermission((ProxiedPlayer) player, teamMessage.getPermissionToString(), message);
		return true;
	}

	public boolean hasPermission(ProxiedPlayer player, String permission, boolean message) {
		if (!LoginManager.getManager().isLoggedIn(player) || BannedServerManager.getInstance().isBanned((Player) player))
			return false; //Not logged in Player cant have perms
		boolean perm = hasPermission(player.getName(), permission);
		if (message && !perm)
			player.sendMessage(Main.getTranslationManager().translate("prefix", player) + "§cYou don't have permission to do that.");
		return perm;
	}

	public boolean hasPermission(UUID uuid, PermissionType permission) {
		return hasPermission(uuid, permission.getPermissionToString());
	}

	public boolean hasPermission(UUID uuid, String permission) {
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(uuid);
		return hasPermission(player.getPlayerId(), permission);
	}

	public boolean hasPermission(String name, PermissionType permission) {
		return hasPermission(name, permission.getPermissionToString());
	}

	public boolean hasPermission(String name, String permission) {
		LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(name);
		return hasPermission(player.getPlayerId(), permission);
	}

	public boolean hasPermission(Integer playerId, String permission) {
		PermissionPlayer pplayer = user.get(playerId);
		if (pplayer == null)
			user.put(playerId, pplayer = new PermissionPlayer(this, playerId));
		return pplayer.hasPermission(permission);
	}

	public Group getGroup(String name) {
		for (Group g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}

	public Group addGroup(String name) {
		for (Group g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		Group g;
		groups.add(g = new Group(this, name));
		return g;
	}

	public void removeGroup(String name) {
		Group gg = null;
		for (Group g : groups)
			if (g.getName().equalsIgnoreCase(name))
				gg = g;
		if (gg == null)
			return;
		groups.remove(gg);
		gg.delete();
	}

	@EventHandler
	public void onServerMessage(ServerMessageEvent e) {
		if (e.getChannel().equalsIgnoreCase("permission")) {
			byte action = e.getBuffer().readByte();
			if (action == 2) {
				int playerID = e.getBuffer().readInt();
				PermissionPlayer player = PermissionManager.getManager().getPlayer(playerID);
				String neededGroup = e.getBuffer().readString();
				boolean hase = neededGroup == null;
				for (Group g : new ArrayList<>(player.getGroups())) {
					if (g.getName().equalsIgnoreCase(neededGroup))
						hase = true;
					player.removeGroup(g.getName());
				}
				String group = e.getBuffer().readString();
				System.out.println("Changing group from " + player.getPlayerId() + " to " + group);
				if (hase)
					player.addGroup(group);
			} else if (action == 3) {
				int playerID = e.getBuffer().readInt();
				PermissionPlayer player = PermissionManager.getManager().getPlayer(playerID);
				String permission = e.getBuffer().readString();
				GroupTyp type = GroupTyp.values()[e.getBuffer().readInt()];

				System.out.println("Adding permission " + permission + ":" + type + " to " + player.getPlayerId());
				player.addPermission(permission, type);
			}
		}
		if (e.getChannel().equalsIgnoreCase("bpermission")) {
			byte action = e.getBuffer().readByte();
			if (action == 0) { //Remove cached player
				int users;
				user.remove(users = e.getBuffer().readInt());
				System.out.println("Cleaning user: " + users);
			} else if (action == 1) {
				String group = e.getBuffer().readString();
				for (Group g : groups)
					if (g.getName().equalsIgnoreCase(group)) {
						System.out.println("Reload permission group: " + g.getName());
						g.reload();
						g.initPerms();
					}
			}
		}
	}

	protected void updatePlayer(int player) {
		Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "bpermission", new DataBuffer().writeByte(0).writeInt(player)).getAsync(new Callback<PacketOutPacketStatus.Error[]>() {
			@Override
			public void call(PacketOutPacketStatus.Error[] obj, Throwable e) {
				if (e != null) {
					e.printStackTrace();
					return;
				}
				Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "permission", new DataBuffer().writeByte(0).writeInt(player));
			}
		});
		;
	}

	protected void updateGroup(Group group) {
		Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "bpermission", new DataBuffer().writeByte(1).writeString(group.getName())).getAsync(new Callback<PacketOutPacketStatus.Error[]>() {
			@Override
			public void call(PacketOutPacketStatus.Error[] obj, Throwable e) {
				if (e != null) {
					e.printStackTrace();
					return;
				}
				Main.getDatenServer().getClient().sendServerMessage(ClientType.ALL, "permission", new DataBuffer().writeByte(1).writeString(group.getName()));
			}
		});
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		if (e.getSender() instanceof ProxiedPlayer && !e.getTag().startsWith("MC") && !e.getTag().startsWith("WECUI") && !e.getTag().startsWith("bungeecord")) {
			e.setCancelled(true);
			System.out.print("Player " + ((ProxiedPlayer) e.getSender()).getName() + " try to send a plugin message on Channel: " + e.getTag());
			return;
		}
		if (e.getTag().equalsIgnoreCase("permission")) {
			try {
				DataInputStream stream = new DataInputStream(new ByteArrayInputStream(e.getData()));
				//Aufbau ([UUID (Packet UUID)] [INTEGER (Action)] [Data (variable length)])
				DataBuffer buffer = new DataBuffer(stream);
				UUID packetUUID = buffer.readUUID();

				ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
				int action = buffer.readByte();
				if (action == 0) { //INFO Get player permissions ([UUID (player)])
					PermissionPlayer p = getPlayer(buffer.readInt());
					if (p == null)
						sendToBukkit(packetUUID, new DataBuffer().writeInt(-1).writeString("Player not found"), player.getServer().getInfo()); //Response (Player not found) [UUID (packet)] [INT -1] [STRING reson]
					else {
						DataBuffer out = new DataBuffer();
						List<Group> groupsBase = new ArrayList<>(p.getGroups());
						List<Group> groups = new ArrayList<>(groupsBase);
						for (Group group : groupsBase) {
							groups.addAll(group.getGroupsDeep());
						}
						out.writeInt(groups.size());
						Collections.sort(groups, (a, b) -> Integer.compare(b.getImportance(), a.getImportance()));
						for (Group group : groups) {
							out.writeString(group.getName());
						}
						out.writeInt(p.getPermissions().size());
						for (Permission perm : p.getPermissions()) {
							out.writeString(perm.getPermission());
							out.writeByte(perm.getGroup().ordinal());
						}
						System.out.print("Requesting permission " + player.getName() + " Action " + action);
						sendToBukkit(packetUUID, out, player.getServer().getInfo()); //Response (Permissions) [UUID (packet)] [INT Group-Length] [STRING[] groups] [INT perms-Length] [STRING[] perms]
					}
				} else if (action == 1) {//INFO Get group permissions ([String (group)])
					String group = buffer.readString();
					Group g = getGroup(group);
					if (g == null)
						sendToBukkit(packetUUID, new DataBuffer().writeInt(-1).writeString("Group not found"), player.getServer().getInfo()); //Response (Group not found) [UUID (packet)] [INT -1] [STRING reson]
					else {
						DataBuffer out = new DataBuffer();
						ArrayList<Permission> permissions = g.getPermissionsDeep();
						out.writeInt(permissions.size());

						for (Permission perm : permissions) {
							if (perm == null || perm.getGroup() == null || perm.getPermission() == null) {
								System.err.println("Permissiongroupo for: " + perm + " is null!");
								out.writeString("anUndefinedPermissionThankAnNullPointerException").writeByte(GroupTyp.ALL.ordinal());
								continue;
							}
							out.writeString(perm.getPermission());
							out.writeByte(perm.getGroup().ordinal());
						}
						out.writeString(g.getPrefix());
						out.writeInt(g.getImportance());
						System.out.print("Requesting group permissions for group " + group);
						sendToBukkit(packetUUID, out, player.getServer().getInfo()); //Response (Permissions) [UUID (packet)] [INT perms-Length] [STRING[] perms] [STRING name]
					}
				} else if (action == 2) {
					Integer target = buffer.readInt();
					PermissionPlayer p = getPlayer(target);
					if (p == null) {
						sendToBukkit(packetUUID, new DataBuffer().writeInt(-1).writeString("Player not found"), player.getServer().getInfo()); //Response (Player not found) [UUID (packet)] [INT -1] [STRING reson]
						return;
					} else {
						p.addPermission(buffer.readString(), GroupTyp.values()[buffer.readByte()]);
					}
					sendToBukkit(packetUUID, new DataBuffer().writeInt(1), player.getServer().getInfo());
				} else if (action == 3) {//setgroup <long[2] UUID> <string Group> <byte Grouptype>
					Integer target = buffer.readInt();
					PermissionPlayer p = getPlayer(target);
					if (true) {
						sendToBukkit(packetUUID, new DataBuffer().writeInt(-1).writeString("Error 001"), player.getServer().getInfo()); //Response (Player not found) [UUID (packet)] [INT -1] [STRING reson]
						return;
					}
					if (p == null)
						sendToBukkit(packetUUID, new DataBuffer().writeInt(-1).writeString("Player not found"), player.getServer().getInfo()); //Response (Player not found) [UUID (packet)] [INT -1] [STRING reson]
					else {
						for (Group g : p.getGroups())
							p.removeGroup(g.getName());
						p.addGroup(buffer.readString());
					}
					sendToBukkit(packetUUID, new DataBuffer().writeInt(1), player.getServer().getInfo());
				}
			} catch (Exception ex) {
				System.out.print(e.getSender().getAddress());
				ex.printStackTrace();
			}
		}
	}

	public void sendToBukkit(UUID uuid, DataBuffer data, ServerInfo server) {
		DataBuffer buffer = new DataBuffer();
		buffer.writeUUID(uuid);

		byte[] cbuffer = new byte[data.writerIndex()];
		System.arraycopy(data.array(), 0, cbuffer, 0, data.writerIndex());
		buffer.writeBytes(cbuffer);
		data.release();

		byte[] bbuffer = new byte[buffer.writerIndex()];
		System.arraycopy(buffer.array(), 0, bbuffer, 0, buffer.writerIndex());
		boolean success = server.sendData("permission", bbuffer, false); //Dont Queue
		if (!success)
			System.out.println("Cant send a plugin message...");
		buffer.release();
	}

	private static void printGroup(Group g, String premif) {

	}
}
