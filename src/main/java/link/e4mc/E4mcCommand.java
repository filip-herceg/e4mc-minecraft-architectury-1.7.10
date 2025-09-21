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
        return "/e4mc <start|stop|restart|status|debug>";
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
        } else if ("status".equals(subCommand)) {
            handleStatusCommand(sender);
        } else if ("debug".equals(subCommand)) {
            handleDebugCommand(sender, args);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + getCommandUsage(sender)));
        }
    }
    private static final String KEY_DEBUG_ENABLED = "text.e4mc_minecraft.debug.enabled";
    private static final String KEY_DEBUG_DISABLED = "text.e4mc_minecraft.debug.disabled";
    private static final String KEY_DEBUG_STATUS = "text.e4mc_minecraft.debug.status";
    private static final String KEY_DEBUG_USAGE = "text.e4mc_minecraft.debug.usage";
    private static final String KEY_STATUS_NOSESSION = "text.e4mc_minecraft.status.noSession";
    private static final String KEY_STATUS_LINE = "text.e4mc_minecraft.status.line";
    private static final String KEY_STATUS_LASTERR = "text.e4mc_minecraft.status.lastError";
    private static final String KEY_STATUS_BROKER = "text.e4mc_minecraft.status.broker";
    
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
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + TextHelper.translate(KEY_DEBUG_ENABLED))); // reuse generic text
    }

    private void handleStartCommand(ICommandSender sender) {
        RelaySession s = E4mcClient.getSession();
        if (s == null) {
            s = new RelaySession();
            E4mcClient.setSession(s);
        }
        if (s.getState() == RelaySession.State.STOPPED) {
            s.startAsync();
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + TextHelper.translate(KEY_DEBUG_ENABLED))); // reuse generic text
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + TextHelper.translate("text.e4mc_minecraft.serverAlreadyOpen")));
        }
    }

    private void handleStatusCommand(ICommandSender sender) {
        RelaySession s = E4mcClient.getSession();
        if (s == null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + TextHelper.translate(KEY_STATUS_NOSESSION)));
            return;
        }
    String msg = String.format(TextHelper.translate(KEY_STATUS_LINE), s.getState(), s.getLastSource(),
        safe(s.getLastRelayId()), safe(s.getLastRelayHost()), String.valueOf(s.getLastRelayPort()));
    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + msg));
        String err = s.getLastError();
        if (err != null && !err.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + String.format(TextHelper.translate(KEY_STATUS_LASTERR), err)));
        }
        if ("broker".equals(s.getLastSource())) {
            String b = String.format(TextHelper.translate(KEY_STATUS_BROKER), safe(s.getLastBrokerUrl()), String.valueOf(s.getLastHttpCode()),
            preview(s.getLastHttpSnippet()));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + b));
        }
    }

    private void handleDebugCommand(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
                String.format(TextHelper.translate(KEY_DEBUG_STATUS), (Diagnostics.isEnabled() ? "ON" : "OFF"))));
            return;
        }
        String mode = args[1].toLowerCase();
        if ("on".equals(mode)) {
            Diagnostics.setEnabled(true);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + TextHelper.translate(KEY_DEBUG_ENABLED)));
        } else if ("off".equals(mode)) {
            Diagnostics.setEnabled(false);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + TextHelper.translate(KEY_DEBUG_DISABLED)));
        } else if ("status".equals(mode)) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
                    String.format(TextHelper.translate(KEY_DEBUG_STATUS), (Diagnostics.isEnabled() ? "ON" : "OFF"))));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + TextHelper.translate(KEY_DEBUG_USAGE)));
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String preview(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 100 ? s.substring(0, 100) + "..." : s;
    }
}
