package nomaj.arrowcounter;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger.Api;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ArrowCounterHudManager {
   private final JavaPlugin plugin;
   private final Map<UUID, ArrowCounterHud> activeHuds;
   private final ScheduledExecutorService scheduler;

   public ArrowCounterHudManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.activeHuds = new ConcurrentHashMap();
      this.scheduler = Executors.newSingleThreadScheduledExecutor();
      this.registerListeners();
      this.startUpdateTask();
   }

   private void registerListeners() {
      this.plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, (event) -> {
         this.scheduler.schedule(() -> {
            this.handlePlayerJoin(event.getPlayerRef(), event.getPlayer());
         }, 500L, TimeUnit.MILLISECONDS);
      });
      this.plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, (event) -> {
         this.handlePlayerLeave(event.getPlayerRef());
      });
   }

   private void handlePlayerJoin(Ref<EntityStore> playerEntityRef, Player player) {
      PlayerRef playerRef = player.getPlayerRef();
      if (playerRef != null) {
         ArrowCounterHud hud = new ArrowCounterHud(playerRef, player);
         player.getHudManager().setCustomHud(playerRef, hud);
         this.activeHuds.put(playerRef.getUuid(), hud);
      }
   }

   private void handlePlayerLeave(PlayerRef playerRef) {
      this.activeHuds.remove(playerRef.getUuid());
   }

   private void startUpdateTask() {
      this.scheduler.scheduleAtFixedRate(() -> {
         try {
            Iterator var1 = this.activeHuds.values().iterator();

            while(var1.hasNext()) {
               ArrowCounterHud hud = (ArrowCounterHud)var1.next();
               if (hud.getPlayerRef().isValid()) {
                  hud.updateArrowCount();
               }
            }
         } catch (Exception var3) {
            ((Api)this.plugin.getLogger().at(Level.SEVERE).withCause(var3)).log("Error updating arrow counter HUD");
         }

      }, 0L, 100L, TimeUnit.MILLISECONDS);
   }

   public void shutdown() {
      this.activeHuds.clear();
      this.scheduler.shutdown();

      try {
         if (!this.scheduler.awaitTermination(1L, TimeUnit.SECONDS)) {
            this.scheduler.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.scheduler.shutdownNow();
      }

   }
}
