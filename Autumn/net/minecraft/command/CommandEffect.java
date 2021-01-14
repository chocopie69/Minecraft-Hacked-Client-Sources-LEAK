package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;

public class CommandEffect extends CommandBase {
   public String getCommandName() {
      return "effect";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getCommandUsage(ICommandSender sender) {
      return "commands.effect.usage";
   }

   public void processCommand(ICommandSender sender, String[] args) throws CommandException {
      if (args.length < 2) {
         throw new WrongUsageException("commands.effect.usage", new Object[0]);
      } else {
         EntityLivingBase entitylivingbase = (EntityLivingBase)getEntity(sender, args[0], EntityLivingBase.class);
         if (args[1].equals("clear")) {
            if (entitylivingbase.getActivePotionEffects().isEmpty()) {
               throw new CommandException("commands.effect.failure.notActive.all", new Object[]{entitylivingbase.getName()});
            }

            entitylivingbase.clearActivePotions();
            notifyOperators(sender, this, "commands.effect.success.removed.all", new Object[]{entitylivingbase.getName()});
         } else {
            int i;
            try {
               i = parseInt(args[1], 1);
            } catch (NumberInvalidException var11) {
               Potion potion = Potion.getPotionFromResourceLocation(args[1]);
               if (potion == null) {
                  throw var11;
               }

               i = potion.id;
            }

            int j = 600;
            int l = 30;
            int k = 0;
            if (i < 0 || i >= Potion.potionTypes.length || Potion.potionTypes[i] == null) {
               throw new NumberInvalidException("commands.effect.notFound", new Object[]{i});
            }

            Potion potion1 = Potion.potionTypes[i];
            if (args.length >= 3) {
               l = parseInt(args[2], 0, 1000000);
               if (potion1.isInstant()) {
                  j = l;
               } else {
                  j = l * 20;
               }
            } else if (potion1.isInstant()) {
               j = 1;
            }

            if (args.length >= 4) {
               k = parseInt(args[3], 0, 255);
            }

            boolean flag = true;
            if (args.length >= 5 && "true".equalsIgnoreCase(args[4])) {
               flag = false;
            }

            if (l > 0) {
               PotionEffect potioneffect = new PotionEffect(i, j, k, false, flag);
               entitylivingbase.addPotionEffect(potioneffect);
               notifyOperators(sender, this, "commands.effect.success", new Object[]{new ChatComponentTranslation(potioneffect.getEffectName(), new Object[0]), i, k, entitylivingbase.getName(), l});
            } else {
               if (!entitylivingbase.isPotionActive(i)) {
                  throw new CommandException("commands.effect.failure.notActive", new Object[]{new ChatComponentTranslation(potion1.getName(), new Object[0]), entitylivingbase.getName()});
               }

               entitylivingbase.removePotionEffect(i);
               notifyOperators(sender, this, "commands.effect.success.removed", new Object[]{new ChatComponentTranslation(potion1.getName(), new Object[0]), entitylivingbase.getName()});
            }
         }

      }
   }

   public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, this.getAllUsernames()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, Potion.func_181168_c()) : (args.length == 5 ? getListOfStringsMatchingLastWord(args, new String[]{"true", "false"}) : null));
   }

   protected String[] getAllUsernames() {
      return MinecraftServer.getServer().getAllUsernames();
   }

   public boolean isUsernameIndex(String[] args, int index) {
      return index == 0;
   }
}
