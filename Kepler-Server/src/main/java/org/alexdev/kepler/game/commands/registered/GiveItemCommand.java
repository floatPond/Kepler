package org.alexdev.kepler.game.commands.registered;

import org.alexdev.kepler.dao.mysql.BadgeDao;
import org.alexdev.kepler.dao.mysql.ItemDao;
import org.alexdev.kepler.game.commands.Command;
import org.alexdev.kepler.game.entity.Entity;
import org.alexdev.kepler.game.entity.EntityType;
import org.alexdev.kepler.game.fuserights.Fuseright;
import org.alexdev.kepler.game.item.Item;
import org.alexdev.kepler.game.item.ItemManager;
import org.alexdev.kepler.game.item.interactors.InteractionType;
import org.alexdev.kepler.game.player.Player;
import org.alexdev.kepler.game.player.PlayerDetails;
import org.alexdev.kepler.game.player.PlayerManager;
import org.alexdev.kepler.game.room.Room;
import org.alexdev.kepler.game.room.RoomUserStatus;
import org.alexdev.kepler.game.room.enums.StatusType;
import org.alexdev.kepler.game.texts.TextsManager;
import org.alexdev.kepler.messages.outgoing.rooms.badges.AVAILABLE_BADGES;
import org.alexdev.kepler.messages.outgoing.rooms.badges.USER_BADGE;
import org.alexdev.kepler.messages.outgoing.rooms.user.CHAT_MESSAGE;
import org.alexdev.kepler.messages.outgoing.rooms.user.FIGURE_CHANGE;
import org.alexdev.kepler.messages.outgoing.user.ALERT;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

import static java.lang.Integer.parseInt;

public class GiveItemCommand extends Command {

    @Override
    public void addPermissions() {
        this.permissions.add(Fuseright.ADMINISTRATOR_ACCESS);
    }
    @Override
    public void addArguments() {
        this.arguments.add("user");
        this.arguments.add("item");
    }

    @Override
    public void handleCommand(Entity entity, String message, String[] args) {

        Player player = (Player) entity;

        //Not in a room
        if (player.getRoomUser().getRoom() == null) {
            return;
        }

        Player targetUser = PlayerManager.getInstance().getPlayerByName(args[0]);

        if (targetUser == null) {
            player.send(new CHAT_MESSAGE(CHAT_MESSAGE.ChatMessageType.WHISPER, player.getRoomUser().getInstanceId(), "Could not find user: " + args[0]));
            return;
        }

        if (args.length == 1) {
            player.send(new CHAT_MESSAGE(CHAT_MESSAGE.ChatMessageType.WHISPER, player.getRoomUser().getInstanceId(), "item code not provided"));
            return;
        }

        String sItemId = args[1];

        // item should be numeric
        if (!StringUtils.isNumeric(sItemId)) {
            player.send(new CHAT_MESSAGE(CHAT_MESSAGE.ChatMessageType.WHISPER, player.getRoomUser().getInstanceId(), "Item code provided not numeric."));
            return;
        }

        int iItem = parseInt(args[1]);

        PlayerDetails targetDetails = targetUser.getDetails();

        Item item = new Item();
        item.setOwnerId(targetDetails.getId());
        item.setDefinitionId(iItem);

        try {
            ItemDao.newItem(item);
            player.getInventory().addItem(item);
            player.getInventory().getView("new");
            player.send(new CHAT_MESSAGE(CHAT_MESSAGE.ChatMessageType.WHISPER, player.getRoomUser().getInstanceId(), ItemDao.getItemName(iItem) + " added to user " + targetDetails.getName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getDescription() {
        return "Add item to user's inventory";
    }
}
