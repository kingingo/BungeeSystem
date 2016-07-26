package dev.wolveringer.nick;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.wolveringer.BungeeUtil.PacketHandleEvent;
import dev.wolveringer.BungeeUtil.PacketHandler;
import dev.wolveringer.BungeeUtil.gameprofile.PlayerInfoData;
import dev.wolveringer.BungeeUtil.packets.Packet;
import dev.wolveringer.BungeeUtil.packets.PacketPlayInChat;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutChat;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutPlayerInfo;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutScoreboardScore;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutScoreboardTeam;
import dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle;
import dev.wolveringer.bs.Main;
import dev.wolveringer.chat.ChatSerializer;
import dev.wolveringer.chat.IChatBaseComponent;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.permission.PermissionManager;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class NickHandler implements PacketHandler<Packet>{
	private static final Pattern PATTERN = Pattern.compile("\\{player_(?=(([a-zA-Z0-9]){3,30})\\})");
	
	@Getter
	@Setter
	private static NickHandler instance;
	
	@Override
	public void handle(PacketHandleEvent<Packet> e) {
		if (e.getPacket() instanceof PacketPlayOutChat) {
			PacketPlayOutChat chat = (PacketPlayOutChat) e.getPacket();
			if(!PATTERN.matcher(ChatSerializer.toJSONString(chat.getMessage())).find()){ //Check if message have a player variable (messages will send too while datenserver is disconnected and then i cant check the permission)
				return;
			}
			else if(!Main.getDatenServer().isActive()){
				chat.setMessage(ChatSerializer.fromMessage("§cNo message."));
				return;
			}
			chat.setMessage(replaceNames(chat.getMessage(), PermissionManager.getManager().hasPermission(e.getPlayer(), "chat.nick.see")));
		}
		else if(e.getPacket() instanceof PacketPlayInChat){
			PacketPlayInChat chat = (PacketPlayInChat) e.getPacket();
			if(PATTERN.matcher(chat.getMessage()).find())
				e.setCancelled(!PermissionManager.getManager().hasPermission(e.getPlayer(), "chat.syntax.nick", true));
		}
		else if(e.getPacket() instanceof PacketPlayOutPlayerInfo){
			//if(((PacketPlayOutPlayerInfo)e.getPacket()).getAction() == EnumPlayerInfoAction.ADD_PLAYER || ((PacketPlayOutPlayerInfo)e.getPacket()).getAction() == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME)
			for(PlayerInfoData i : ((PacketPlayOutPlayerInfo)e.getPacket()).getData()){
				if(i.getName() != null)
					i.setName(replaceNames(i.getName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "tab.nick.see")));
				if(i.getGameprofile() != null && i.getGameprofile().getName() != null){
					i.getGameprofile().setName(replaceNames(i.getGameprofile().getName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "nametag.nick.see")));
				}
			}
		}
		else if(e.getPacket() instanceof PacketPlayOutScoreboardTeam){
			PacketPlayOutScoreboardTeam team = (PacketPlayOutScoreboardTeam) e.getPacket();
			if(team.getPlayers() != null)
				for(int i = 0;i<team.getPlayers().length;i++){
					team.getPlayers()[i] = replaceNames(team.getPlayers()[i], PermissionManager.getManager().hasPermission(e.getPlayer(), "nametag.nick.see"));
				}
		}
		else if(e.getPacket() instanceof PacketPlayOutScoreboardScore){
			PacketPlayOutScoreboardScore score = (PacketPlayOutScoreboardScore) e.getPacket();
			if(score.getScoreName() != null){
				score.setObjektiveName(replaceNames(score.getObjektiveName(), PermissionManager.getManager().hasPermission(e.getPlayer(), "scoreboard.nick.see")));
			}
		}
		else if(e.getPacket() instanceof PacketPlayOutTitle){
			PacketPlayOutTitle title = (PacketPlayOutTitle) e.getPacket();
			if(title.getAction() == dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle.Action.SET_SUBTITLE || title.getAction() == dev.wolveringer.BungeeUtil.packets.PacketPlayOutTitle.Action.SET_TITLE){
				title.setTitle(replaceNames(title.getTitle(), PermissionManager.getManager().hasPermission(e.getPlayer(), "title.nick.see")));
			}
		}
	}
	
	private static String replaceNames(String str,boolean info){
		Matcher m = PATTERN.matcher(str);
		while (m.find()) {
			LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(m.group(1));
			str = str.replace("{player_"+m.group(1)+"}", player.hasNickname() ? info ? player.getName() : player.getNickname() : player.getName());
		}
		return str;
	}

	private static IChatBaseComponent replaceNames(IChatBaseComponent textComponent, boolean info) {
		List<BaseComponent> out = new ArrayList<>();
		BaseComponent[] comps = ComponentSerializer.parse(ChatSerializer.toJSONString(textComponent));
		for(BaseComponent c : comps)
			for(BaseComponent c1 : replaceNames(c, info))
				out.add(c1);
		return ChatSerializer.fromJSON(ComponentSerializer.toString(out.toArray(new BaseComponent[0])));
	}
	
	private static List<BaseComponent> replaceNames(BaseComponent bcomp, boolean info){
		ArrayList<BaseComponent> out = new ArrayList<>();
		if(bcomp instanceof TextComponent){
			TextComponent comp = (TextComponent) bcomp;
			
			String text = comp.getText();
			Matcher m = PATTERN.matcher(text);
			while (m.find()) {
				TextComponent add = new TextComponent(comp);//Copy Style
				add.setText(text.substring(0, m.start()));
				out.add(add);
				
				LoadedPlayer player = Main.getDatenServer().getClient().getPlayerAndLoad(m.group(1));
				
				TextComponent nickname = new TextComponent(comp); //Copy Style
				nickname.setText(!player.hasNickname() ? m.group(1) : info ? player.getName() : player.getNickname());
				if(player.getNickname() != null && info){
					nickname.setItalic(true);
					nickname.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("§aDer Spieler §e"+player.getName()+" §aist genickt als §b"+player.getNickname()).create()));
				}
				out.add(nickname);
				
				comp.setText(text.substring(m.start()+("{player_"+m.group(1)+"}").length()));
			}
			if(comp.getText().length() > 0)
				out.add(comp);
		}
		if(bcomp.getExtra() != null)
			for(BaseComponent s : bcomp.getExtra())
				out.addAll(replaceNames(s, info));
		return out;
	}
}
