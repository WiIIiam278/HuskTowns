/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.gui.census;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.gui.BukkitGuiManager;
import net.william278.husktowns.town.Member;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class MemberItem extends AbstractItem {
    private final Member member;
    private final BukkitGuiManager guiManager;

    public MemberItem(Member member, BukkitGuiManager guiManager) {
        this.member = member;
        this.guiManager = guiManager;
    }

    @Override
    public ItemProvider getItemProvider() {
        return guiManager.getGuiSettings().getCensusGuiSettings().getItem("memberItem")
                .toItemProvider(
                        "%member_name%", member.user().getUsername(),
                        "%member_role%", member.role().getName()
                );
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        guiManager.getPlugin().getOnlineUsers().stream()
                .filter(onlineUser -> onlineUser.getUuid().equals(member.user().getUuid()))
                .findFirst()
                .ifPresent(onlineUser ->
                        new MemberGui(onlineUser, this, BukkitHuskTowns.getInstance().getGUIManager()).open(player));
    }

    public Member getMember() {
        return member;
    }

}