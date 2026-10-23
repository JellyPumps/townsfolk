# Townsfolk

Townsfolk is a Fabric mod for Minecraft 1.21.1 that adds citizens to your world. Each one has a home, a job, and a daily schedule.

I'm building it for my own multi-city world, after not finding any NPC mod that would give me the features I wanted. It isn't a settlement or colony mod. Citizens never place or break blocks.

## Status

Early development. The roadmap below shows what is done and what isn't. I'll update it on the go.

This is a hobby project, so progress may not be consistent as other things take priority.

## Planned features

- Citizens with a home, a workplace and a schedule tied to the in-game clock
- Road and sidewalk navigation using a waypoint graph that you place by hand
- Shops, with shopkeepers you can trade with
- Crossings where pedestrians wait for traffic
- Optional Minecraft Transit Railway support
- A cheap simulation for citizens that are far from every player

## Roadmap

- [ ] Homes, workplaces and a place registry
- [ ] Time-of-day schedules
- [ ] Waypoint navigation
- [ ] Near and far simulation
- [ ] Crossings
- [ ] Shops
- [ ] MTR support
- [ ] Vehicle support (probably a separate mod)

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API


## Design notes

This project uses Yarn mappings.

Only citizens near a player will exist as entities, to avoid lag and unnecessary simulations. The rest are stored as data and advanced by their schedules, then spawned in the right place when a player gets close. I haven't built this yet, so the details may change.

## Contributing

Issues are open. If you report a bug, include your mod list and, for stuck citizens, the coordinates and time of day. I'm not taking pull requests yet.