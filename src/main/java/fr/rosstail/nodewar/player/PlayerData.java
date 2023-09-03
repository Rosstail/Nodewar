package fr.rosstail.nodewar.player;

import org.bukkit.entity.Player;

public class PlayerData extends PlayerModel {


    public PlayerData(Player player) {
        super(player);
    }

    public PlayerData(String uuid, String username) {
        super(uuid, username);
    }

    public PlayerData(PlayerModel playerModel) {
        super(playerModel);
    }
}
