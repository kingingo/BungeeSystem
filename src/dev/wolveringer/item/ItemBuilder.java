package dev.wolveringer.item;

import java.util.ArrayList;

import dev.wolveringer.BungeeUtil.Material;
import dev.wolveringer.BungeeUtil.item.Item;
import dev.wolveringer.BungeeUtil.item.ItemStack;
import dev.wolveringer.BungeeUtil.item.ItemStack.Click;

public class ItemBuilder {
	@FunctionalInterface
	public static interface ItemClickListener {
		public void click(Click c);
	}
	

	public static ItemBuilder create() {
		return new ItemBuilder();
	}
	
	public static ItemBuilder create(Material m) {
		return create(m.getId());
	}

	public static ItemBuilder create(int id) {
		return new ItemBuilder(id);
	}

	private int id;
	private int sid = 0;
	private int amouth = 1;
	private String name;
	private ArrayList<String> lore = new ArrayList<>();
	private boolean glow;
	private ItemClickListener listener;
	
	public ItemBuilder(int id) {
		this.id = id;
	}

	public ItemBuilder() {}
	
	public ItemBuilder name(String name) {
		this.name = name;
		return this;
	}

	public ItemBuilder lore(String lore) {
		this.lore.add(lore);
		return this;
	}

	public ItemBuilder glow() {
		this.glow = true;
		return this;
	}

	public ItemBuilder glow(boolean flag) {
		this.glow = flag;
		return this;
	}

	public ItemBuilder durbility(int short_) {
		this.sid = short_;
		return this;
	}

	public ItemBuilder amouth(int n) {
		this.amouth = n;
		return this;
	}

	public ItemBuilder listener(ItemClickListener run){
		this.listener = run;
		return this;
	}
	
	public ItemBuilder id(int id){
		this.id = id;
		return this;
	}
	
	public ItemBuilder material(Material m){
		this.id = m.getId();
		return this;
	}
	
	public Item build() {
		Item i;
		if(listener == null){
			i = new Item(id, amouth, (short) sid);
		}
		else
			i = new ItemStack(id, amouth, (short) sid) {
				@Override
				public void click(Click c) {
					listener.click(c);
				}
			};
		if (name != null)
			i.getItemMeta().setDisplayName(name);
		if (!lore.isEmpty())
			i.getItemMeta().setLore(lore);
		if (glow)
			i.getItemMeta().setGlow(true);
		return i;
	}
}