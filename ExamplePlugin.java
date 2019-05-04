package example;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {

  @Override
  public void onEnable(){
      ClickableChatFunctions.getInstance().onEnable(this);
  }


  //Sends the player "Hello World" and when they click on it, they get trapped in a bedrock cage
  public void example(Player player){
    ClickableChatFunctions.getInstance().sendMessage(player, "Hello World", true, new PlayerClickAction() {

          @Override
          public void run(Player player) {
            Location location = player.getLocation();

                  location.getBlock().getRelative(BlockFace.DOWN).setType(Material.BEDROCK);

                  location.getBlock().getRelative(BlockFace.NORTH).setType(Material.BEDROCK);
                  location.getBlock().getRelative(BlockFace.EAST).setType(Material.BEDROCK);
                  location.getBlock().getRelative(BlockFace.SOUTH).setType(Material.BEDROCK);
                  location.getBlock().getRelative(BlockFace.WEST).setType(Material.BEDROCK);

                  location.clone().add(0, 2, 0).getBlock().setType(Material.BEDROCK);

                  //Must call at the end
                  ClickableChatFunctions.getInstance().removeFromQue(player);
          }
        });
  }

}
