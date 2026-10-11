package plugin.siren.Events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import plugin.siren.Marriage;
import plugin.siren.Utils.Registry.MarriageManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The couple channel: while a player has it on, everything they type goes to
 * their partner instead of the whole server.
 *
 * <h2>Redirected, not cancelled</h2>
 *
 * <p>This narrows {@link PlayerChatEvent#setTargets} to the two of them rather
 * than cancelling the event and sending its own messages. That matters for
 * co-existence: cancelling would throw away whatever {@link
 * PlayerChatEvent#getFormatter} another mod had installed, so a Cultivation title
 * or any other chat decoration would silently vanish the moment a couple turned
 * their channel on. Narrowing the recipient list leaves every other mod's work
 * intact and simply delivers the line to fewer people.</p>
 *
 * <p>The formatter IS replaced, but only to wrap the existing one - the line
 * still needs to look different from public chat, or a player cannot tell at a
 * glance who just heard them.</p>
 *
 * <h2>Threading</h2>
 *
 * <p>{@code PlayerChatEvent} is an {@code IAsyncEvent} with no guarantee it fires
 * on the sender's own world thread, so nothing here touches a component. Whether
 * the channel is on is read from {@link MarriageManager}'s UUID-keyed cache
 * (written by world threads, explicitly safe to read from anywhere) rather than
 * from the sender's live settings component - the same rule Cultivation's chat
 * handler follows.</p>
 */
public final class PlayerChatEventM {
    private PlayerChatEventM(){}

    public static void onPlayerChatEvent(PlayerChatEvent event){
        // Registered at LATE (see Marriage.setup), so a mod that muted or cancelled the
        // line has already done so - leave it be, and do not count it as a love note.
        if(event.isCancelled()){
            return;
        }
        PlayerRef sender = event.getSender();
        if(sender == null || !sender.isValid()){
            return;
        }
        if(!Marriage.getConfig().get().ifCoupleChannelEnabled()){
            return;
        }

        UUID senderUuid = sender.getUuid();
        if(!MarriageManager.isCoupleChannelOn(senderUuid)){
            return;
        }

        UUID partnerUuid = MarriageManager.getPartnerUuid(senderUuid);
        if(partnerUuid == null){
            // Divorced with the channel still latched on. Say so once and turn it
            // off, rather than letting them keep typing into a channel with nobody
            // at the other end.
            MarriageManager.setCoupleChannelOn(senderUuid, false);
            sender.sendMessage(Message.translation("server.marriage.channel.unmarried"));
            return;
        }

        PlayerRef partner = Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            // Their partner is not here to hear it. Swallowing the line into the
            // void would look exactly like the server eating their message, so it
            // is stopped with an explanation instead.
            event.setCancelled(true);
            sender.sendMessage(Message.translation("server.marriage.channel.partnerOffline")
                    .param("partnerUsername", MarriageManager.getPartnerName(senderUuid)));
            return;
        }

        List<PlayerRef> couple = new ArrayList<>(2);
        couple.add(sender);
        couple.add(partner);
        event.setTargets(couple);

        PlayerChatEvent.Formatter previous = event.getFormatter();
        event.setFormatter((from, message) -> Message.translation("server.marriage.channel.line")
                .param("line", previous.format(from, message))
                .color(Color.PINK));

        MarriageManager.recordMessage(senderUuid);
    }
}
