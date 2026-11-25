package run.anx.bridge;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** BungeeCord and Waterfall adapter. */
public final class BungeeBridge extends Plugin implements Platform, Listener {
    private final Map<String, Boolean> backendUp = new ConcurrentHashMap<>();
    private Agent agent;

    @Override
    public void onEnable() {
        agent = new Agent(this);
        agent.start();
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new Command("anx", "anx.admin") {
            @Override public void execute(CommandSender sender, String[] args) {
                Commands.run(agent, args, message -> sender.sendMessage(new TextComponent(message)));
            }
        });
    }

    @Override public void onDisable() { if (agent != null) agent.stop(); }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        if (agent == null || event.getResponse() == null) return;
        String suffix = agent.motdSuffix();
        if (suffix.isEmpty()) return;
        BaseComponent description = event.getResponse().getDescriptionComponent();
        event.getResponse().setDescriptionComponent(new TextComponent(
                description == null ? new TextComponent("") : description, new TextComponent(suffix)));
    }

    @Override public String id() { return "bungeecord"; }
    @Override public String software() { return getProxy().getName(); }
    @Override public String softwareVersion() { return getProxy().getVersion(); }
    @Override public Path dataDir() { return getDataFolder().toPath(); }
    @Override public void info(String message) { getLogger().info(message); }
    @Override public void warn(String message) { getLogger().warning(message); }
    @Override public void dispatchConsole(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
    }

    @Override
    public CompletableFuture<Stats> stats() {
        List<UUID> ids = new ArrayList<>();
        for (ProxiedPlayer player : getProxy().getPlayers()) ids.add(player.getUniqueId());
        Map<String, Integer> players = new LinkedHashMap<>();
        List<CompletableFuture<?>> pings = new ArrayList<>();
        for (ServerInfo server : getProxy().getServers().values()) {
            players.put(server.getName(), server.getPlayers().size());
            CompletableFuture<Boolean> pingFuture = new CompletableFuture<>();
            server.ping((ping, error) -> pingFuture.complete(error == null && ping != null));
            pings.add(pingFuture.completeOnTimeout(false, 3, TimeUnit.SECONDS)
                    .thenAccept(up -> backendUp.put(server.getName(), up)));
        }
        Integer maximum = getProxy().getConfig().getPlayerLimit() > 0 ? getProxy().getConfig().getPlayerLimit() : null;
        return CompletableFuture.allOf(pings.toArray(CompletableFuture[]::new)).handle((ignored, error) ->
                new Stats(getProxy().getOnlineCount(), maximum, ids, players, Map.copyOf(backendUp),
                        List.of(software() + " " + softwareVersion())));
    }
}
