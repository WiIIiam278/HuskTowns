package net.william278.husktowns.hook.economy;

import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import net.william278.husktowns.HuskTowns;

public class RedisEconomyHook extends VaultHook {

    public RedisEconomyHook(HuskTowns plugin) {
        super(plugin);
    }

    @Override
    public boolean initialize() {
        if (!HuskTowns.getSettings().doEconomy) {
            return false;
        }
        RedisEconomyAPI api = RedisEconomyAPI.getAPI();
        if (api == null) {
            return super.initialize();
        }
        economy = api.getCurrencyByName(HuskTowns.getSettings().redisEconomyCurrencyName);
        if (economy == null) {
            return super.initialize();
        }
        return true;
    }
}
