package nomaj.arrowcounter;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class ArrowCounterHud extends CustomUIHud {
   private final Player player;

   public ArrowCounterHud(PlayerRef playerRef, Player player) {
      super(playerRef);
      this.player = player;
   }

   protected void build(UICommandBuilder builder) {
      builder.append("Pages/com.arrowcounter_counter.ui");
   }

   public void updateArrowCount() {
      // Check if player is holding a bow
      Inventory inventory = this.player.getInventory();
      if (inventory == null) {
         return;
      }
      ItemStack heldItem = inventory.getItemInHand();

      UICommandBuilder builder = new UICommandBuilder();
      if (heldItem == null || !isBow(heldItem)) {
         // No bow held - hide everything
         builder.set("#ArrowIcon.Visible", false);
         builder.set("#ArrowCount.Visible", false);
         builder.set("#NoArrowIcon.Visible", false);
         this.update(false, builder);
         return;
      }
      
      // Bow is held - count arrows and update display
      int arrowCount = countArrows(inventory);
      
      if (arrowCount == 0) {
         // Bow held but no arrows - show no-arrow icon only
         builder.set("#ArrowIcon.Visible", false);
         builder.set("#ArrowCount.Visible", false);
         builder.set("#NoArrowIcon.Visible", true);
      } else {
         // Bow held with arrows - show normal display
         builder.set("#ArrowIcon.Visible", true);
         builder.set("#ArrowCount.Visible", true);
         builder.set("#NoArrowIcon.Visible", false);
         builder.set("#ArrowCount.Text", String.valueOf(arrowCount));
      }
      
      this.update(false, builder);
   }

   private boolean isBow(ItemStack itemStack) {
      String itemId = itemStack.getItemId();
      return itemId.startsWith("Weapon_Shortbow_") || itemId.startsWith("Weapon_Crossbow_");
   }

   private int countArrows(Inventory inventory) {
      final int[] totalArrows = {0};

      // Use combined container to check all inventory spaces
      var combined = inventory.getCombinedEverything();
      if (combined != null) {
         combined.forEachWithMeta((slot, itemStack, unused) -> {
            if (itemStack != null && itemStack.getItemId().startsWith("Weapon_Arrow_")) {
               totalArrows[0] += itemStack.getQuantity();
            }
         }, null);
      }

      return totalArrows[0];
   }
}
