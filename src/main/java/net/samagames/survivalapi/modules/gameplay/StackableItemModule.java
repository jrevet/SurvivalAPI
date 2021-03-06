package net.samagames.survivalapi.modules.gameplay;

import net.samagames.survivalapi.SurvivalAPI;
import net.samagames.survivalapi.SurvivalPlugin;
import net.samagames.survivalapi.modules.AbstractSurvivalModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/*
 * This file is part of SurvivalAPI.
 *
 * SurvivalAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SurvivalAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SurvivalAPI.  If not, see <http://www.gnu.org/licenses/>.
 */
public class StackableItemModule extends AbstractSurvivalModule
{
    /**
     * Constructor
     *
     * @param plugin Parent plugin
     * @param api API instance
     * @param moduleConfiguration Module configuration
     */
    public StackableItemModule(SurvivalPlugin plugin, SurvivalAPI api, Map<String, Object> moduleConfiguration)
    {
        super(plugin, api, moduleConfiguration);

    }

    /**
     * Event to patch stackable items
     *
     * @param event Event
     */
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventory(InventoryClickEvent event)
    {
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        Inventory top = event.getView().getTopInventory();
        InventoryType topType = top.getType();

        String topName = top.getName();

        if (event.getRawSlot() < 2 && topType == InventoryType.CHEST && (topName.equalsIgnoreCase("Horse") || topName.equalsIgnoreCase("Donkey") || topName.equalsIgnoreCase("Mule") || topName.equalsIgnoreCase("Undead horse") || topName.equalsIgnoreCase("Skeleton horse")))
            return;

        InventoryAction action = event.getAction();

        if (action == InventoryAction.DROP_ALL_SLOT || action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ONE_SLOT || action == InventoryAction.DROP_ONE_CURSOR)
            return;

        if (cursor != null && clicked != null)
        {
            Player player = (Player) event.getWhoClicked();

            Material cursorType = cursor.getType();
            int cursorAmount = cursor.getAmount();

            Material clickedType = clicked.getType();
            int clickedAmount = clicked.getAmount();

            int maxItems = 0;

            boolean cursorEmpty = cursorType == Material.AIR;
            boolean slotEmpty = clickedType == Material.AIR;

            if (event.isLeftClick() && !cursorEmpty && !slotEmpty)
            {
                boolean sameType = clickedType.equals(cursorType);

                if (sameType && cursor.isSimilar(clicked))
                {
                    int total = clickedAmount + cursorAmount;

                    if (total <= maxItems && total > clicked.getMaxStackSize())
                    {
                        ItemStack clone = cursor.clone();
                        clone.setAmount(total);
                        event.setCurrentItem(clone);

                        event.setCursor(null);
                        event.setResult(Event.Result.DENY);

                        if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH)
                            Bukkit.getScheduler().runTaskLater(plugin, player::updateInventory, 1);
                    }
                    else
                    {
                        ItemStack clone = cursor.clone();
                        clone.setAmount(maxItems);
                        event.setCurrentItem(clone);

                        ItemStack clone2 = cursor.clone();
                        clone2.setAmount(total - maxItems);
                        event.setCursor(clone2);

                        event.setResult(Event.Result.DENY);
                    }
                }
            }
        }
    }
}
