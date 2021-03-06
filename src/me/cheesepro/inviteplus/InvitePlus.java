package me.cheesepro.inviteplus;

import me.cheesepro.inviteplus.commands.CommandInvite;
import me.cheesepro.inviteplus.commands.CommandInvitedby;
import me.cheesepro.inviteplus.listeners.PlayerJoinListener;
import me.cheesepro.inviteplus.utils.Config;
import me.cheesepro.inviteplus.utils.ConfigManager;
import me.cheesepro.inviteplus.utils.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mark on 2015-03-14.
 */
public class InvitePlus extends JavaPlugin implements Listener{

    public static String pluginName = ChatColor.AQUA.toString() + ChatColor.BOLD + "["+ChatColor.YELLOW.toString()+ChatColor.BOLD+"InvitePlus"+ChatColor.AQUA.toString()+ChatColor.BOLD+"]"+ChatColor.RESET;
    public static String consolepluginName = "[INFO] ";
    public static Map<String, List<String>> cache = new HashMap<String, List<String>>();
    public static Map<String, Integer> count = new HashMap<String, Integer>();
    public static Map<String, Map<String, List<String>>> listRewards = new HashMap<String, Map<String, List<String>>>();
    public static Map<String, Map<String, String>> rewards = new HashMap<String, Map<String, String>>();
    public static Map<String, Integer> rewardHistories = new HashMap<String, Integer>();
    public static Map<String, String> messages = new HashMap<String, String>();
    public static Economy economy = null;
    public static boolean creditEnabled = true;
    Logger logger = new Logger(this);
    ConfigManager configManager;
    Config data;

    public void onEnable(){
        saveDefaultConfig();
        loadConfig();
        cacheConfig();
        registerCommands();
        registerListeners();
        if(setupEconomy()) try {
            logger.send("Vault dependency found! Money feature enabled!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.send("Vault dependency found! But an error as occurred!");
        }
        else{
            logger.send("Vault dependency not found! Money feature disabled!");
        }
        logger.send("Enabled!");
    }

    public void onDisable(){
        logger.send("Disabled");
    }

    void registerCommands(){
        CommandInvite commandInviteInstance = new CommandInvite(this);
        getCommand("invite").setExecutor(commandInviteInstance);

        CommandInvitedby commandInvitedbyInstance = new CommandInvitedby(this);
        getCommand("invitedby").setExecutor(commandInvitedbyInstance);
    }

    void registerListeners(){
        new PlayerJoinListener(this);
    }

    void loadConfig(){
        configManager = new ConfigManager(this);
        data = configManager.getNewConfig("data.yml", new String[]{"InvitePlus data storage"});
    }

    void cacheConfig(){
        if(getConfig().get("credit-onjoin")!=null){
            creditEnabled = Boolean.parseBoolean(String.valueOf(getConfig().get("credit-onjoin")));
        }
        if(getConfig().get("rewards")!=null){
            for(String reward : getConfig().getConfigurationSection("rewards").getKeys(false)){
                if(getConfig().get("rewards."+reward+".count")!=null) {
                    try{
                        int count = Integer.parseInt(String.valueOf(getConfig().get("rewards."+reward+".count")));
                        if(count!=0){
                            Map<String, String> value = new HashMap<String, String>();
                            Map<String, List<String>> rewardmessages = new HashMap<String, List<String>>();
                            Map<String, List<String>> rewardbroadcasts = new HashMap<String, List<String>>();
                            Map<String, List<String>> rewardcommands = new HashMap<String, List<String>>();
                            value.put("count", String.valueOf(getConfig().get("rewards."+reward+".count")));
                            if (getConfig().get("rewards."+reward+".messages") != null) {
                                List<String> messagescache = getConfig().getStringList("rewards." + reward + ".messages");
                                rewardmessages.put(reward, messagescache);
                            }
                            if (getConfig().get("rewards."+reward+".broadcasts") != null) {
                                List<String> broadcastcache = getConfig().getStringList("rewards." + reward + ".broadcasts");
                                rewardbroadcasts.put(reward, broadcastcache);
                            }
                            if (getConfig().get("rewards."+reward+".commands") != null) {
                                List<String> broadcastcache = getConfig().getStringList("rewards."+reward+".commands");
                                rewardcommands.put(reward, broadcastcache);
                            }
                            if(setupEconomy()){
                                if (getConfig().get("rewards."+reward+".money") != null) {
                                    value.put("money", String.valueOf(getConfig().get("rewards."+reward+".money")));
                                }
                            }
                            rewards.put(reward, value);
                            listRewards.put("commands", rewardcommands);
                            listRewards.put("messages", rewardmessages);
                            listRewards.put("broadcasts", rewardbroadcasts);
                        }else{
                            logger.send("Count value of reward " + reward + " cannot be 0! Must be at least 1.");
                        }
                    }catch (NumberFormatException e){
                        logger.send("Value of count can only be a INTEGER!");
                    }catch (NullPointerException e){
                        logger.send("Please make sure that you filled in all the values in the config!");
                    }catch (Exception e){
                        logger.send("Unknown Exception!");
                    }
                }else{
                    logger.send("In order to create a valid reward, the count value must be at least 1.");
                }
            }
        }
        if(getConfig().get("messages")!=null){
            for(String message : getConfig().getConfigurationSection("messages").getKeys(false)){
                messages.put(message, getConfig().getString("messages."+message));
            }
        }
        if(data.get("inviters")!=null){
            for(String invitersCache : data.getConfigurationSection("inviters").getKeys(false)){
                List<String> inviteds = data.getStringList("inviters." + invitersCache);
                cache.put(invitersCache, inviteds);
            }
        }
        if(data.get("count")!=null){
            for(String invitersCache : data.getConfigurationSection("count").getKeys(false)){
                count.put(invitersCache, data.getInt("count." + invitersCache));
            }
        }
        if(data.get("rewardhistories")!=null){
            for(String player : data.getConfigurationSection("rewardhistories").getKeys(false)){
                rewardHistories.put(player, data.getInt("rewardhistories."+player));
            }
        }
    }

    public boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }


    public String getPluginName(){
        return pluginName;
    }

    public String getConsolepluginName(){
        return consolepluginName;
    }

    public Config getData(){
        return data;
    }

    public Map<String, List<String>> getCache(){
        return cache;
    }

    public Map<String, Integer> getCount(){
        return count;
    }

    public Map<String, Map<String, String>> getRewards(){
        return rewards;
    }

    public Map<String, Map<String, List<String>>> getListRewards() {
        return listRewards;
    }

    public Map<String, Integer> getRewardHistories() {
        return rewardHistories;
    }

    public Map<String, String> getMessages(){
        return messages;
    }

    public boolean isCreditEnabled(){
        return creditEnabled;
    }

    public Economy getEconomy(){
        return economy;
    }

}
