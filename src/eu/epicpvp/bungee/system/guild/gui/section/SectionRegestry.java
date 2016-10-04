package eu.epicpvp.bungee.system.guild.gui.section;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import eu.epicpvp.datenclient.gilde.GildSection;
import eu.epicpvp.datenserver.definitions.gilde.GildeType;
import lombok.Getter;

public class SectionRegestry {
	@Getter
	private static final SectionRegestry instance;

	static {
		instance = new SectionRegestry();
		instance.registerSection(GildeType.ARCADE, GuiGildeSection.class);
		instance.registerSection(GildeType.PVP, GuiGildeSection.class);
		instance.registerSection(GildeType.SKY, GuiGildeSection.class);
		instance.registerSection(GildeType.VERSUS, GuiGildeSection.class);
		instance.registerSection(GildeType.WARZ, GuiGildeSection.class);
	}

	private HashMap<GildeType, Class<? extends GuiGildeSection>> classes = new HashMap<>();

	public void registerSection(GildeType type,Class<? extends GuiGildeSection> clazz){
		classes.put(type, clazz);
	}
	public GuiGildeSection createGildeSection(GildeType type,GildSection section){
		Class<? extends GuiGildeSection> clazz = classes.get(type);
		if(clazz == null){
			System.out.println("Cant find class: "+type);
			return null;
		}
		try {
			clazz.getConstructor(new Class[]{GildSection.class}).setAccessible(true);
			return clazz.getConstructor(new Class[]{GildSection.class}).newInstance(new Object[]{section});
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	public GuiGildeSection createGildeSection(GildSection section){
		return createGildeSection(section.getType() ,section);
	}
}
