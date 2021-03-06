package net.samagames.survivalapi.game.types.run;

import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.game.SurvivalGameLoop;
import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.survivalapi.utils.TimedEvent;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

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
public class RunBasedGameLoop extends SurvivalGameLoop
{
    /**
     * Constructor
     *
     * @param plugin Parent plugin
     * @param server Server instance
     * @param game Game instance
     */
    public RunBasedGameLoop(JavaPlugin plugin, Server server, SurvivalGame game)
    {
        super(plugin, server, game);

        this.episodeEnabled = false;
    }

    @Override
    public void createDamageEvent()
    {
        this.nextEvent = new TimedEvent(1, 0, "Dégats actifs", ChatColor.GREEN, false, () ->
        {
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats sont désormais actifs.", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map sera réduite dans 19 minutes. Le PvP sera activé à ce moment là.", true);
            this.game.enableDamages();

            this.createTeleportationEvent();
        });
    }

    public void createTeleportationEvent()
    {
        this.nextEvent = new TimedEvent(19, 0, "Téléportation", ChatColor.YELLOW, true, () ->
        {
            SamaGamesAPI.get().getGameManager().setMaxReconnectTime(-1);

            this.game.disableDamages();
            ((RunBasedGame) this.game).teleportDeathMatch();

            for (SurvivalPlayer player : (Collection<SurvivalPlayer>) this.game.getInGamePlayers().values())
            {
                if(!player.isOnline())
                    continue;

                try
                {
                    player.getPlayerIfOnline().removePotionEffect(PotionEffectType.SPEED);
                    player.getPlayerIfOnline().removePotionEffect(PotionEffectType.FAST_DIGGING);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            this.game.setWorldBorderSize(400.0D);
            this.game.setWorldBorderSize(50.0D, 10L * 60L);

            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map est désormais réduite. Les bordures sont en coordonnées " + ChatColor.RED + "-200 +200" + ChatColor.RESET + ".", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats et le PvP seront activés dans 30 secondes !", true);

            this.createDeathmatchEvent();
        });
    }

    public void createDeathmatchEvent()
    {
        this.nextEvent = new TimedEvent(0, 30, "PvP activé", ChatColor.RED, false, () ->
        {
            this.game.enableDamages();
            this.game.enablePVP();

            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats et le PvP sont maintenant activés. Bonne chance !", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map est maintenant en réduction constante pendant les 10 prochaines minutes.", true);

            this.createReducingEvent();
        });
    }

    @Override
    public void createReducingEvent()
    {
        this.nextEvent = new TimedEvent(9, 30, "Fin de la réduction", ChatColor.RED, false, () ->
        {
            this.game.setWorldBorderSize(50.0D);

            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map est désormais réduite. Fin de la partie forcée dans 2 minutes !", true);
            this.createEndEvent();
        });
    }

    public void createEndEvent()
    {
        this.nextEvent = new TimedEvent(2, 0, "Fin de la partie", ChatColor.RED, true, () ->
        {
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La partie se termine.", true);
            this.server.shutdown();
        });
    }
}
