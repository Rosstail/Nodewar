package fr.rosstail.nodewar.permissionmannager.types;

import com.palmergames.bukkit.towny.TownyAPI;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.permissionmannager.NwIPermissionManagerHandler;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.Set;
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
    public void setPlayerGroup(String groupName, Player player) {
        UserManager userManager = luckPerms.getUserManager();

        CompletableFuture<User> userFuture = userManager.loadUser(player.getUniqueId());

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
    public void removePlayerGroup(Player player, String exceptionTeamName) {
        UserManager userManager = luckPerms.getUserManager();

        CompletableFuture<User> userFuture = userManager.loadUser(player.getUniqueId());

        userFuture.thenAccept(user -> {
            Set<Node> groupsToRemove = user.getNodes().stream()
                    .filter(node -> node instanceof InheritanceNode)
                    .map(node -> (InheritanceNode) node)
                    .filter(node -> node.getGroupName().startsWith("nw_") && (exceptionTeamName == null || !node.getGroupName().endsWith(exceptionTeamName)))
                    .collect(Collectors.toSet());

            groupsToRemove.forEach(user.data()::remove);

            userManager.saveUser(user);
        }).exceptionally(exception -> {
            exception.printStackTrace();
            return null;
        });
    }
}
