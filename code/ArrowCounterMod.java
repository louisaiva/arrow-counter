package nomaj.arrowcounter;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ArrowCounterMod extends JavaPlugin {
   private ArrowCounterHudManager hudManager;

   public ArrowCounterMod(@Nonnull JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      super.setup();
      this.hudManager = new ArrowCounterHudManager(this);
      this.getLogger().at(Level.INFO).log("ArrowCounter plugin loaded! Will show the count of arrows when (cross)bow is held.");
   }

   protected void shutdown() {
      super.shutdown();
      if (this.hudManager != null) {
         this.hudManager.shutdown();
         this.getLogger().at(Level.INFO).log("ArrowCounter plugin shut down. byebye see u");
      }

   }
}
