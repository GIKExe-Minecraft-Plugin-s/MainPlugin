package ru.gikexe.mainplugin;


import com.google.common.base.Charsets;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;

public class Config extends HashMap<String, Object> {
	private static final Yaml yaml = new Yaml();
	private final File file;

	public Config(JavaPlugin plugin, String filename) {
		file = Paths.get(plugin.getDataFolder().getPath(),filename).toFile();
		if (!file.exists()) plugin.saveResource(filename,false);
		reload();
	}

	public void reload() {
		try {
			clear();
			putAll(yaml.load(new FileInputStream(file)));
		} catch (FileNotFoundException error) {
			error.printStackTrace();
		}
	}

	public void save() {
		try {
			yaml.dump(this,new FileWriter(file, Charsets.UTF_8));
		} catch (IOException error) {
			error.printStackTrace();
		}
	}
}