# Parties

An **AllayMC** plugin that lets players create and manage parties to play together between servers.

## Requirements

- AllayMC API `0.28.0`
- Java 21
- [`ProxyThread`](https://github.com/NivarisMC/ProxyThread) library for cross-server synchronization

## Installation

1. Build the plugin with Gradle:
   ```bash
   ./gradlew shadowJar
   ```
2. Copy the generated jar from `build/libs/` into the `plugins/` folder of your AllayMC server.
3. Make sure the `proxythread-allay` library is also installed or loaded as a plugin on the server.
4. Start the server and adjust `config.yml` and `messages.yml` to your needs.

## Configuration

`config.yml` handles cross-server synchronization:

```yaml
server-list:
  lobby1:
    socket-port: 19200
  sw_solo:
    socket-port: 19201
  sw_doubles:
    socket-port: 19202

current-server: lobby1
```

- `server-list`: list of connected servers with the socket port used by `proxythread-allay`.
- `current-server`: name of the current server (must match a key in `server-list`).

## Commands

All commands use `/party`:

| Command | Description |
|---------|-------------|
| `/party` | Opens the main or management menu. |
| `/party create [public]` | Creates a new party. Add `public` to make it public. |
| `/party dispose` | Deletes the party (owner only). |
| `/party invite <player>` | Invites a player to the party. |
| `/party accept <inviter>` | Accepts a received invitation. |
| `/party reject <inviter>` | Rejects a received invitation. |
| `/party quit` | Leaves the party. |
| `/party kick <player>` | Removes a member from the party (owner only). |

## How it works

### Creation and management

- Each player can be in only one party at a time.
- A party has an **owner** (the creator) and supports up to **4 members** in total.
- The owner can invite other players, kick members, or dispose the party.
- If the owner leaves the party, the party is automatically disbanded.
- When a player leaves the server, they are removed from the party and their invitations are cleared.

### Invitations

- Invitations are managed per player: an invitee can have multiple pending invitations from different parties.
- Accepting an invitation adds the player to the party, provided they are not already in another party and the party is not full.

### User interface

Running `/party` without arguments opens a **Bedrock Form** that allows players to:

- Create a party
- View and manage received invitations
- Manage members of the current party
- Invite online players via an input/dropdown form
- Leave or delete the party

### Cross-server synchronization

The plugin integrates with `proxythread-allay` to keep parties alive when players are transferred between servers (e.g., from a lobby to a minigame):

- Party data (owner and members) is sent over sockets between servers.
- When a packet of type `party_proxy_data` is received, the party is automatically recreated on the target server.
- `PartyTransferManager` handles a transfer queue: it waits about 4 seconds (80 ticks) and then transfers all party members to the target server address.

## Events

The plugin emits custom events that other plugins can listen to:

- `PartyCreateEvent`
- `PartyDisposeEvent`
- `PartyJoinEvent`
- `PartyLeaveEvent`
- `PartyInviteEvent`
- `PartyAcceptEvent`
- `PartyRejectEvent`

Each event extends `PartyEvent` or `PlayerPartyEvent` and supports cancellation where applicable.

## Build

To produce the production jar:

```bash
./gradlew shadowJar
```

To test locally with AllayGradle:

```bash
./gradlew runServer
```

## Author

Made by **NivarisMC** — [GitHub](https://github.com/nivarismc)
