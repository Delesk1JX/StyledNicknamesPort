Simple, but configurable nicknaming mod for Forge and NeoForge.

Players can change their own nickname with `/nickname set <name>`, and administrators can set or
clear the nickname of anyone with `/styled-nicknames set|clear <player>`.

## Nickname formatting

A nickname supports the same text format as the original Fabric mod:

| Syntax | Example | Result |
| --- | --- | --- |
| Named colour | `<red>Alex` | Red text |
| Hex colour | `<#ff8800>Alex` | Any of 16 million colours |
| Short hex | `<#f80>Alex` | The same colour, shorter |
| Formatting | `<bold>`, `<italic>`, `<underline>`, `<st>`, `<obf>` | Bold, italic, underlined, struck through, obfuscated |
| Colour tag | `<color:lime>Alex` | Sets a colour from a name or a hex value |
| Gradient | `<gradient:#ff0000:#0000ff>Alex` | Interpolates between the listed colours |
| Hard gradient | `<hgr:#ff0000:#00ff00>Alex` | One colour per segment |
| Rainbow | `<rainbow>Alex` | Colours shift across the text |
| Hover | `<hover:tooltip>Alex</>` | Shows text or an entity on hover |
| Click | `<open_url:https://example.com>Alex</>` | Opens a link |
| Insert | `<insert:Alex>Alex</>` | Inserts text when shift clicked |
| Clearing | `<clear_color>`, `<clear>` | Removes colours or all formatting |
| Legacy codes | `&cAlex` | Only when `allowLegacyFormatting` is on |

`</>` closes the innermost tag, `</red>` closes a specific one and `<r>` resets everything. A
backslash escapes a tag: `\<red>` is shown as text rather than being applied.

Colours are white by default, which means the nickname does not lose the colour of the chat
background. `<#${nickname}>` in the config is what puts that colour in front of a name.

## Configuration

`config/styled-nicknames.json` is written on first start:

| Option | Default | Meaning |
| --- | --- | --- |
| `allowByDefault` | `true` | Whether players may set a nickname without being an operator |
| `nicknameFormat` | `#${nickname}` | How the name is replaced in a nickname |
| `nicknameFormatColor` | `${nickname}` | Used instead when the nickname only changes the colour |
| `maxLength` | `48` | Longest nickname allowed; `0` disables the check |
| `changeDisplayName` | `true` | Whether the nickname replaces the name in chat |
| `changePlayerListName` | `false` | Whether the nickname also replaces the name in the player list |
| `allowLegacyFormatting` | `false` | Whether `&c` style codes work |
| `allowSpacesInNicknames` | `false` | Whether a nickname may contain spaces |
| `defaultEnabledFormatting` | see file | Which formatting tags players may use |

Every tag can be switched off individually in `defaultEnabledFormatting`. A tag that is off is not
applied even if a player types it, so a nickname cannot smuggle formatting past the config. Operators
keep every tag.

The messages can use the `${nickname}` placeholder, for example
`"nicknameChangedMessage": "<green>Your nickname is <yellow>${nickname}"`.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/nickname set <name>` | player | Sets your own nickname |
| `/nickname clear` | player | Removes your nickname |
| `/nick set <name>` | player | Short form of the above |
| `/realname <name>` | player | Finds the player behind a nickname |
| `/styled-nicknames` | everyone | Shows the mod version |
| `/styled-nicknames reload` | operator | Reloads the config |
| `/styled-nicknames set <player> <name>` | operator | Sets another player's nickname |
| `/styled-nicknames clear <player>` | operator | Removes another player's nickname |

A nickname survives a restart and follows a player between servers only if both servers share the
same player data.

## Notes

- This is an unofficial port. The original by Patbox is not involved in or endorsing it.
- Nothing else needs to be installed: the text format parser this mod uses is bundled.
- Nicknames are stored in the player's own data, so a nickname set on one server does not follow the
  player to another one.
