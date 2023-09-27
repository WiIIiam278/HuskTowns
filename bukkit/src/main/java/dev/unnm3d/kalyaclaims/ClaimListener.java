/*

 */
package dev.unnm3d.kalyaclaims;

import net.william278.husktowns.claim.Claim;
import net.william278.husktowns.events.ClaimEvent;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import net.william278.husktowns.user.BukkitUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class ClaimListener implements Listener {
    private final KalyaClaims kalyaClaims;

    public ClaimListener(KalyaClaims kalyaClaims) {
        this.kalyaClaims = kalyaClaims;
    }

    @EventHandler
    public void onClaimEvent(ClaimEvent event) {
        Optional<Member> member = kalyaClaims.getHuskTowns().getUserTown(event.getUser());
        if (member.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        if (member.get().town().getLevel() == 1) {
            event.getClaim().setType(Claim.Type.PLOT);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent joinEvent) {

        BukkitUser bukkitUser = BukkitUser.adapt(joinEvent.getPlayer());
        Optional<Member> resultino = kalyaClaims.getHuskTowns().getUserTown(bukkitUser);
        if (resultino.isPresent()) return;
        generateTownName(bukkitUser, bukkitUser.getUsername() + "-Colony", 0);

    }

    public void generateTownName(BukkitUser user, String townName, int iteration) {
        Optional<Town> optionalTown = kalyaClaims.getHuskTowns().getTowns().stream()
                .filter(town -> town.getName().equals(townName + (iteration == 0 ? "" : iteration)))
                .findFirst();
        if (optionalTown.isEmpty()) {
            kalyaClaims.getHuskTowns().getManager().towns().createTown(user, townName + (iteration == 0 ? "" : iteration));
        } else {
            generateTownName(user, townName, iteration + 1);
        }
    }

}
