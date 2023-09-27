/*

 */
package dev.unnm3d.kalyaclaims;

import net.william278.husktowns.BukkitHuskTowns;


public class KalyaClaims {
    private final BukkitHuskTowns huskTowns;

    public KalyaClaims(BukkitHuskTowns huskTowns){
        this.huskTowns=huskTowns;
        huskTowns.getServer().getPluginManager().registerEvents(new ClaimListener(this), huskTowns);
    }

    public BukkitHuskTowns getHuskTowns(){
        return huskTowns;
    }
}
