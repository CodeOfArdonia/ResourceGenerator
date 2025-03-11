# Resource Generator

This is a server side mod to place resource generators. Suitable for minigames, skyblock and so on.

## Configuration

This mod use command `/resgen` to control, need permission 0 in local game and permission 2 on dedicated server.

`/resgen <pos> add|remove|info|list|highlight`

- `...add <interval> (item <item> <count>|itemTag <tag>|block <block state>|blockTag <tag>|lootTable <loot table>`

Place a resource generator.

Note: For block & block tag, interval is count after broken the block.

- `...remove`

Remove a resource generator.

- `...info`

Get all info of a specific generator.

- `...list (chunk|range <distance>`

List generators in specific range.

- `...highlight (chunk|range <distance>`

Highlight generators with particles in specific range.