package dev.wolveringer.bs.commands;

import java.util.UUID;

import lombok.Getter;
import me.kingingo.kBungeeCord.kBungeeCord;
import me.kingingo.kBungeeCord.Language.Language;
import me.kingingo.kBungeeCord.Permission.GroupTyp;
import me.kingingo.kBungeeCord.Permission.PermissionType;
import me.kingingo.kBungeeCord.UUIDCatcher.UUIDCatcher;
import me.kingingo.kBungeeCord.Utils.UtilPlayer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

public class CommandPermission extends Command implements Listener {

	@Getter
	private kBungeeCord instance;
	
	public CommandPermission(String name,kBungeeCord instance) {
		super(name);
		this.instance=instance;
		BungeeCord.getInstance().getPluginManager().registerListener(instance, this);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
			if(args.length==0){
				if(getInstance().getPermManager().hasPermission(p, PermissionType.PERMISSION,true)){
					p.sendMessage(Language.getText(p, "PREFIX")+"/perm user [Name:GroupTyp] setgroup [Group]");
					p.sendMessage(Language.getText(p, "PREFIX")+"/perm user [Name:GroupTyp] add [Permission]");
					p.sendMessage(Language.getText(p, "PREFIX")+"/perm user [Name] remove [Permission]");
					p.sendMessage(Language.getText(p, "PREFIX")+"/perm group [Group] prefix 'Prefix'");
					p.sendMessage(Language.getText(p, "PREFIX")+"/perm group [Group:GroupTyp] add [Permission]");
					p.sendMessage(Language.getText(p, "PREFIX")+"/perm group [Group] remove [Permission]");
				}else if(getInstance().getPermManager().hasPermission(p, PermissionType.SETGROUP,false)){
					p.sendMessage(Language.getText(p, "PREFIX")+"/perm user [Name] setgroup [Group]");
				}
			}else if(args[0].equalsIgnoreCase("group")&&getInstance().getPermManager().hasPermission(p, PermissionType.PERMISSION,true)){
					if(args.length>=2){
							if(args.length==2){
								p.sendMessage(Language.getText(p, "PREFIX")+"�cGroup "+args[1]+":");
								p.sendMessage(Language.getText(p, "PREFIX")+"Permissions: ");
								for(String perm : getInstance().getPermManager().getGroups().get(args[1].toLowerCase()).getPerms()){
									p.sendMessage("�7"+perm);
								}
							}else if(args[2].equalsIgnoreCase("prefix")){
								String prefix = args[3];
								
								StringBuilder sb = new StringBuilder();
								for (int i = 3; i < args.length; i++) {
									sb.append(args[i]);
									sb.append(" ");
								}
								sb.setLength(sb.length() - 1);
								prefix = sb.toString();
								
								if(prefix.substring(0, 1).equalsIgnoreCase("'")&&prefix.substring(prefix.length()-1, prefix.length()).equalsIgnoreCase("'")){
									prefix=prefix.replaceAll("'", "");
									prefix=prefix.replaceAll("&","�");
									
									if(getInstance().getPermManager().getGroups().containsKey(args[1].toLowerCase())){
										getInstance().getPermManager().setGroupPrefix(args[1], prefix);
										p.sendMessage(Language.getText(p, "PREFIX")+"Die Prefix �e"+prefix+"�a wurde der Gruppe �e"+args[1]+"�a gesetzt.");
									}else{
										p.sendMessage(Language.getText(p, "PREFIX")+"Diese Gruppe wurde nicht gefunden!");
									}
								}else{
									p.sendMessage(Language.getText(p, "PREFIX")+"Bitte das Format einhalten 'Prefix'");
								}
							}else if(args[2].equalsIgnoreCase("add")){

								if(!args[1].contains(":")){
									p.sendMessage(Language.getText(p, "PREFIX")+"�c[Group:GroupTyp]");
									return;
								}
								String group = args[1].split(":")[0];
								GroupTyp typ = GroupTyp.get(args[1].split(":")[1]);
								
								if(typ==null){
									p.sendMessage(Language.getText(p, "PREFIX")+"�c[Group:GroupTyp]");
									return;
								}
								
								getInstance().getPermManager().addGroupPermission(group, typ,args[3]);
								p.sendMessage(Language.getText(p, "PREFIX")+"�aDie Permission �e"+args[3]+"�a wurde der Gruppe �e"+args[1]+"�a hinzugef�gt.");
							}else if(args[2].equalsIgnoreCase("remove")){
								if(!args[1].contains(":")){
									p.sendMessage(Language.getText(p, "PREFIX")+"�c[Group:GroupTyp]");
									return;
								}
								String group = args[1].split(":")[0];
								GroupTyp typ = GroupTyp.get(args[1].split(":")[1]);
								
								if(typ==null){
									p.sendMessage(Language.getText(p, "PREFIX")+"�c[Group:GroupTyp]");
									return;
								}
								
								getInstance().getPermManager().delGroupPermission(group, typ,args[3]);
								p.sendMessage(Language.getText(p, "PREFIX")+"�aDie Permission �e"+args[3]+"�a wurde von der Gruppe �e"+args[1]+"�a entfernt.");
							}
				}
			}else if(args[0].equalsIgnoreCase("user")){
				if(args.length>=2){
					if(args.length==2){
						if(getInstance().getPermManager().hasPermission(p, PermissionType.PERMISSION,true)||getInstance().getPermManager().hasPermission(p, PermissionType.SETGROUP,true)){
							UUID uuid = UUIDCatcher.getUUID(args[1]);
							if(uuid!=null){
								System.out.println(args[1]+" UUID: "+uuid);
								p.sendMessage(Language.getText(p, "PREFIX")+"�cUser "+args[1]+":");
								p.sendMessage(Language.getText(p, "PREFIX")+"�cGroup: "+getInstance().getPermManager().getGroup(uuid));
							}
						}
					}else if(args[2].equalsIgnoreCase("sg")||args[2].equalsIgnoreCase("setgroup")){
						if(getInstance().getPermManager().hasPermission(p, PermissionType.PERMISSION,true)||getInstance().getPermManager().hasPermission(p, PermissionType.SETGROUP,true)){

								if(!args[1].contains(":")){
									p.sendMessage(Language.getText(p, "PREFIX")+"�c[User:GroupTyp]");
									return;
								}
								String user = args[1].split(":")[0];
								GroupTyp typ = GroupTyp.get(args[1].split(":")[1]);
							
								if(typ==null){
									p.sendMessage(Language.getText(p, "PREFIX")+"�c[User:GroupTyp]");
									return;
								}
								
								if(!getInstance().getPermManager().hasPermission(p, PermissionType.PERMISSION,true) && (args[3].equalsIgnoreCase("owner")||args[3].equalsIgnoreCase("admin")) ){
									p.sendMessage(Language.getText(p, "PREFIX")+"�cDu kannst diesen Rang nicht vergeben!");
									return;
								}
								if(UtilPlayer.isOnline(user)){
									getInstance().getPermManager().setGroup(BungeeCord.getInstance().getPlayer(user), args[3],typ);
								}else{
									UUID uuid = UUIDCatcher.getUUID(user);
									getInstance().getPermManager().setGroup(uuid, args[3],typ);
								}
								p.sendMessage(Language.getText(p, "PREFIX")+"�aDie Gruppe von den User �e"+args[1]+"�a wurde zu �e"+args[3]+"�a ge�ndert");
						}
					}else if(args[2].equalsIgnoreCase("add")){
						if(getInstance().getPermManager().hasPermission(p, PermissionType.PERMISSION,true)){
							
							if(!args[1].contains(":")){
								p.sendMessage(Language.getText(p, "PREFIX")+"�c[User:GroupTyp]");
								return;
							}
							String user = args[1].split(":")[0];
							GroupTyp typ = GroupTyp.get(args[1].split(":")[1]);
						
							if(typ==null){
								p.sendMessage(Language.getText(p, "PREFIX")+"�c[User:GroupTyp]");
								return;
							}
							
							String perm = args[3];
							UUID uuid = UUIDCatcher.getUUID(user);
							
							getInstance().getPermManager().addUserPermission(uuid, typ, perm);
							p.sendMessage(Language.getText(p, "PREFIX")+"�aDie Permission �e"+args[3]+"�a wurde von den User �e"+user+"�a hinzugef�gt.");
						}
					}else if(args[2].equalsIgnoreCase("remove")){
						if(getInstance().getPermManager().hasPermission(p, PermissionType.PERMISSION,true)){
							if(!args[1].contains(":")){
								p.sendMessage(Language.getText(p, "PREFIX")+"�c[User:GroupTyp]");
								return;
							}
							String user = args[1].split(":")[0];
							GroupTyp typ = GroupTyp.get(args[1].split(":")[1]);
						
							if(typ==null){
								p.sendMessage(Language.getText(p, "PREFIX")+"�c[User:GroupTyp]");
								return;
							}
							
							String perm = args[3];
							UUID uuid = UUIDCatcher.getUUID(user);
							
							getInstance().getPermManager().delUserPermission(uuid, typ, perm);
							p.sendMessage(Language.getText(p, "PREFIX")+"�aDie Permission �e"+args[3]+"�a wurde von den User �e"+args[1]+"�a entfernt.");
						}
					}
				}
			}
	}
	
	public boolean GroupExist(String group){
		return getInstance().getPermManager().getGroups().containsKey(group.toLowerCase());
	}

}
