package ru.l1ratch.easylocalresoursepack;

import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class EasyLocalResoursepack extends JavaPlugin implements Listener {

    private HttpServer httpServer;
    private int serverPort;
    private String resourcePackFile;

    @Override
    public void onEnable() {
        // Загружаем конфигурацию
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        serverPort = config.getInt("server-port");
        resourcePackFile = config.getString("resource-pack-file");

        try {
            // Создаем и запускаем HTTP-сервер
            httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
            httpServer.createContext("/resourcepack", exchange -> {
                File file = new File(getDataFolder(), resourcePackFile);
                if (file.exists()) {
                    byte[] bytes = new byte[(int) file.length()];
                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(bytes);
                    }
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } else {
                    exchange.sendResponseHeaders(404, -1); // файл не найден
                }
            });
            httpServer.setExecutor(null);
            httpServer.start();
            getLogger().info("HTTP сервер запущен на порту " + serverPort);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Регистрация события
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("EasyLocalResoursepack включен!");
    }

    @Override
    public void onDisable() {
        if (httpServer != null) {
            httpServer.stop(0);
            getLogger().info("HTTP сервер остановлен");
        }
        getLogger().info("EasyLocalResoursepack выключен!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Выдача ресурс-пака
        String resourcePackUrl = "http://localhost:" + serverPort + "/resourcepack";
        event.getPlayer().setResourcePack(resourcePackUrl);
        event.getPlayer().sendMessage(ChatColor.GREEN + "Вам выдан ресурспак!");
    }
}

