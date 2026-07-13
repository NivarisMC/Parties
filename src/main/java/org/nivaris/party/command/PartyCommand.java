package org.nivaris.party.command;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.CommandResult;
import org.allaymc.api.command.CommandSender;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.form.Forms;
import org.allaymc.api.form.type.CustomForm;
import org.allaymc.api.form.type.SimpleForm;
import org.allaymc.api.player.Player;
import org.allaymc.api.server.Server;
import org.nivaris.party.Invitation;
import org.nivaris.party.Main;
import org.nivaris.party.Messages;
import org.nivaris.party.Party;
import org.nivaris.party.PartyException;

import java.util.List;

public class PartyCommand extends Command {

    public PartyCommand() {
        super("party", Messages.get("command.description"), "party");
    }

    @Override
    public void prepareCommandTree(org.allaymc.api.command.tree.CommandTree tree) {
        var root = tree.getRoot();

        root.str("create").optional().exec(context -> {
            Player player = playerSender(context);
            if (player == null) return context.fail();

            String type = context.getResult(0);
            boolean isPublic = "public".equals(type);

            try {
                Main.getInstance().getPartyManager().createParty(player, isPublic);
                player.sendMessage(Messages.get("party.create.success"));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }

            return context.success();
        }).root()
        .str("dispose").exec(context -> {
            Player player = playerSender(context);
            if (player == null) return context.fail();

            try {
                Main.getInstance().getPartyManager().disposeParty(player);
                player.sendMessage(Messages.get("party.dispose.success"));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }

            return context.success();
        }).root()
        .str("invite").str("target").exec(context -> {
            Player player = playerSender(context);
            if (player == null) return context.fail();

            String target = context.getResult(0);

            try {
                Main.getInstance().getPartyManager().inviteParty(player, target);
                player.sendMessage(Messages.get("party.invite.success"));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }

            return context.success();
        }).root()
        .str("accept").str("invitor").exec(context -> {
            Player player = playerSender(context);
            if (player == null) return context.fail();

            String invitor = context.getResult(0);

            try {
                Main.getInstance().getInvitationManager().getInvitation(player).accept(invitor);
                player.sendMessage(Messages.format("party.accept.success", invitor));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }

            return context.success();
        }).root()
        .str("reject").str("invitor").exec(context -> {
            Player player = playerSender(context);
            if (player == null) return context.fail();

            String invitor = context.getResult(0);

            try {
                Main.getInstance().getInvitationManager().getInvitation(player).reject(invitor);
                player.sendMessage(Messages.get("party.reject.success"));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }

            return context.success();
        }).root()
        .str("quit").exec(context -> {
            Player player = playerSender(context);
            if (player == null) return context.fail();

            try {
                Main.getInstance().getPartyManager().leaveParty(player);
                player.sendMessage(Messages.get("party.quit.success"));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }

            return context.success();
        }).root()
        .str("kick").str("player").exec(context -> {
            Player player = playerSender(context);
            if (player == null) return context.fail();

            String target = context.getResult(0);

            try {
                Party party = Main.getInstance().getPartyManager().getPlayerParty(player);
                if (party == null) throw new PartyException(Messages.get("error.no_party"));
                if (!party.isOwner(player)) throw new PartyException(Messages.get("error.not_owner"));

                Player targetPlayer = Server.getInstance().getPlayerManager().getPlayerByName(target);
                if (targetPlayer == null) throw new PartyException(Messages.get("error.player_not_found"));

                Main.getInstance().getPartyManager().leaveParty(targetPlayer, true);
                player.sendMessage(Messages.format("party.kick.success", target));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }

            return context.success();
        });
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!sender.isPlayer()) {
                sender.sendMessage(Messages.get("error.not_a_player"));
                return CommandResult.fail();
            }

            EntityPlayer entityPlayer = sender.asPlayer();
            if (entityPlayer == null) return CommandResult.fail();
            Player player = entityPlayer.getController();
            Party party = Main.getInstance().getPartyManager().getPlayerParty(player);
            if (party != null) {
                sendManageForm(player, party);
            } else {
                sendMainForm(player);
            }
            return CommandResult.success(null);
        }
        return super.execute(sender, args);
    }

    private Player playerSender(org.allaymc.api.command.tree.CommandContext context) {
        CommandSender sender = context.getSender();
        if (!sender.isPlayer()) {
            context.addOutput(Messages.get("error.not_a_player"));
            return null;
        }
        EntityPlayer entityPlayer = sender.asPlayer();
        if (entityPlayer == null) return null;
        return entityPlayer.getController();
    }

    private void sendMainForm(Player player) {
        Invitation invitation = Main.getInstance().getInvitationManager().getInvitation(player);

        SimpleForm form = Forms.simple()
            .title(Messages.get("form.party.title"))
            .content("");

        form.button(Messages.get("button.create_party")).onClick(btn -> {
            try {
                Main.getInstance().getPartyManager().createParty(player, false);
                player.sendMessage(Messages.get("party.create.success"));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }
        });

        form.button(Messages.format("button.view_invitations", invitation.getCountInvitor())).onClick(btn -> {
            sendInvitationForm(player);
        });

        player.viewForm(form);
    }

    private void sendManageForm(Player player, Party party) {
        SimpleForm form = Forms.simple()
            .title(Messages.get("form.manage.title"))
            .content("");

        form.button(Messages.format("button.manage_players", party.getMembersCount())).onClick(btn -> {
            sendManagePlayerForm(player, party);
        });

        form.button(Messages.get("button.invite")).onClick(btn -> {
            sendInviteForm(player);
        });

        form.button(party.isOwner(player) ? Messages.get("button.dispose") : Messages.get("button.leave")).onClick(btn -> {
            try {
                if (party.isOwner(player)) {
                    Main.getInstance().getPartyManager().disposeParty(player);
                    player.sendMessage(Messages.get("party.dispose.delete"));
                } else {
                    Main.getInstance().getPartyManager().leaveParty(player);
                    player.sendMessage(Messages.get("party.quit.success"));
                }
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }
        });

        player.viewForm(form);
    }

    private void sendInvitationForm(Player player) {
        Invitation invitation = Main.getInstance().getInvitationManager().getInvitation(player);

        SimpleForm form = Forms.simple()
            .title(Messages.get("form.invitations.title"))
            .content("");

        for (String invitorName : invitation.getInviters().keySet()) {
            form.button(invitorName).onClick(btn -> {
                sendActionInvitationForm(player, btn.getText());
            });
        }

        player.viewForm(form);
    }

    private void sendActionInvitationForm(Player player, String invitor) {
        SimpleForm form = Forms.simple()
            .title(Messages.format("form.invitation_action.title", invitor))
            .content("");

        form.button(Messages.get("button.accept")).onClick(btn -> {
            try {
                Main.getInstance().getInvitationManager().getInvitation(player).accept(invitor);
                player.sendMessage(Messages.format("party.accept.form_success", invitor));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }
        });

        form.button(Messages.get("button.reject")).onClick(btn -> {
            try {
                Main.getInstance().getInvitationManager().getInvitation(player).reject(invitor);
                player.sendMessage(Messages.format("party.reject.form_success", invitor));
            } catch (PartyException e) {
                player.sendMessage(e.getMessage());
            }
        });

        player.viewForm(form);
    }

    private void sendManagePlayerForm(Player player, Party party) {
        SimpleForm form = Forms.simple()
            .title(Messages.get("form.manage_players.title"))
            .content("");

        for (String member : party.getMembers().keySet()) {
            String text = member;
            if (party.getOwner().equals(member)) {
                text += Messages.get("label.party_leader");
            }
            form.button(text);
        }

        player.viewForm(form);
    }

    private void sendInviteForm(Player player) {
        String[] onlinePlayers = Server.getInstance().getPlayerManager().getPlayers().values().stream()
            .map(p -> p.getLoginData().getXname())
            .toArray(String[]::new);

        CustomForm form = Forms.custom()
            .title(Messages.get("form.invite_player.title"))
            .input(Messages.get("input.player_name"))
            .dropdown(Messages.get("dropdown.select_player"), List.of(onlinePlayers))
            .onResponse(responses -> {
                String playerName = responses.get(0);
                if (playerName.isEmpty()) {
                    int selectedIndex = Integer.parseInt(responses.get(1));
                    if (selectedIndex >= 0 && selectedIndex < onlinePlayers.length) {
                        playerName = onlinePlayers[selectedIndex];
                    }
                }
                try {
                    Main.getInstance().getPartyManager().inviteParty(player, playerName);
                    player.sendMessage(Messages.format("party.invite.success_to_player", playerName));
                } catch (PartyException e) {
                    player.sendMessage(e.getMessage());
                }
            });

        player.viewForm(form);
    }
}
