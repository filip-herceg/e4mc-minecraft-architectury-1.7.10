package link.e4mc;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class E4mcCommand extends CommandBase {
    
    @Override
    public String getCommandName() {
        return "e4mc";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/e4mc <start|stop|restart>";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Allow all players to use this command, we'll check permissions manually
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            // In unit tests or headless environments, allow execution
            return true;
        }
        if (server.isDedicatedServer()) {
            return sender.canCommandSenderUseCommand(4, getCommandName());
        } else {
            // In single player, only the owner can use it
            String owner = server.getServerOwner();
            String name = sender != null ? sender.getCommandSenderName() : null;
            return owner != null && owner.equals(name);
        }
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + getCommandUsage(sender)));
            return;
        }
        
        String subCommand = args[0].toLowerCase();
        
        if ("stop".equals(subCommand)) {
            handleStopCommand(sender);
        } else if ("restart".equals(subCommand)) {
            handleRestartCommand(sender);
        } else if ("start".equals(subCommand)) {
            handleStartCommand(sender);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + getCommandUsage(sender)));
        }
    }
    
    private void handleStopCommand(ICommandSender sender) {
        RelaySession s = E4mcClient.getSession();
        if (s != null && s.getState() != RelaySession.State.STOPPED) {
            s.stop();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + 
                TextHelper.translate("text.e4mc_minecraft.closeServer")));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + 
                TextHelper.translate("text.e4mc_minecraft.serverAlreadyClosed")));
        }
    }
    
    private void handleRestartCommand(ICommandSender sender) {
        RelaySession s = E4mcClient.getSession();
        if (s != null && s.getState() != RelaySession.State.STARTED) {
            s.stop();
        }
        RelaySession newSession = new RelaySession();
        newSession.startAsync();
        E4mcClient.setSession(newSession);
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Restarting e4mc relay connection..."));
    }

    private void handleStartCommand(ICommandSender sender) {
        RelaySession s = E4mcClient.getSession();
        if (s == null) {
            s = new RelaySession();
            E4mcClient.setSession(s);
        }
        if (s.getState() == RelaySession.State.STOPPED) {
            s.startAsync();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Starting e4mc relay connection..."));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + TextHelper.translate("text.e4mc_minecraft.serverAlreadyOpen")));
        }
    }
}
