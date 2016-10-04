package eu.epicpvp.bungee.system.item;

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

	public static ItemBuilder create(Item handle) {
		return new ItemBuilder(handle);
	}

	private int id;
	private int sid = -1;
	private int amouth = -1;
	private String name;
	private ArrayList<String> lore = new ArrayList<>();
	private boolean glow;
	private ItemClickListener listener;
	private Item handle;

	public ItemBuilder(int id) {
		this.id = id;
	}

	public ItemBuilder(Item handle) {
		this.handle = new Item(handle); //Copy
	}

	public ItemBuilder() {
	}

	public ItemBuilder name(String name) {
		this.name = name;
		return this;
	}

	public ItemBuilder lore(String lore) {
		this.lore.add(lore);
		return this;
	}

	public ItemBuilder lore(String[] lore) {
		this.lore.clear();
		if(lore != null)
			for(String s : lore)
				this.lore.add(s);
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

	public ItemBuilder listener(ItemClickListener run) {
		this.listener = run;
		return this;
	}

	public ItemBuilder listener(Runnable run) {
		this.listener = new ItemClickListener() {
			@Override
			public void click(Click c) {
				run.run();
			}
		};
		return this;
	}

	public ItemBuilder id(int id) {
		this.id = id;
		return this;
	}

	public ItemBuilder material(Material m) {
		this.id = m.getId();
		return this;
	}

	public Item build() {
		Item i;
		if (handle != null) {
			if (id != 0)
				handle.setTypeId(id);
			if (sid != -1)
				handle.setDurability((short) sid);
			if(amouth != -1)
				handle.setAmount(amouth);
			i = handle;
		} else
			i = new Item(id, amouth == -1 ? 1 : amouth, (short) (sid == -1 ? 0 : sid));
		if (listener != null) {
			i = new ItemStack(i) {
				@Override
				public void click(Click c) {
					listener.click(c);
				}
			};
		}
		if (name != null)
			i.getItemMeta().setDisplayName(name);
		if (!lore.isEmpty())
			i.getItemMeta().setLore(lore);
		if (glow)
			i.getItemMeta().setGlow(true);
		return i;
	}
}
