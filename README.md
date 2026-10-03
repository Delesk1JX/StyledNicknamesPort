# Styled Nicknames — Forge and NeoForge ports

Unofficial community port of [Styled Nicknames](https://github.com/Patbox/StyledNicknames) by Patbox,
for the three game versions that have no official Fabric-free build:

| Target | Loader | Loader version |
| --- | --- | --- |
| Minecraft 1.20.1 | Forge | 47.4.10 |
| Minecraft 1.21.1 | NeoForge | 21.1.252 |
| Minecraft 26.1.2 | NeoForge | 26.1.2.112 |

The mod works the same way as the original: players set a nickname with
`/nickname set <name>`, and the name supports the Simplified Text Format and QuickText, hex colours,
gradients and click or hover actions.

## Layout

```
common/          the mod itself: version independent
forge-1.20.1/    Forge 1.20.1 build
neoforge-1.21.1/ NeoForge 1.21.1 build
neoforge-26.1.2/ NeoForge 26.1.2 build
tools/           helper scripts
docs/            distribution pages
```

`common/` holds everything that does not change between game versions: the text parser, the config,
the commands, and three of the four mixins. Each build folder adds only what genuinely differs.

## Building

Each target is a separate Gradle project and is built on its own:

```
cd forge-1.20.1     && gradlew.bat build
cd neoforge-1.21.1  && gradlew.bat build
cd neoforge-26.1.2  && gradlew.bat build
```

The resulting jars are named the way CurseForge and Modrinth expect:

```
forge-1.20.1/build/libs/stylednicknames-1.12.1-forge.jar
neoforge-1.21.1/build/libs/stylednicknames-1.12.1-neoforge.jar
neoforge-26.1.2/build/libs/stylednicknames-1.12.1-neoforge.jar
```

Run the tests with `gradlew.bat test`. They cover the text parser and check that every class the
mixins target still exists on that game version, which catches a renamed method before it silently
fails at runtime.

### Note on line endings

`gradlew` on Windows can write files with a UTF-8 byte order mark, which javac and Gradle's Groovy
parser reject on line 1. Run `powershell -File tools/strip-bom.ps1` if a build fails with
`illegal character` or `Unexpected character`.

## Publishing

Set the API tokens as environment variables, then run `gradlew.bat publishMods` in a build folder:

```
CURSEFORGE_API_TOKEN   from https://www.curseforge.com/account/api-tokens
MODRINTH_API_TOKEN     from https://modrinth.com/settings/pats
CHANGELOG              optional release notes
```

- CurseForge project: <https://www.curseforge.com/minecraft/mc-mods/styled-nicknames/1720679>
- Modrinth project: <https://modrinth.com/mod/o69zsSH1>

## Differences from the original Fabric mod

The port is self-contained: there is nothing extra to install. That required replacing two
dependencies that only ship for Fabric.

- **Placeholder API.** The original depends on Patbox's Placeholder API for the text format. No
  Forge or NeoForge release of it matches the API version the original uses, so the part of it this
  mod needs is bundled: the tag parsers, the tag registry and the text nodes. The syntax a player
  types is unchanged, including QuickText, the Simplified Text Format, legacy `&c` codes, hex
  colours, gradients and rainbow. The tags that need content introduced after 1.20.1, such as
  `<player>` or item hovers, are not available.
- **Player Data API.** There is no Forge or NeoForge release of it, so nicknames are stored in the
  player's persistent NBT. The game saves that with the rest of the player's data, so a nickname
  survives a restart either way.
- **Permissions.** Fabric has a permission API that NeoForge and Forge do not. Permissions here
  mean an operator level, which is what the loaders provide.

`defaultEnabledFormatting` in the config lists the same tags as the original and is written on
first start, so switching between the Fabric and Forge versions of the mod keeps the same options.

## License

LGPL-3.0-only, the same license as the original mod. See
`common/src/main/templates/LICENSE`, which is the upstream file. Original work by Patbox; the ports
are not endorsed by the original author.
