package fr.rosstail.nodewar.permission.types;

import fr.rosstail.nodewar.permission.NwIPermissionManagerHandler;
import fr.rosstail.nodewar.player.PlayerDataManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class NwLuckPermsHandler implements NwIPermissionManagerHandler {
    private final LuckPerms luckPerms;

    public NwLuckPermsHandler() {
        RegisteredServiceProvider<LuckPerms> luckPermsProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        this.luckPerms = luckPermsProvider.getProvider();
    }

    @Override
    public void createGroup(String groupName) {
        luckPerms.getGroupManager().createAndLoadGroup(groupName);
    }

    @Override
    public void deleteGroup(String groupName) {
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        if (group != null) {
            luckPerms.getGroupManager().deleteGroup(group);
        }
    }

    @Override
    public void setPlayerGroup(String groupName, String playerName, UUID playerUUID) {
        UserManager userManager = luckPerms.getUserManager();
        UUID uuid = playerUUID != null ? playerUUID :
                UUID.fromString(PlayerDataManager.getPlayerUUIDFromName(playerName));

        CompletableFuture<User> userFuture = userManager.loadUser(uuid);

        userFuture.thenAccept(user -> {
            Node groupNode = InheritanceNode.builder(groupName).build();
            user.data().add(groupNode);
            userManager.saveUser(user);
        }).exceptionally(exception -> {
            exception.printStackTrace();
            return null;
        });
    }

    @Override
    public void removePlayerGroup(String playerName, UUID playerUUID, String groupExceptionName) {
        UserManager userManager = luckPerms.getUserManager();
        UUID uuid = playerUUID != null ? playerUUID :
                UUID.fromString(PlayerDataManager.getPlayerUUIDFromName(playerName));

        CompletableFuture<User> userFuture = userManager.loadUser(uuid);

        userFuture.thenAccept(user -> {
            Set<Node> groupsToRemove = user.getNodes().stream()
                    .filter(node -> node instanceof InheritanceNode)
                    .map(node -> (InheritanceNode) node)
                    .filter(node -> node.getGroupName().startsWith("nw_") && (groupExceptionName == null || !node.getGroupName().endsWith(groupExceptionName)))
                    .collect(Collectors.toSet());

            groupsToRemove.forEach(user.data()::remove);

            userManager.saveUser(user);
        }).exceptionally(exception -> {
            exception.printStackTrace();
            return null;
        });
    }
}
