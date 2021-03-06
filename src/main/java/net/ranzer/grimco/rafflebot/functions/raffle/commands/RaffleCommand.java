package net.ranzer.grimco.rafflebot.functions.raffle.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.ranzer.grimco.rafflebot.commands.BotCommand;
import net.ranzer.grimco.rafflebot.commands.Describable;
import net.ranzer.grimco.rafflebot.data.GuildManager;
import net.ranzer.grimco.rafflebot.functions.raffle.Raffle;
import net.ranzer.grimco.rafflebot.functions.raffle.commands.manage.BanUserCommand;
import net.ranzer.grimco.rafflebot.functions.raffle.commands.manage.ChannelEnableCommand;
import net.ranzer.grimco.rafflebot.functions.raffle.commands.manage.ModifyRolesCommand;
import net.ranzer.grimco.rafflebot.functions.raffle.commands.run.*;
import net.ranzer.grimco.rafflebot.util.Logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RaffleCommand extends AbstractRaffleCommand implements Describable {

    private static final List<BotCommand> subCommands;

    static {
        subCommands=new ArrayList<>();
        subCommands.add(new RaffleOpenCommand());
        subCommands.add(new RaffleEnterCommand());
        subCommands.add(new RaffleWithdrawCommand());
        subCommands.add(new RaffleCloseCommand());
        subCommands.add(new RaffleDrawCommand());
        subCommands.add(new RaffleEndCommand());
        subCommands.add(new ModifyRolesCommand());
        subCommands.add(new BanUserCommand());
        subCommands.add(new ChannelEnableCommand());

    }

    @Override
    public void process(String[] args, MessageReceivedEvent event) {

       if (args.length == 0) {
            if(raffles.containsKey(event.getTextChannel())) {
                event.getChannel().sendMessage(statusEmbed(raffles.get(event.getTextChannel()))).queue();
            }
            return;
        }

        Optional<BotCommand> c = subCommands.stream().filter(cc -> cc.getAlias().contains(args[0])).findFirst();

        // Silent failure of miss-typed subcommands
        if (!c.isPresent()) {
            Logging.debug("no raffle subcommand");
            return;
        }
        Logging.debug("raffle Subcommand: "+c.get().getName());
        if (!(c.get().getName().equals("enable")||c.get().getName().equals("role"))){
            if (!GuildManager.getGuildData(event.getGuild()).getChannel(event.getTextChannel()).getRaffle()){
                event.getChannel().sendMessage("Raffles are Disabled in this channel").queue();
                return;
            }
        }
        c.get().runCommand(Arrays.copyOfRange(args, 1, args.length), event);

    }

    private MessageEmbed statusEmbed(Raffle r) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(getCategory().COLOR);
        eb.setAuthor("Current Raffle Status");
        eb.addField("Status",r.isOpen()?"Open":"Closed",true);
        eb.addField("Entries",Integer.toString(r.getNumEntries()),true);

        return eb.build();
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList("raffle","giveaway");
    }

    @Override
    public String getShortDescription() {
        return "Be fair when giving stuff";
    }

    @Override
    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();


        sb.append(getShortDescription()).append("\n\n");

        for (BotCommand cmd : subCommands) {
            sb.append(
                    String.format("**%s**: %s\n", cmd.getName(), ((Describable)cmd).getShortDescription())
            );
        }

        return sb.toString();
    }

    @Override
    public String getUsage(Guild g) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("`%s%s {", getPrefix(g), getName()));
        for(BotCommand cmd : subCommands){
            sb.append(String.format("%s|", cmd.getName()));
        }
        sb.delete(sb.length()-1,sb.length());
        sb.append("}`");

        return sb.toString();
    }

    @Override
    public boolean hasSubcommands() {
        return true;
    }

    @Override
    public List<BotCommand> getSubcommands() {
        return subCommands;
    }
}
